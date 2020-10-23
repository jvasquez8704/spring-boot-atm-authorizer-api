package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.dto.OcbVoucherDTO;
import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.component.IUtilComponent;
import com.bancatlan.atmauthorizer.component.impl.UtilComponentImpl;
import com.bancatlan.atmauthorizer.dto.VoucherTransactionDTO;
import com.bancatlan.atmauthorizer.exception.*;
import com.bancatlan.atmauthorizer.model.*;
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
        LOG.debug("voucherProcess - Request {}", dto.toString());
        if (dto.getAction() == null || dto.getAction().equals("")) {
            LOG.error("No action found in Request {}", dto.getAction());
            throw new ModelAtmErrorException(Constants.PARAMETER_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_FOUND_BANK_PAYMENT_SERVICE_ACTION, dto);
        }
        switch (dto.getAction().toUpperCase()) {
            case Constants.BANK_ACTION_VERIFY:
                return this.bankVerifyPayment(dto);
            case Constants.BANK_ACTION_PAYMENT:
                return this.bankConfirmPayment(dto);
            case Constants.ITM_MTI_WITHDRAW:
                return this.processWithdraw(dto);
            case Constants.ITM_MTI_REVERSE_WITHDRAW:
                return this.processCancelWithdraw(dto);
            case Constants.BANK_ACTION_GUIP:
                VoucherTransactionDTO firstCall = this.bankVerifyPayment(dto);
                return this.bankConfirmPayment(firstCall);
            default:
                LOG.error("Action not supported {}", dto.getAction());
                throw new ModelAtmErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_SUPPORTED_VOUCHER_ACTION, dto);
        }
    }

    @Override
    public VoucherTransactionDTO bankVerifyPayment(VoucherTransactionDTO dto) {
        dto = this.validateAndFitOcbRequest(dto);
        UtilComponentImpl.setSessionKey(dto.getSessionKey());
        //(1)
        Transaction txnVoucher = transaction.init(dto.getTransaction());
        dto.getTransaction().setId(txnVoucher.getId());
        dto.getTransaction().setCurrency(txnVoucher.getCurrency());
        dto.getTransaction().setCreationDate(txnVoucher.getCreationDate());
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
            String sms = String.format(template, String.format("%.2f", txnVoucher.getAmount()), pickupCode);
            bankService.sendNotification(dto.getTransaction().getPayee().getMsisdn(),"",sms,Constants.BANK_NOTIFICATION_SMS);
        }

        return dto;
    }

    @Override
    public OcbVoucherDTO verify(OcbVoucherDTO dto) {
        dto = this.validateAndFitOcbRequest(dto);
        UtilComponentImpl.setSessionKey(dto.getAuth().getSessionKey());
        //(1)
        Transaction txnVoucher = transaction.init(dto.getTransaction());
        dto.getTransaction().setId(txnVoucher.getId());
        dto.getTransaction().setCurrency(txnVoucher.getCurrency());
        dto.getTransaction().setCreationDate(txnVoucher.getCreationDate());
        dto.getTransaction().setOrderId(dto.getVoucher().getSecretCode());//todo mejorar esto
        //(2)
        transaction.authentication(dto.getTransaction());

        //(3)
        transaction.authorization(txnVoucher);

        //(4)
        transaction.verify(dto.getTransaction());
        return dto;
    }

    @Override
    public OcbVoucherDTO confirm(OcbVoucherDTO dto) {
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
        dto.getVoucher().setSecretCode(txnVoucher.getOrderId());//todo mejorar para sp05
        txnVoucher.setOrderId(null);//todo mejorar para sp05
        //this.securityValidation(req);
        //(5)
        transaction.confirm(txnVoucher);
        String pickupCode = utilComponent.getPickupCodeByCellPhoneNumber(txnVoucher.getPayee().getMsisdn());

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
            String sms = String.format(template, String.format("%.2f", txnVoucher.getAmount()), pickupCode);
            bankService.sendNotification(txnVoucher.getPayee().getMsisdn(),"",sms,Constants.BANK_NOTIFICATION_SMS);
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
        Transaction txn = transaction.getTransactionByAtmReference(dto.getTransaction().getAtmReference(), Constants.CONFIRM_TXN_STATUS);
        if (txn == null) {
            //execStatus.setErrorCode("57");
            throw new ModelNotFoundException("Txn and Voucher by this atm reference - " +
                    dto.getTransaction().getAtmReference() + " - not found or already canceled");
        }

        Voucher voucher = this.getById(txn.getVoucher().getId());
        if(voucher == null){//is missing if it's active
            throw new ModelNotFoundException(" Voucher with txn " +
                    txn.getId() + " - not found");
        }

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
    public Voucher reverseInProcess(Voucher voucher) {
        //TODO change this validation when voucher has to be withdraw in parts
        if (voucher.getAmountInitial().equals(voucher.getAmountCurrent()) && voucher.getActive()) {
            if (transaction.processBatchCancelConfirm(voucher.getTxnCreatedBy())) {
                voucher.setActive(false);
                voucher.setExpired(true);
                voucher.setUpdateDate(LocalDateTime.now());
            }
        }
        return this.update(voucher);
    }

    @Override
    public List<Voucher> getAllByOcbUser(String ocbUser) {
        return repo.findAllActiveByOcbUser(ocbUser);
    }

    @Override
    public Voucher getVoucherByCreatorTransaction(Transaction transaction) {
        return repo.getVoucherByTxnCreatedByIs(transaction);
    }

    @Override
    public void reverseExpiredVouchers() {
        long startTime = System.currentTimeMillis();
        List<Voucher> voucherList = repo.getVouchersByIsActiveAndExpirationDateIsBefore(true, LocalDateTime.now());
        if (!voucherList.isEmpty()) {
            for (Voucher vou : voucherList) {
                if (vou.getTxnCreatedBy().getCreationDate() == null) {
                    vou.getTxnCreatedBy().setCreationDate(vou.getTxnCreatedBy().getUpdateDate());
                }
                this.reverseInProcess(vou);
                LOG.info("Id => {}, initialAmount {}, currentAmount {}", vou.getId(), vou.getAmountInitial(), vou.getAmountCurrent());
            }
            LOG.info("ReverseExpiredVouchers: Finishing bash process, which it took {} ml", System.currentTimeMillis() - startTime);
        } else {
            LOG.info("ReverseExpiredVouchers: No transactions found");
        }
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
        this.validateAtmRequest(dto);
        //(1)
        dto.getTransaction().setOrderId(dto.getAtmBody().getF11());
        Transaction txn = transaction.init(dto.getTransaction());

        //(2)
        dto.getTransaction().setId(txn.getId());
        dto.getTransaction().setCurrency(txn.getCurrency());
        dto.getTransaction().setCreationDate(txn.getCreationDate());
        dto.getTransaction().setAmount(txn.getAmount());
        transaction.authentication(dto.getTransaction());

        //validate both PIN
        if (dto.getVoucher() == null || dto.getVoucher().getSecretCode() == null || dto.getVoucher().getPickupCode() == null ||
                dto.getVoucher().getSecretCode().equals("") || dto.getVoucher().getSecretCode().length() != 4 || dto.getVoucher().getPickupCode().equals("") ||
                dto.getVoucher().getPickupCode().length() != 5) {
            LOG.error("Codes in request are not properly values to be processed {}", dto.toString());
            throw new ModelAtmErrorException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_81,dto);
        }

        Voucher voucher = this.findVoucherToWithdraw(dto.getVoucher().getPickupCode(), dto.getVoucher().getSecretCode(), txn.getPayer());
        if (!this.isValidVoucherToWithDraw(voucher, dto)) {
            LOG.error("Voucher found is not valid {} ", dto);
            throw new ModelAtmErrorException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_76, dto);
        }
        /**
         * Amount validation all or nothing
         */
        if (!voucher.getAmountInitial().equals(dto.getTransaction().getAmount())) {
            LOG.error("Amount in voucher found {} did not march, err: {} ", voucher.getId(), AtmError.ERROR_13);
            throw new ModelAtmErrorException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_13, dto);
        }
        //(3)
        transaction.authorization(txn);

        //(4)
        transaction.verify(dto.getTransaction());

        txn.setAtmReference(utilComponent.generateAtmReference(dto.getAtmBody().getF11(), dto.getTransaction().getAtmReference()));
        txn.setVoucher(voucher);//Todo consider to make a transactional table
        Transaction txnPaidBy = transaction.preConfirm(txn);

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

        //TODO call MYMO update transaction
        return dto;
    }

    public VoucherTransactionDTO processCancelWithdraw(VoucherTransactionDTO dto) {
        //Atm reference
        this.validateAtmRequest(dto);

        //find confirmed txn
        Transaction txn = transaction.getTransactionByAtmReference(utilComponent.generateAtmReference(dto.getAtmBody().getF11(), dto.getTransaction().getAtmReference()), Constants.WAITING_AUTOMATIC_PROCESS_TXN_STATUS);
        if (txn == null) {
            LOG.error("processCancelWithdraw: atmReference was not found or it already cancelled {}", AtmError.ERROR_77);
            throw new ModelAtmErrorException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_77, dto);
        }

        Voucher voucher = this.getById(txn.getVoucher().getId());
        if (voucher == null || !this.isValidVoucherToReverse(voucher, dto)) {
            LOG.error("Voucher found is not valid {}", AtmError.ERROR_R1);
            throw new ModelAtmErrorException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_R1);
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
        if (voucher == null || voucher.getAmountCurrent() == 0L || utilComponent.convertAmountWithDecimals(dto.getTransaction().getAmount()) > voucher.getAmountCurrent() || !voucher.getActive()) {
            return false;
        }
        return true;
    }

    private Boolean isValidVoucherToReverse(Voucher voucher, VoucherTransactionDTO dto) {
        if (voucher == null || voucher.getAmountCurrent().equals(voucher.getAmountInitial()) || utilComponent.convertAmountWithDecimals(dto.getTransaction().getAmount()) > voucher.getAmountInitial()) {
            return false;
        }
        return true;
    }

    private VoucherTransactionDTO validateAndFitOcbRequest(VoucherTransactionDTO dto) {
        /*OCB Validations*/
        if (dto.getSessionKey() == null || dto.getSessionKey().equals("")) {
            LOG.error("Custom Exception {}", AuthorizerError.MISSING_OCB_SESSION_KEY.toString());
            throw new ModelCustomErrorException(Constants.PARAMETER_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.MISSING_OCB_SESSION_KEY);
        }

        if (dto.getTransaction() != null && dto.getTransaction().getId() != null) {
            LOG.error("Custom Exception {}", AuthorizerError.NOT_SUPPORTED_TXN_ID_BANK_PAYMENT_SERVICE_ACTION.toString());
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_SUPPORTED_TXN_ID_BANK_PAYMENT_SERVICE_ACTION);
        }

        /**
         * PAYEE
         * Validating telephone field */
        if (dto.getTransaction().getPayee() == null || dto.getTransaction().getPayee().getMsisdn() == null || dto.getTransaction().getPayee().getMsisdn().equals("")) {
            LOG.error("Custom Exception {}", AuthorizerError.MISSING_TARGET_TELEPHONE_FIELD);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_TARGET_TELEPHONE_FIELD);
        }

        /**
         * Validating format telephone of beneficiary/payee
         * Before cleaning spaces
         * */
        dto.getTransaction().getPayee().setMsisdn(dto.getTransaction().getPayee().getMsisdn().trim());

        if (!utilComponent.isValidPhoneNumber(dto.getTransaction().getPayee().getMsisdn())) {
            LOG.error("Custom Exception Payee MSISDN {}", AuthorizerError.BAD_FORMAT_TARGET_TELEPHONE);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.BAD_FORMAT_TARGET_TELEPHONE);
        }

        /**
         * Validating telephone company of beneficiary/payee */
        if (!utilComponent.isValidCommunicationCompany(dto.getTransaction().getPayee().getMsisdn())) {
            LOG.error("Custom Exception {}", AuthorizerError.CUSTOM_ERROR_NOT_SUPPORTED_COMMUNICATION_COMPANY);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.CUSTOM_ERROR_NOT_SUPPORTED_COMMUNICATION_COMPANY);
        }

        if (dto.getValidatePayeeMsisdn() == null || dto.getValidatePayeeMsisdn().equals("")) {
            LOG.error("Custom Exception {}", AuthorizerError.MISSING_CONFIRM_TARGET_TELEPHONE_FIELD.toString());
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_CONFIRM_TARGET_TELEPHONE_FIELD);
        }

        /**
         * Confirmation of telephone fields
         * Before cleaning spaces
         * */
        dto.setValidatePayeeMsisdn(dto.getValidatePayeeMsisdn().trim());
        if (!dto.getValidatePayeeMsisdn().equals(dto.getTransaction().getPayee().getMsisdn())) {
            LOG.error("Custom Exception {}", AuthorizerError.NOT_MATCH_CONFIRM_TARGET_TELEPHONE.toString());
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_MATCH_CONFIRM_TARGET_TELEPHONE);
        }

        /**
         * PAYER
         * Validating telephone field */
        if (dto.getTransaction().getPayer() == null || dto.getTransaction().getPayer().getMsisdn() == null || dto.getTransaction().getPayer().getMsisdn().equals("")) {
            LOG.error("Custom Exception {}", AuthorizerError.MISSING_PAYER_MSISDN);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_PAYER_MSISDN);
        }

        /**
         * Validating format payer telephone */
        if (!utilComponent.isValidMsisdn(dto.getTransaction().getPayer().getMsisdn())) {
            LOG.error("Custom Exception Payer MSISDN {}", AuthorizerError.BAD_FORMAT_PAYER_MSISDN);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.BAD_FORMAT_PAYER_MSISDN);
        }

        if (dto.getAmountKey() == null || dto.getAmountKey().equals("")) {
            LOG.error("Custom Exception {}", AuthorizerError.MISSING_AMOUNT_TO_TRANSFER_FIELD.toString());
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_AMOUNT_TO_TRANSFER_FIELD);
        }

        /**
         *General validation for amount */
        if (dto.getAmountKey().equals(Constants.ATM_ANOTHER_AMOUNT_KEY)) {
            if (dto.getAmount() == null || dto.getAmount().equals("")) {
                LOG.error("Custom Exception {}", AuthorizerError.MISSING_WRITTEN_AMOUNT.toString());
                throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_WRITTEN_AMOUNT);
            }
            if (!utilComponent.isValidAmountWithAtm(dto.getAmount())) {
                LOG.error("Custom Exception {}", AuthorizerError.NOT_MATCH_AMOUNT_WITH_ATM.toString());
                throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_MATCH_AMOUNT_WITH_ATM);
            }
            dto.getTransaction().setAmount(Double.parseDouble(dto.getAmount()));
        } else {
            Double amountFromMapKeys = utilComponent.getAmountFromKey(dto.getAmountKey());
            if (amountFromMapKeys == null) {
                LOG.error("Custom Exception {}", AuthorizerError.AMOUNT_KEY_DOES_NOT_EXIST.toString());
                throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.AMOUNT_KEY_DOES_NOT_EXIST);
            }
            dto.getTransaction().setAmount(utilComponent.getAmountFromKey(dto.getAmountKey()));
        }

        /**
         * validation mandatory field for secret code */
        if (dto.getVoucher() == null || dto.getVoucher().getSecretCode() == null || dto.getVoucher().getSecretCode().equals("")) {
            LOG.error("Custom Exception {}", AuthorizerError.MISSING_SECRET_CODE_FIELD.toString());
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_SECRET_CODE_FIELD);
        }

        /**
         * General validation for secret code */
        if (!utilComponent.isANumber(dto.getVoucher().getSecretCode()) || dto.getVoucher().getSecretCode().length() != 4) {
            LOG.error("Custom Exception {}", AuthorizerError.BAD_FORMAT_SECRET_CODE.toString());
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.BAD_FORMAT_SECRET_CODE);
        }

        /**
         * Validation for status account */
        /*if (dto.getTransaction().getPayerPaymentInstrument() == null || dto.getTransaction().getPayerPaymentInstrument().getStrCustomStatus() == null || !dto.getTransaction().getPayerPaymentInstrument().getStrCustomStatus().equals(Constants.BANK_CURRENCY_STATUS_ACTIVE)) {
            LOG.error("Custom Exception {}", AuthorizerError.CUSTOM_ERROR_NOT_VALID_ACCOUNT_STATUS);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.CUSTOM_ERROR_NOT_VALID_ACCOUNT_STATUS);
        }*/

        /**
         * Validation for type account
         */
        if (dto.getTransaction().getPayerPaymentInstrument() == null || dto.getTransaction().getPayerPaymentInstrument().getStrIdentifier() == null || dto.getTransaction().getPayerPaymentInstrument().getStrIdentifier().length() > 15 ) {
            LOG.error("Custom Exception {}", AuthorizerError.CUSTOM_ERROR_NOT_VALID_ACCOUNT_TYPE);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.CUSTOM_ERROR_NOT_VALID_ACCOUNT_TYPE);
        }

        /**
         * Validation for currency type account */
        if (dto.getTransaction().getCurrency() == null || dto.getTransaction().getCurrency().getCode() == null || !(dto.getTransaction().getCurrency().getCode().equals(Constants.BANK_HN_CURRENCY) || dto.getTransaction().getCurrency().getCode().equals(Constants.HN_CURRENCY))) {
            LOG.error("Custom Exception {}", AuthorizerError.NOT_SUPPORT_CURRENCY_DIFFERENT_HN);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_SUPPORT_CURRENCY_DIFFERENT_HN);
        }

        /**
         * Validation for insufficient funds */
        if (dto.getTransaction().getPayerPaymentInstrument().getStrCustomBalance() == null || !utilComponent.isValidAvailableBalance(dto.getTransaction().getPayerPaymentInstrument().getStrCustomBalance(), dto.getTransaction().getAmount())) {
            LOG.error("Custom Exception {}", AuthorizerError.CUSTOM_ERROR_INSUFFICIENT_FUNDS);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.CUSTOM_ERROR_INSUFFICIENT_FUNDS);
        }

        /*OCB Adjustments*/
        if (dto.getTransaction().getCurrency() != null && dto.getTransaction().getCurrency().getCode() != null && dto.getTransaction().getCurrency().getCode().equals(Constants.BANK_HN_CURRENCY)) {
            LOG.info("Adjusting Curency from OCB Req. LPS to HNL #{}",dto.getTransaction().getCurrency().getCode());
            dto.getTransaction().getCurrency().setCode(Constants.HN_CURRENCY);
        }

        if (dto.getTransaction().getPayer() != null) {
            dto.getTransaction().getPayer().setMsisdn(utilComponent.adjustingTelephone(dto.getTransaction().getPayer().getMsisdn()));
        }
        return dto;
    }

    private OcbVoucherDTO validateAndFitOcbRequest(OcbVoucherDTO dto) {
        /*OCB Validations*/
        if (dto.getAuth().getSessionKey() == null || dto.getAuth().getSessionKey().equals("")) {
            LOG.error("Custom Exception {}", AuthorizerError.MISSING_OCB_SESSION_KEY.toString());
            throw new ModelCustomErrorException(Constants.PARAMETER_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.MISSING_OCB_SESSION_KEY);
        }

        if (dto.getTransaction() != null && dto.getTransaction().getId() != null) {
            LOG.error("Custom Exception {}", AuthorizerError.NOT_SUPPORTED_TXN_ID_BANK_PAYMENT_SERVICE_ACTION.toString());
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_SUPPORTED_TXN_ID_BANK_PAYMENT_SERVICE_ACTION);
        }

        /**
         * PAYEE
         * Validating telephone field */
        if (dto.getTransaction().getPayee() == null || dto.getTransaction().getPayee().getMsisdn() == null || dto.getTransaction().getPayee().getMsisdn().equals("")) {
            LOG.error("Custom Exception {}", AuthorizerError.MISSING_TARGET_TELEPHONE_FIELD);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_TARGET_TELEPHONE_FIELD);
        }

        /**
         * Validating format telephone of beneficiary/payee
         * Before cleaning spaces
         * */
        dto.getTransaction().getPayee().setMsisdn(dto.getTransaction().getPayee().getMsisdn().trim());

        if (!utilComponent.isValidPhoneNumber(dto.getTransaction().getPayee().getMsisdn())) {
            LOG.error("Custom Exception Payee MSISDN {}", AuthorizerError.BAD_FORMAT_TARGET_TELEPHONE);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.BAD_FORMAT_TARGET_TELEPHONE);
        }

        /**
         * Validating telephone company of beneficiary/payee */
        if (!utilComponent.isValidCommunicationCompany(dto.getTransaction().getPayee().getMsisdn())) {
            LOG.error("Custom Exception {}", AuthorizerError.CUSTOM_ERROR_NOT_SUPPORTED_COMMUNICATION_COMPANY);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.CUSTOM_ERROR_NOT_SUPPORTED_COMMUNICATION_COMPANY);
        }

        /*if (dto.getValidatePayeeMsisdn() == null || dto.getValidatePayeeMsisdn().equals("")) {
            LOG.error("Custom Exception {}", AuthorizerError.MISSING_CONFIRM_TARGET_TELEPHONE_FIELD.toString());
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_CONFIRM_TARGET_TELEPHONE_FIELD);
        }*/

        /**
         * Confirmation of telephone fields
         * Before cleaning spaces
         * */
        /*dto.setValidatePayeeMsisdn(dto.getValidatePayeeMsisdn().trim());
        if (!dto.getValidatePayeeMsisdn().equals(dto.getTransaction().getPayee().getMsisdn())) {
            LOG.error("Custom Exception {}", AuthorizerError.NOT_MATCH_CONFIRM_TARGET_TELEPHONE.toString());
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_MATCH_CONFIRM_TARGET_TELEPHONE);
        }*/

        /**
         * PAYER
         * Validating telephone field */
        if (dto.getTransaction().getPayer() == null || dto.getTransaction().getPayer().getMsisdn() == null || dto.getTransaction().getPayer().getMsisdn().equals("")) {
            LOG.error("Custom Exception {}", AuthorizerError.MISSING_PAYER_MSISDN);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_PAYER_MSISDN);
        }

        /**
         * Validating format payer telephone */
        if (!utilComponent.isValidMsisdn(dto.getTransaction().getPayer().getMsisdn())) {
            LOG.error("Custom Exception Payer MSISDN {}", AuthorizerError.BAD_FORMAT_PAYER_MSISDN);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.BAD_FORMAT_PAYER_MSISDN);
        }

        /*if (dto.getAmountKey() == null || dto.getAmountKey().equals("")) {
            LOG.error("Custom Exception {}", AuthorizerError.MISSING_AMOUNT_TO_TRANSFER_FIELD.toString());
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_AMOUNT_TO_TRANSFER_FIELD);
        }*/

        /**
         *General validation for amount */
        /*if (dto.getAmountKey().equals(Constants.ATM_ANOTHER_AMOUNT_KEY)) {
            if (dto.getAmount() == null || dto.getAmount().equals("")) {
                LOG.error("Custom Exception {}", AuthorizerError.MISSING_WRITTEN_AMOUNT.toString());
                throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_WRITTEN_AMOUNT);
            }
            if (!utilComponent.isValidAmountWithAtm(dto.getAmount())) {
                LOG.error("Custom Exception {}", AuthorizerError.NOT_MATCH_AMOUNT_WITH_ATM.toString());
                throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_MATCH_AMOUNT_WITH_ATM);
            }
            dto.getTransaction().setAmount(Double.parseDouble(dto.getAmount()));
        } else {
            Double amountFromMapKeys = utilComponent.getAmountFromKey(dto.getAmountKey());
            if (amountFromMapKeys == null) {
                LOG.error("Custom Exception {}", AuthorizerError.AMOUNT_KEY_DOES_NOT_EXIST.toString());
                throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.AMOUNT_KEY_DOES_NOT_EXIST);
            }
            dto.getTransaction().setAmount(utilComponent.getAmountFromKey(dto.getAmountKey()));
        }*/

        /**
         * validation mandatory field for secret code */
        if (dto.getVoucher() == null || dto.getVoucher().getSecretCode() == null || dto.getVoucher().getSecretCode().equals("")) {
            LOG.error("Custom Exception {}", AuthorizerError.MISSING_SECRET_CODE_FIELD.toString());
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_SECRET_CODE_FIELD);
        }

        /**
         * General validation for secret code */
        if (!utilComponent.isANumber(dto.getVoucher().getSecretCode()) || dto.getVoucher().getSecretCode().length() != 4) {
            LOG.error("Custom Exception {}", AuthorizerError.BAD_FORMAT_SECRET_CODE.toString());
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.BAD_FORMAT_SECRET_CODE);
        }

        /**
         * Validation for status account */
        /*if (dto.getTransaction().getPayerPaymentInstrument() == null || dto.getTransaction().getPayerPaymentInstrument().getStrCustomStatus() == null || !dto.getTransaction().getPayerPaymentInstrument().getStrCustomStatus().equals(Constants.BANK_CURRENCY_STATUS_ACTIVE)) {
            LOG.error("Custom Exception {}", AuthorizerError.CUSTOM_ERROR_NOT_VALID_ACCOUNT_STATUS);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.CUSTOM_ERROR_NOT_VALID_ACCOUNT_STATUS);
        }*/

        /**
         * Validation for type account
         */
        if (dto.getTransaction().getPayerPaymentInstrument() == null || dto.getTransaction().getPayerPaymentInstrument().getStrIdentifier() == null || dto.getTransaction().getPayerPaymentInstrument().getStrIdentifier().length() > 15 ) {
            LOG.error("Custom Exception {}", AuthorizerError.CUSTOM_ERROR_NOT_VALID_ACCOUNT_TYPE);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.CUSTOM_ERROR_NOT_VALID_ACCOUNT_TYPE);
        }

        /**
         * Validation for currency type account */
        if (dto.getTransaction().getCurrency() == null || dto.getTransaction().getCurrency().getCode() == null || !(dto.getTransaction().getCurrency().getCode().equals(Constants.BANK_HN_CURRENCY) || dto.getTransaction().getCurrency().getCode().equals(Constants.HN_CURRENCY))) {
            LOG.error("Custom Exception {}", AuthorizerError.NOT_SUPPORT_CURRENCY_DIFFERENT_HN);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_SUPPORT_CURRENCY_DIFFERENT_HN);
        }

        /**
         * Validation for insufficient funds */
        if (dto.getTransaction().getPayerPaymentInstrument().getStrCustomBalance() == null || !utilComponent.isValidAvailableBalance(dto.getTransaction().getPayerPaymentInstrument().getStrCustomBalance(), dto.getTransaction().getAmount())) {
            LOG.error("Custom Exception {}", AuthorizerError.CUSTOM_ERROR_INSUFFICIENT_FUNDS);
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.CUSTOM_ERROR_INSUFFICIENT_FUNDS);
        }

        /*OCB Adjustments*/
        if (dto.getTransaction().getCurrency() != null && dto.getTransaction().getCurrency().getCode() != null && dto.getTransaction().getCurrency().getCode().equals(Constants.BANK_HN_CURRENCY)) {
            LOG.info("Adjusting Curency from OCB Req. LPS to HNL #{}",dto.getTransaction().getCurrency().getCode());
            dto.getTransaction().getCurrency().setCode(Constants.HN_CURRENCY);
        }

        if (dto.getTransaction().getPayer() != null) {
            dto.getTransaction().getPayer().setMsisdn(utilComponent.adjustingTelephone(dto.getTransaction().getPayer().getMsisdn()));
        }
        return dto;
    }

    private void validateAtmRequest(VoucherTransactionDTO dto) {
        if (dto.getTransaction() == null || dto.getTransaction().getAtmReference() == null || dto.getTransaction().getAtmReference().equals("")) {
            LOG.error("AtmReference in request is not defined {}", AtmError.ERROR_76);
            throw new ModelAtmErrorException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_76, dto);
        }

        if (dto.getAction().equals(Constants.ITM_MTI_WITHDRAW)) {
            Transaction txn = transaction.getTransactionByAtmReference(utilComponent.generateAtmReference(dto.getAtmBody().getF11(), dto.getTransaction().getAtmReference()), Constants.CONFIRM_TXN_STATUS);
            if (txn != null) {
                LOG.error("txn already exist with atmReference in request {}", AtmError.ERROR_94);
                throw new ModelAtmErrorException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_94, dto);
            }
        }

        //Init validations
        if (dto.getTransaction().getUseCase() == null || dto.getTransaction().getUseCase().getId() == null) {
            LOG.error("Use case null {}", AtmError.ERROR_12);
            throw new ModelAtmErrorException(Constants.PARAMETER_NOT_FOUND_MESSAGE_ERROR, AtmError.ERROR_12, dto);
        }

        if (!dto.getTransaction().getUseCase().getId().equals(Constants.WITHDRAW_VOUCHER_USE_CASE)) {
            LOG.error("Use case is wrong {}", AtmError.ERROR_12);
            throw new ModelAtmErrorException(Constants.PARAMETER_NOT_FOUND_MESSAGE_ERROR, AtmError.ERROR_12, dto);
        }

        if (dto.getTransaction().getAmount() == null) {
            LOG.error("Amount in request is null {}", AtmError.ERROR_13);
            throw new ModelAtmErrorException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_13, dto);
        }

        Double amountFromAtm = utilComponent.convertAmountWithDecimals(dto.getTransaction().getAmount());
        if (!utilComponent.isValidAmountWithAtm(amountFromAtm.toString())) {
            LOG.error("Custom Exception {}", AtmError.ERROR_13.toString());
            throw new ModelAtmErrorException(Constants.CUSTOM_MESSAGE_ERROR, AtmError.ERROR_13, dto);
        }

        //Authentication validations
        if (dto.getTransaction().getPayer() == null || dto.getTransaction().getPayer().getMsisdn() == null || dto.getTransaction().getPayer().getMsisdn().equals("") ||
                !utilComponent.isValidPhoneNumber(dto.getTransaction().getPayer().getMsisdn())) {
            LOG.error("NumberPhone does not come properly in request {}", AtmError.ERROR_14);
            throw new ModelAtmErrorException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_14, dto);
        }

        Customer cst = customer.getByMsisdn(dto.getTransaction().getPayer().getMsisdn());
        if (cst == null) {
            LOG.error("Customer with numberPhone specified in request not fount {}", AtmError.ERROR_25);
            throw new ModelAtmErrorException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_25, dto);
        }

        Customer userATM = customer.getById(Constants.ATM_USER_ID);
        if (userATM == null || !userATM.getName().trim().equals(Constants.ATM_USER_STR)) {
            LOG.error("ATM user not fount {}", AtmError.ERROR_03);
            throw new ModelAtmErrorException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_03, dto);
        }
        //Authorization validations
        //verify validations
        //confirm validations
    }

    private void securityValidation(VoucherTransactionDTO dto){
        //Todo validate with session if the current id txn belong to the session linked with previous id txn generated
    }
}
