package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.component.IUtilComponent;
import com.bancatlan.atmauthorizer.component.impl.UtilComponentImpl;
import com.bancatlan.atmauthorizer.dto.VoucherTransactionDTO;
import com.bancatlan.atmauthorizer.exception.AtmError;
import com.bancatlan.atmauthorizer.exception.AuthorizerError;
import com.bancatlan.atmauthorizer.exception.ModelCustomErrorException;
import com.bancatlan.atmauthorizer.exception.ModelNotFoundException;
import com.bancatlan.atmauthorizer.model.Customer;
import com.bancatlan.atmauthorizer.model.Transaction;
import com.bancatlan.atmauthorizer.model.Voucher;
import com.bancatlan.atmauthorizer.repo.IVoucherRepo;
import com.bancatlan.atmauthorizer.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherServiceImpl implements IVoucherService {
    Logger LOG = LoggerFactory.getLogger(VoucherServiceImpl.class);
    @Autowired
    IVoucherRepo repo;

    @Autowired
    ITransactionService transaction;

    @Autowired
    ICustomerService customer;

    @Autowired
    IUserCaseSevice useCase;

    @Autowired
    ICurrencyService currency;

    @Autowired
    IBankService bankService;

    @Autowired
    IUtilComponent utilComponent;

    @Override
    public Voucher create(Voucher voucher) {
        voucher.setCreationDate(LocalDateTime.now());
        voucher.setExpirationDate(LocalDateTime.now().plusDays(3));
        voucher.setCustomer(voucher.getTxnCreatedBy().getPayee());
        return repo.save(voucher);
    }

    @Transactional
    @Override
    public VoucherTransactionDTO voucherProcess(VoucherTransactionDTO dto) {
        if (dto.getAction() == null || dto.getAction().equals("")) {
            throw new ModelCustomErrorException(Constants.PARAMETER_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_FOUND_BANK_PAYMENT_SERVICE_ACTION);
        }
        switch (dto.getAction().toUpperCase()) {
            case Constants.BANK_ACTION_VERIFY:
                return this.bankVerifyPayment(dto);
            case Constants.BANK_ACTION_PAYMENT:
                return this.bankConfirmPayment(dto);
            case Constants.ITM_PROCESS_CODE_WITHDRAW:
                return this.processWithdraw(dto);
            case Constants.ITM_PROCESS_CODE_REVERSE_WITHDRAW:
                return this.processCancelWithdraw(dto);
            default:
                throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AtmError.ERROR_12);
        }
    }

    @Override
    public VoucherTransactionDTO bankVerifyPayment(VoucherTransactionDTO dto) {
        LOG.info("Request from OCB", dto);
        dto = this.validateAndFitOcbRequest(dto);
        UtilComponentImpl.setSessionKey(dto.getSessionKey());
        //(1)
        Transaction txnVoucher = transaction.init(dto.getTransaction());
        dto.getTransaction().setId(txnVoucher.getId());
        dto.getTransaction().setCurrency(txnVoucher.getCurrency());

        //(2)
        transaction.authentication(dto.getTransaction());

        //(3)
        transaction.authorization(txnVoucher);

        //(4)
        transaction.verify(dto.getTransaction());
        return dto;
    }

    @Override
    public VoucherTransactionDTO bankConfirmPayment(VoucherTransactionDTO dto) {
        if(dto.getTransaction() == null || dto.getTransaction().getId() == null){
            throw new ModelNotFoundException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.MISSING_TXN_ON_REQUEST);
        }

        Transaction txnVoucher = transaction.getById(dto.getTransaction().getId());
        if(txnVoucher == null){
            throw new ModelNotFoundException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.MISSING_TXN_DOES_NOT_EXIST);
        }

        if (txnVoucher.getTxnStatus() != null && txnVoucher.getTxnStatus().getId() != null && txnVoucher.getTxnStatus().getId() >= Constants.CONFIRM_TXN_STATUS) {
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.ALREADY_PROCESSED_TXN);
        }
        this.securityValidation(dto);
        //(5)
        transaction.confirm(txnVoucher);
        String pickupCode = utilComponent.getPickupCodeByCellPhoneNumber(dto.getTransaction().getPayee().getMsisdn());

        dto.getVoucher().setPickupCode(pickupCode);
        dto.getVoucher().setTxnCreatedBy(txnVoucher);
        dto.getVoucher().setAmountInitial(txnVoucher.getAmount());
        dto.getVoucher().setAmountCurrent(txnVoucher.getAmount());
        dto.getVoucher().setActive(true);
        dto.getVoucher().setExpired(false);
        dto.getVoucher().setCanceled(false);
        dto.getVoucher().setDeleted(false);
        Voucher voucherResult = this.create(dto.getVoucher());

        dto.setTransaction(txnVoucher);
        dto.setVoucher(voucherResult);

        if(voucherResult != null){
            String template = Constants.TEMPLATE_NOTIFICATION_SMS;//Todo get template from DB
            String sms = String.format(template, String.valueOf(txnVoucher.getAmount()), pickupCode);
            bankService.sendNotification(dto.getTransaction().getPayee().getMsisdn(),"",sms,Constants.BANK_NOTIFICATION_SMS);
        }

        return dto;
    }

    @Override
    public Voucher withdraw(VoucherTransactionDTO dto) {
        //find customer
        Customer cst = customer.getByMsisdn(dto.getTransaction().getPayer().getMsisdn());
        if(cst == null){
            //execStatus.setErrorCode("57");
            throw new ModelNotFoundException("User with Cellphone - " +
                    dto.getTransaction().getPayer().getMsisdn() + " - not found");
        }

        Voucher voucher = this.findVoucherToWithdraw(dto.getVoucher().getPickupCode(),dto.getVoucher().getSecretCode(), cst);
        if(voucher == null || voucher.getAmountCurrent() == 0L || dto.getTransaction().getAmount() > voucher.getAmountCurrent()){//is missing if it's active
            throw new ModelNotFoundException(" Voucher with pickupCode " +
                    dto.getVoucher().getPickupCode() + " and secretCode " + dto.getVoucher().getSecretCode() + " - not found");
        }
        //Todo agregar validacion de limites privilegios ciclo de vida de txns
        //confirm txn
        Transaction txn = new Transaction();
        txn.setCurrency(currency.getById(Constants.HN_CURRENCY_ID));
        txn.setUseCase(useCase.getById(Constants.WITHDRAW_VOUCHER_USE_CASE));
        txn.setAmount(dto.getTransaction().getAmount());
        txn.setPayer(cst);
        txn.setPayee(customer.getById(Constants.ATM_USER_ID));
        txn.setAtmReference(dto.getTransaction().getAtmReference());
        txn.setVoucher(voucher);//Todo Es mejor crear una tabla maestra
        Transaction txnPaidBy = transaction.confirm(txn);
        Double currentAmount = voucher.getAmountCurrent() - txnPaidBy.getAmount();

        //mark voucher
        if (currentAmount == 0) {
            voucher.setActive(false);
        }
        voucher.setAmountCurrent(currentAmount);
        voucher.setTxnPaidOutBy(txnPaidBy);
        voucher.setCustomerUpdate(cst);
        return this.update(voucher);
    }

    @Override
    public Voucher findVoucherToWithdraw(String pickupCode, String secretCode, Customer customer) {
        return repo.findByPickupCodeAndSecretCodeAndCustomerAndIsActive(pickupCode,secretCode, customer, true);
    }

    @Override
    public Voucher findVoucherToReverse(String pickupCode, String secretCode, Customer customer) {
        return repo.findByPickupCodeAndSecretCodeAndCustomerAndIsActive(pickupCode,secretCode, customer, false);
    }

    @Override
    public Voucher cancelWithdraw(VoucherTransactionDTO dto) {
        //find customer
        Transaction txn = transaction.getTransactionByAtmReference(dto.getTransaction().getAtmReference());
        if(txn == null  || txn.getTxnStatus().getId().equals(Constants.CANCEL_CONFIRM_TXN_STATUS)){
            //execStatus.setErrorCode("57");
            throw new ModelNotFoundException("Txn and Voucher by this atm reference - " +
                    dto.getTransaction().getAtmReference() + " - not found or already canceled");
        }

        Voucher voucher = this.getById(txn.getVoucher().getId());
        if(voucher == null){//is missing if it's active
            throw new ModelNotFoundException(" Voucher with txn " +
                    txn.getId() + " - not found");
        }
        //Todo regresar el dinero de la cuenta de cajeros a la cuenta de ATM (o en el caso de Oscar P. regresar el dinero al usuario (pero en estado de congelado))
        //cancel txn
        Transaction txnPaidBy = transaction.cancelConfirm(txn);
        Double currentAmount = voucher.getAmountCurrent() + txnPaidBy.getAmount();

        //mark voucher
        voucher.setActive(true);
        voucher.setAmountCurrent(currentAmount);
        voucher.setCustomerUpdate(voucher.getCustomer());
        return this.update(voucher);
    }

    @Override
    public List<Voucher> getAllByOcbUser(String ocbUser) {
        return repo.findAllActiveByOcbUser(ocbUser);
    }

    @Override
    public Voucher update(Voucher voucher) {
        voucher.setUpdateDate(LocalDateTime.now());
        return repo.save(voucher);
    }

    @Override
    public List<Voucher> getAll() {
        return repo.findAll();
    }

    @Override
    public Voucher getById(Long id) {
        Optional<Voucher> voucher = repo.findById(id);
        return voucher.isPresent() ? voucher.get() : null;
    }

    @Override
    public Boolean delete(Long id) {
        Boolean retVal = true;
        try {
            repo.deleteById(id);
        }catch (Exception ex){
            retVal = false;
            System.out.println(ex);
        }
        return retVal;
    }

    public VoucherTransactionDTO processWithdraw(VoucherTransactionDTO dto) {
        //Atm reference
        if (dto.getTransaction() == null || dto.getTransaction().getAtmReference() == null || dto.getTransaction().getAtmReference().equals("")) {
            LOG.error("AtmReference in request is not defined", dto);
            throw new ModelNotFoundException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_76);
        }
        //(1)
        Transaction txn = transaction.init(dto.getTransaction());

        //(2)
        dto.getTransaction().setId(txn.getId());
        dto.getTransaction().setCurrency(txn.getCurrency());
        transaction.authentication(dto.getTransaction());

        //validate both PIN
        if (dto.getVoucher() == null || dto.getVoucher().getSecretCode() == null || dto.getVoucher().getPickupCode() == null ||
                dto.getVoucher().getSecretCode().equals("") || dto.getVoucher().getSecretCode().length() != 4 || dto.getVoucher().getPickupCode().equals("") ||
                dto.getVoucher().getPickupCode().length() != 5) {
            LOG.error("Codes in request are not properly values to be processed", dto);
            throw new ModelNotFoundException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_81);
        }

        Voucher voucher = this.findVoucherToWithdraw(dto.getVoucher().getPickupCode(), dto.getVoucher().getSecretCode(), txn.getPayer());
        if (!this.isValidVoucherToWithDraw(voucher, dto)) {
            LOG.error("Voucher found is not valid", voucher);
            LOG.error("Request => ", dto);
            throw new ModelNotFoundException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_76);
        }
        //(3)
        transaction.authorization(txn);

        //(4)
        transaction.verify(dto.getTransaction());

        txn.setAtmReference(dto.getTransaction().getAtmReference());
        txn.setVoucher(voucher);//Todo Es mejor crear una tabla maestra
        Transaction txnPaidBy = transaction.confirm(txn);

        Double currentAmount = voucher.getAmountCurrent() - txnPaidBy.getAmount();
        dto.setTransaction(txnPaidBy);
        //mark voucher
        if (currentAmount == 0) {
            voucher.setActive(false);
        }
        voucher.setAmountCurrent(currentAmount);
        voucher.setTxnPaidOutBy(txnPaidBy);
        voucher.setCustomerUpdate(txn.getPayer());
        dto.setVoucher(this.update(voucher));
        return dto;
    }

    public VoucherTransactionDTO processCancelWithdraw(VoucherTransactionDTO dto) {
        //Atm reference
        if (dto.getTransaction() == null || dto.getTransaction().getAtmReference() == null || dto.getTransaction().getAtmReference().equals("")) {
            LOG.error("processCancelWithdraw: AtmReference in request is not defined", dto);
            throw new ModelNotFoundException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_76);
        }

        //find customer
        Transaction txn = transaction.getTransactionByAtmReference(dto.getTransaction().getAtmReference());
        if(txn == null  || txn.getTxnStatus().getId().equals(Constants.CANCEL_CONFIRM_TXN_STATUS)){
            LOG.error("processCancelWithdraw: AtmReference was not found or already cancelled", txn);
            throw new ModelNotFoundException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_77);
        }

        Voucher voucher = this.getById(txn.getVoucher().getId());
        if (voucher == null || !this.isValidVoucherToReverse(voucher, dto)) {
            LOG.error("Voucher found is not valid", voucher);
            LOG.error("Request => ", dto);
            throw new ModelNotFoundException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_R1);
        }
        //cancel txn
        Transaction txnPaidBy = transaction.cancelConfirm(txn);
        Double currentAmount = voucher.getAmountCurrent() + txnPaidBy.getAmount();

        //mark voucher
        voucher.setActive(true);
        voucher.setAmountCurrent(currentAmount);
        voucher.setCustomerUpdate(voucher.getCustomer());
        dto.setTransaction(txnPaidBy);
        dto.setVoucher(this.update(voucher));
        return dto;
    }

    private Boolean isValidVoucherToWithDraw(Voucher voucher, VoucherTransactionDTO dto) {
        if (voucher == null || voucher.getAmountCurrent() == 0L || dto.getTransaction().getAmount() > voucher.getAmountCurrent() || !voucher.getActive()) {
            return false;
        }
        return true;
    }

    private Boolean isValidVoucherToReverse(Voucher voucher, VoucherTransactionDTO dto) {
        if (voucher == null || voucher.getAmountCurrent().equals(voucher.getAmountInitial()) || dto.getTransaction().getAmount() > voucher.getAmountInitial()) {
            return false;
        }
        return true;
    }

    private VoucherTransactionDTO validateAndFitOcbRequest(VoucherTransactionDTO dto) {
        /*OCB Validations*/
        if (dto.getSessionKey() == null || dto.getSessionKey().equals("")) {
            throw new ModelCustomErrorException(Constants.PARAMETER_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.MISSING_OCB_SESSION_KEY);
        }

        if (dto.getTransaction() != null && dto.getTransaction().getId() != null) {
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_SUPPORTED_TXN_ID_BANK_PAYMENT_SERVICE_ACTION);
        }

        if (dto.getTransaction().getPayee() == null && dto.getTransaction().getPayee().getMsisdn() == null && dto.getTransaction().getPayee().getMsisdn().equals("")) {
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_TARGET_TELEPHONE_FIELD);
        }

        if (dto.getValidatePayeeMsisdn() == null && dto.getValidatePayeeMsisdn().equals("")) {
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_CONFIRM_TARGET_TELEPHONE_FIELD);
        }

        if (!utilComponent.isValidPhoneNumber(dto.getTransaction().getPayee().getMsisdn())) {
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.BAD_FORMAT_TARGET_TELEPHONE);
        }

        if (!dto.getValidatePayeeMsisdn().equals(dto.getTransaction().getPayee().getMsisdn())) {
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_MATCH_CONFIRM_TARGET_TELEPHONE);
        }

        if (dto.getAmountKey() == null && dto.getAmountKey().equals("")) {
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_AMOUNT_TO_TRANSFER_FIELD);
        }

        if (dto.getAmountKey().equals(Constants.ATM_ANOTHER_AMOUNT_KEY)) {
            if (dto.getAmount() == null && dto.getAmount().equals("")) {
                throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_WRITTEN_AMOUNT);
            }
            if (!utilComponent.isValidAmountWithAtm(dto.getAmount())) {
                throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_MATCH_AMOUNT_WITH_ATM);
            }
            dto.getTransaction().setAmount(Double.parseDouble(dto.getAmount()));
        } else {
            dto.getTransaction().setAmount(utilComponent.getAmountFromKey(dto.getAmountKey()));
        }

        if (dto.getVoucher() == null || dto.getVoucher().getSecretCode() == null || dto.getVoucher().getSecretCode().equals("")) {
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_SECRET_CODE_FIELD);
        }

        if (!utilComponent.isANumber(dto.getVoucher().getSecretCode()) || dto.getVoucher().getSecretCode().length() != 4) {
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.BAD_FORMAT_SECRET_CODE);
        }

        /*OCB Adjustments*/
        if (dto.getTransaction().getCurrency() != null && dto.getTransaction().getCurrency().getCode() != null && dto.getTransaction().getCurrency().getCode().equals(Constants.BANK_HN_CURRENCY)) {
            dto.getTransaction().getCurrency().setCode(Constants.HN_CURRENCY);
        }
        return dto;
    }

    private void securityValidation(VoucherTransactionDTO dto){
        //Todo validate with session if the current id txn belong to the session linked with previous id txn generated
    }
}
