package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.component.IUtilComponent;
import com.bancatlan.atmauthorizer.dto.OcbVoucherDTO;
import com.bancatlan.atmauthorizer.exception.AuthorizerError;
import com.bancatlan.atmauthorizer.exception.ModelCustomErrorException;
import com.bancatlan.atmauthorizer.exception.ModelNotFoundException;
import com.bancatlan.atmauthorizer.model.Transaction;
import com.bancatlan.atmauthorizer.model.Voucher;
import com.bancatlan.atmauthorizer.service.IBankService;
import com.bancatlan.atmauthorizer.service.ICardlessWithdrawal;
import com.bancatlan.atmauthorizer.service.ITransactionService;
import com.bancatlan.atmauthorizer.service.IVoucherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SP05CardlessWithdrawal implements ICardlessWithdrawal {
    Logger LOG = LoggerFactory.getLogger(SP05CardlessWithdrawal.class);

    @Autowired
    ITransactionService transactionService;

    @Autowired
    IVoucherService voucherService;

    @Autowired
    IBankService bankService;

    @Autowired
    IUtilComponent utilComponent;

    @Override
    public OcbVoucherDTO verify(OcbVoucherDTO dto) {
        LOG.debug("SP05 - Verify Request {}", dto.toString());
        dto = this.validateAndFitOcbRequest(dto);
        //utilComponent.setSessionKey(dto.getAuth().getSessionKey());//It needs a better security implementation about
        //(1)
        Transaction txnVoucher = transactionService.init(dto.getTransaction());
        dto.getTransaction().setId(txnVoucher.getId());
        dto.getTransaction().setCurrency(txnVoucher.getCurrency());
        dto.getTransaction().setCreationDate(txnVoucher.getCreationDate());
        dto.getTransaction().setOrderId(dto.getVoucher().getSecretCode());//todo mejorar esto
        //(2)
        transactionService.authentication(dto.getTransaction());

        //(3)
        transactionService.authorization(txnVoucher);

        //(4)
        transactionService.verify(dto.getTransaction());
        return dto;
    }

    @Override
    public OcbVoucherDTO confirm(OcbVoucherDTO dto) {
        LOG.debug("SP05 - Confirm Request {}", dto.toString());
        if(dto.getTransaction() == null || dto.getTransaction().getId() == null){
            throw new ModelNotFoundException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.MISSING_TXN_ON_REQUEST);
        }

        Transaction txnVoucher = transactionService.getById(dto.getTransaction().getId());
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
        transactionService.confirm(txnVoucher);
        String pickupCode = utilComponent.getPickupCodeByCellPhoneNumber(txnVoucher.getPayee().getMsisdn());
        String secretCode = utilComponent.getSecretCodeByCellPhoneNumber(txnVoucher.getPayee().getId().toString());
        String encryptedCode = utilComponent.encryptCode(txnVoucher.getPayee().getMsisdn() + "|" + secretCode +"|" + pickupCode + "|" + txnVoucher.getAmount());
        LOG.info("DecryptedCode {}", utilComponent.decryptCode(encryptedCode));

        dto.getVoucher().setSecretCode(secretCode);
        dto.getVoucher().setPickupCode(pickupCode);
        dto.getVoucher().setTxnCreatedBy(txnVoucher);
        dto.getVoucher().setAmountInitial(txnVoucher.getAmount());
        dto.getVoucher().setAmountCurrent(txnVoucher.getAmount());
        dto.getVoucher().setEncryptedCode(encryptedCode);
        dto.getVoucher().setActive(true);
        dto.getVoucher().setExpired(false);
        dto.getVoucher().setCanceled(false);
        dto.getVoucher().setDeleted(false);
        Voucher voucherResult = voucherService.create(dto.getVoucher());

        dto.setTransaction(txnVoucher);
        dto.setVoucher(voucherResult);

        if(voucherResult != null){
            String template = Constants.TEMPLATE_NOTIFICATION_SMS;//Todo get template from DB
            String sms = String.format(template, String.format("%.2f", txnVoucher.getAmount()), pickupCode);
            bankService.sendNotification(txnVoucher.getPayee().getMsisdn(),"",sms,Constants.BANK_NOTIFICATION_SMS);
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

        if (dto.getTransaction().getAmount() == null || dto.getTransaction().getAmount().equals("")) {
            LOG.error("Custom Exception {}", AuthorizerError.MISSING_WRITTEN_AMOUNT.toString());
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_WRITTEN_AMOUNT);
        }

        if (!utilComponent.isValidAmountWithAtm(dto.getTransaction().getAmount().toString())) {
            LOG.error("Custom Exception {}", AuthorizerError.NOT_MATCH_AMOUNT_WITH_ATM.toString());
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_MATCH_AMOUNT_WITH_ATM);
        }
        dto.getTransaction().setAmount(dto.getTransaction().getAmount());

        /**
         * validation mandatory field for secret code */
        if (dto.getVoucher() == null || dto.getVoucher().getSecretCode() == null || dto.getVoucher().getSecretCode().equals("")) {
            LOG.error("Custom Exception {}", AuthorizerError.MISSING_SECRET_CODE_FIELD.toString());
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.MISSING_SECRET_CODE_FIELD);
        }

        /**
         * General validation for secret code
         * Exclude QR txn
         * */
        if (!dto.getTransaction().getUseCase().getId().equals(Constants.INT_VOUCHER_USE_CASE_QR) && (!utilComponent.isANumber(dto.getVoucher().getSecretCode()) || dto.getVoucher().getSecretCode().length() != 4)) {
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
}
