package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.component.IUtilComponent;
import com.bancatlan.atmauthorizer.component.impl.UtilComponentImpl;
import com.bancatlan.atmauthorizer.dto.VoucherTransactionDTO;
import com.bancatlan.atmauthorizer.exception.AuthorizerError;
import com.bancatlan.atmauthorizer.exception.ModelCustomErrorException;
import com.bancatlan.atmauthorizer.exception.ModelNotFoundException;
import com.bancatlan.atmauthorizer.model.Customer;
import com.bancatlan.atmauthorizer.model.Transaction;
import com.bancatlan.atmauthorizer.model.Voucher;
import com.bancatlan.atmauthorizer.repo.IVoucherRepo;
import com.bancatlan.atmauthorizer.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherServiceImpl implements IVoucherService {
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
                throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_SUPPORTED_VOUCHER_PROCESS_CODE);
        }
    }

    @Override
    public VoucherTransactionDTO bankVerifyPayment(VoucherTransactionDTO dto) {
        if (dto.getTransaction() != null && dto.getTransaction().getId() != null) {
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_SUPPORTED_TXN_ID_BANK_PAYMENT_SERVICE_ACTION);
        }

        if (dto.getSessionKey() == null || dto.getSessionKey().equals("")) {
            throw new ModelCustomErrorException(Constants.PARAMETER_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.MISSING_OCB_SESSION_KEY);
        }
        UtilComponentImpl.setSessionKey(dto.getSessionKey());
        //(1)
        Transaction txnVoucher = transaction.initTxn(dto.getTransaction());
        dto.getTransaction().setId(txnVoucher.getId());
        dto.getTransaction().setCurrency(txnVoucher.getCurrency());

        //(2)
        transaction.preAuthorizationTxn(txnVoucher);

        //(3)
        transaction.authorizationTxn(dto.getTransaction());
        /*authorize txn => verify access, privileges, limits of payer and payee txn*/

        //(4)
        transaction.verifyTxn(dto.getTransaction());
        //throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.VERIFYING_PARTICIPANTS);
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
        //(5)
        transaction.confirmTxn(txnVoucher);
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

        Voucher voucher = repo.findByPickupCodeAndSecretCodeAndCustomer(dto.getVoucher().getPickupCode(),dto.getVoucher().getSecretCode(), cst);
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
        Transaction txnPaidBy = transaction.confirmTxn(txn);
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
        Transaction txnPaidBy = transaction.cancelConfirmTxn(txn);
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
        //find customer
        Customer cst = customer.getByMsisdn(dto.getTransaction().getPayer().getMsisdn());
        if(cst == null){
            //execStatus.setErrorCode("57");
            throw new ModelNotFoundException("User with Cellphone - " +
                    dto.getTransaction().getPayer().getMsisdn() + " - not found");
        }

        Voucher voucher = repo.findByPickupCodeAndSecretCodeAndCustomer(dto.getVoucher().getPickupCode(),dto.getVoucher().getSecretCode(), cst);
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
        Transaction txnPaidBy = transaction.confirmTxn(txn);
        Double currentAmount = voucher.getAmountCurrent() - txnPaidBy.getAmount();
        dto.setTransaction(txnPaidBy);
        //mark voucher
        if (currentAmount == 0) {
            voucher.setActive(false);
        }
        voucher.setAmountCurrent(currentAmount);
        voucher.setTxnPaidOutBy(txnPaidBy);
        voucher.setCustomerUpdate(cst);
        dto.setVoucher(this.update(voucher));
        return dto;
    }

    public VoucherTransactionDTO processCancelWithdraw(VoucherTransactionDTO dto) {
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
        Transaction txnPaidBy = transaction.cancelConfirmTxn(txn);
        Double currentAmount = voucher.getAmountCurrent() + txnPaidBy.getAmount();

        //mark voucher
        voucher.setActive(true);
        voucher.setAmountCurrent(currentAmount);
        voucher.setCustomerUpdate(voucher.getCustomer());
        dto.setTransaction(txnPaidBy);
        dto.setVoucher(this.update(voucher));
        return dto;
    }
}
