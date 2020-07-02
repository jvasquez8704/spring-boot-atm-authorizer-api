package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.component.IUtilComponent;
import com.bancatlan.atmauthorizer.exception.*;
import com.bancatlan.atmauthorizer.model.*;
import com.bancatlan.atmauthorizer.repo.ITransactionRepo;
import com.bancatlan.atmauthorizer.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements ITransactionService {
    Logger LOG = LoggerFactory.getLogger(TransactionServiceImpl.class);
    @Autowired
    private ITransactionRepo repo;

    @Autowired
    private ICustomerService customer;

    @Autowired
    private ITxnStatusService status;

    @Autowired
    private ILimitService limitService;

    @Autowired
    private IPaymentInstrumentService paymentInstrumentService;

    @Autowired
    private  IBankService bankService;

    @Autowired
    private  ICurrencyService currencyService;

    @Autowired
    private IUtilComponent utilComponent;

    @Autowired
    private IVoucherService voucherService;

    @Override
    public Transaction create(Transaction txn) {
        txn.setCreationDate(LocalDateTime.now());
        return repo.save(txn);
    }

    @Override
    public Transaction update(Transaction txn) {
        txn.setUpdateDate(LocalDateTime.now());
        return repo.save(txn);
    }

    @Override
    public List<Transaction> getAll() {
        return repo.findAll();
    }

    @Override
    public Transaction getById(Long id) {
        Optional<Transaction> txn = repo.findById(id);
        return txn.isPresent() ? txn.get() : null;
    }

    @Override
    public Boolean delete(Long id) {
        Boolean retVal = true;
        try {
            repo.deleteById(id);
        } catch (Exception ex) {
            retVal = false;
            System.out.println(ex);
        }
        return retVal;
    }
    /**
     * start txn
     * @param txn
     * @return initialized txn
     */
    @Override
    public Transaction init(Transaction txn) {
        if (txn.getUseCase() == null || txn.getUseCase().getId() == null) {
            throw new ModelCustomErrorException(Constants.PARAMETER_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_FOUND_USE_CASE);
        }
        return this.processInit(txn);
    }
    /**
     * access payer and payee
     * */
    @Override
    public Transaction authentication(Transaction txn) {
        this.processAuthentication(txn);
        txn.setTxnStatus(status.getById(Constants.AUTHENTICATED_TXN_STATUS));
        return this.update(txn);
    }

    /**
     * payer and payee privileges
     */
    @Override
    public Transaction authorization(Transaction txn) {
        this.processAuthorization(txn);
        txn.setTxnStatus(status.getById(Constants.PRE_AUTHORIZED_TXN_STATUS));
        return this.update(txn);
    }

    /**
     * txn status, limits, fees
     */
    @Override
    public Transaction verify(Transaction txn) {
        this.processVerification(txn);
        txn.setTxnStatus(status.getById(Constants.AUTHORIZED_TXN_STATUS));
        return this.update(txn);
    }

    @Override
    public Transaction preConfirm(Transaction txn) {
        txn.setTxnStatus(status.getById(Constants.WAITING_AUTOMATIC_PROCESS_TXN_STATUS));
        LOG.info("GET TXN IN PRE-CONFIRM {}", txn.getId());
        return this.update(txn);
    }

    @Override
    public Transaction confirm(Transaction txn) {
        Transaction txnRet = this.processConfirm(txn);
        txnRet.setTxnStatus(status.getById(Constants.CONFIRM_TXN_STATUS));
        LOG.info("GET TXN IN CONFIRM {}",txn);
        return this.update(txnRet);
    }

    @Override
    public Transaction cancelConfirm(Transaction txn) {
        Transaction retTxn = this.processCancelConfirm(txn);
        txn.setTxnStatus(status.getById(Constants.CANCEL_CONFIRM_TXN_STATUS));
        return this.update(retTxn);
    }

    @Override
    public Boolean processBatchCancelConfirm(Transaction txn) {
        long initTimeProcess = System.currentTimeMillis();
        //Defrost founds user
        LOG.info("Current txn {}", txn.getId());
        //If this txn is not confirm or this txn is not VOUCHER_USE_CASE do not do nothing
        if (!txn.getTxnStatus().getId().equals(Constants.CONFIRM_TXN_STATUS) || !txn.getUseCase().getId().equals(Constants.VOUCHER_USE_CASE)) {
            LOG.info("Rejected txn in process txn status {}, txn use case {}", txn.getTxnStatus().getId(), txn.getUseCase().getId());
            return false;
        }

        String customComment = Constants.STR_DEFROST_SERVICE_NAME + Constants.STR_DASH_SEPARATOR + txn.getId() + Constants.STR_DASH_SEPARATOR + txn.getPayerPaymentInstrument().getStrIdentifier() + Constants.STR_DASH_SEPARATOR + txn.getPayee().getMsisdn();
        String coreRef = bankService.freezeFoundsProcess(txn.getPayerPaymentInstrument().getStrIdentifier(), txn.getAmount(), txn.getId(), Constants.BANK_ACTION_DEFROST, txn.getPayer().getUsername(), customComment);
        txn.setCoreReference(coreRef);

        LOG.info("Time process: {} ms, days {}", System.currentTimeMillis() - initTimeProcess, Duration.between(txn.getCreationDate(), LocalDateTime.now()).toDays());
        //Cancel Voucher
        if (!coreRef.equals(Constants.STR_CUSTOM_ERR) && !coreRef.equals(Constants.STR_EXCEPTION_ERR) && !coreRef.equals(Constants.STR_DASH_SEPARATOR) && !coreRef.equals(Constants.STR_ZERO)) {
            txn.setExpirationDate(LocalDateTime.now());
            txn.setTxnStatus(status.getById(Constants.CANCEL_CONFIRM_TXN_STATUS));
            this.update(txn);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public Boolean verifyTxnParticipants(Transaction txn) {
        return customer.checkCustomerStatus(txn.getPayer()) && customer.checkCustomerStatus(txn.getPayee()) &&
                customer.checkCustomerPrivileges(txn.getPayer()) && customer.checkCustomerPrivileges(txn.getPayee()) &&
                limitService.verifyTxnParticipantsLimits(txn);
    }

    @Override
    public Boolean verifyTxnLimits(Transaction txn) {
        return limitService.verifyTxnLimits(txn);
    }

    @Override
    public List<Transaction> getTransactionByAtmReference(String atmReference) {
        return repo.getTransactionByAtmReference(atmReference);
    }

    @Override
    public Transaction getTransactionByAtmReference(String atmReference, Long txnStatus) {
        return repo.getTransactionByAtmReferenceAndTxnStatus(atmReference, status.getById(txnStatus));
    }

    @Override
    public List<Transaction> getTransactionsByCustomerAndRangeTime(Customer customer, UseCase useCase, LocalDateTime startDateTime, LocalDateTime endDateTime, Boolean isDebit) {
        List<Transaction> txnList = null;
        if (isDebit == null) {
            txnList = repo.getTransactionsByPayerOrPayeeAndUseCaseAndCreationDateBetween(customer, customer, useCase, startDateTime, endDateTime);
        } else if (isDebit) {
            txnList = repo.getTransactionsByPayerAndUseCaseAndCreationDateBetween(customer, useCase, startDateTime, endDateTime);
        } else {
            txnList = repo.getTransactionsByPayeeAndUseCaseAndCreationDateBetween(customer, useCase, startDateTime, endDateTime);
        }
        return txnList;
    }

    @Override
    public void executeAllConfirmedWithDrawls() {
        long startTime = System.currentTimeMillis();
        UseCase useCase = new UseCase();
        useCase.setId(new Long(800));
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.setId(new Long(25));
        List<Transaction> transactionList = repo.getTransactionsByUseCaseAndTxnStatus(useCase, txnStatus);

        if (!transactionList.isEmpty()) {
            for (Transaction txn : transactionList) {
                long startTimeProcess = System.currentTimeMillis();
                this.processBatchConfirm(txn);
                LOG.info("Id => {}, Amount {},  Paid Voucher {} , time process: {} ms.", txn.getId(), txn.getAmount(), txn.getVoucher().getId(), System.currentTimeMillis() - startTimeProcess);
            }
            LOG.info("ExecuteAllConfirmedWithDrawls: Finishing bash process, which it took {} ms", System.currentTimeMillis() - startTime);
        } else {
            LOG.info("ExecuteAllConfirmedWithDrawls: No transactions found");
        }
    }

    @Override
    public void reverseExpiredTransactions() {
        long initTime = System.currentTimeMillis();
        UseCase useCase = new UseCase();
        useCase.setId(new Long(174));
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.setId(new Long(30));
        List<Transaction> txnList = repo.getTransactionsByUseCaseAndTxnStatus(useCase, txnStatus);

        if (!txnList.isEmpty()) {
            for (Transaction txn : txnList) {
                long initTimeProcess = System.currentTimeMillis();
                if (txn.getCreationDate() == null) {
                    txn.setCreationDate(txn.getUpdateDate());
                }
                if (Duration.between(txn.getCreationDate(), LocalDateTime.now()).toDays() >= 3) {
                    this.processBatchCancelConfirm(txn);
                }
                LOG.info("Id => {}, Amount {},  Paid Voucher {} , time process: {} ms, days {}", txn.getId(), txn.getAmount(), txn.getVoucher(), System.currentTimeMillis() - initTimeProcess, Duration.between(txn.getCreationDate(), LocalDateTime.now()).toDays());
            }
            LOG.info("ReverseExpiredVouchers: Finishing bash process, which it took {} ml", System.currentTimeMillis() - initTime);
        } else {
            LOG.info("ReverseExpiredVouchers: No transactions found");
        }
    }

    private Transaction processInit(Transaction txn){
        Transaction initTxn = new Transaction();
        switch (txn.getUseCase().getId().intValue()){
            case Constants.INT_VOUCHER_USE_CASE:
                initTxn.setAmount(txn.getAmount());
                break;
            case Constants.INT_WITHDRAW_VOUCHER_USE_CASE:
                //validate amount
                if (txn.getAmount() == null) {
                    LOG.error("Amount in request is not a properly value to be processed", txn);
                    throw new ModelNotFoundException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_13);
                }
                Double amountFromAtm = utilComponent.convertAmountWithDecimals(txn.getAmount());
                if (!utilComponent.isValidAmountWithAtm(amountFromAtm.toString())) {
                    LOG.error("Custom Exception {}", AtmError.ERROR_13.toString());
                    throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AtmError.ERROR_13);
                }
                initTxn.setAmount(amountFromAtm);
                break;
            default:
                LOG.error("Custom Exception {}", AuthorizerError.NOT_SUPPORT_USE_CASE.toString());
                throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.NOT_SUPPORT_USE_CASE);
        }
        //set currency
        if (txn.getCurrency() == null || txn.getCurrency().getCode() == null || txn.getCurrency().getCode().equals("")) {
            LOG.error("Custom Exception {}", AuthorizerError.NOT_FOUND_CURRENCY_IN_REQ);
            throw new ModelCustomErrorException(Constants.PARAMETER_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_FOUND_CURRENCY_IN_REQ);
        }
        Currency currency = currencyService.getCurrencyByCode(txn.getCurrency().getCode());
        if (currency == null) {
            LOG.error("Custom Exception {}", AuthorizerError.NOT_FOUND_CURRENCY);
            throw new ModelCustomErrorException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_FOUND_CURRENCY);
        }
        initTxn.setCurrency(currency);
        initTxn.setTxnStatus(status.getById(Constants.INITIAL_TXN_STATUS));
        initTxn.setUseCase(txn.getUseCase());
        return this.create(initTxn);
    }

    /**
     * 1.check if participants in txn exist in db
     * 2.check if identification and identity exist for each one
     * 3 check PI for each one then (update PI adding User to PI)
     * 4. add user creator system admin user
     * @param txn
     * @return
     */
    private Transaction processAuthentication(Transaction txn) {
        switch (txn.getUseCase().getId().intValue()) {
            case Constants.INT_VOUCHER_USE_CASE:
                //CHECK PAYER USER AND PAYEE NO CLIENT
                if (txn.getPayer() == null || txn.getPayer().getUsername() == null || txn.getPayer().getUsername().equals("")) {
                    LOG.error("processAuthentication: {}", AuthorizerError.MISSING_OCB_USER);
                    throw new ModelNotFoundException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.MISSING_OCB_USER);
                }

                if (txn.getPayee() == null || txn.getPayee().getMsisdn() == null || txn.getPayee().getMsisdn().equals("")) {
                    LOG.error("processAuthentication: {}", AuthorizerError.MISSING_PAYER_PI);
                    throw new ModelNotFoundException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.MISSING_PAYER_PI);
                }
                /**
                 * business rules, if participants not exits, create them as a pre-registered customer
                 * 1. If found payer by Username => GO
                 * 2. check if request comes the payer telephone, validate if customer exist
                 */
                Customer payer = customer.getByUsername(txn.getPayer().getUsername());
                if (payer == null) {
                    txn.getPayer().setCountry(null);//hack
                    //check if request comes the payer telephone, validate if customer exist
                    if (txn.getPayer().getMsisdn() != null && !txn.getPayer().getMsisdn().equals("")) {
                        payer = customer.checkIfCustomerExist(txn.getPayer().getMsisdn());
                        if (payer == null) {
                            //Set creator user
                            payer = customer.register(txn.getPayer());
                        } else {
                            //If customer found by telephone update just username
                            payer.setUsername(txn.getPayer().getUsername());
                            payer = customer.update(payer);
                        }
                    } else {
                        payer = customer.register(txn.getPayer());
                    }
                }

                /**
                 * 1. If found customer by Username => GO
                 * 2. check if request comes the payer telephone, validate if customer exist
                 */
                Customer payee = customer.checkIfCustomerExist(txn.getPayee().getMsisdn());
                if (payee == null) {
                    txn.getPayee().setCountry(null);//hack
                    //check if this client exist as payee => check if username is different of null
                    if (txn.getPayee().getUsername() == null || txn.getPayee().getUsername().equals("")) {
                        payee = customer.register(txn.getPayee());
                    } else {//this almost never happens
                        payee = customer.getByUsername(txn.getPayee().getUsername());
                        if (payee == null) {
                            //Set creator user
                            payee = customer.register(txn.getPayee());
                        } else {
                            //update just username
                            payee.setUsername(txn.getPayee().getMsisdn());
                            payee = customer.update(payee);
                        }
                    }
                }

                //CHECK PAYER PI
                if (txn.getPayerPaymentInstrument() == null || txn.getPayerPaymentInstrument().getStrIdentifier() == null || txn.getPayerPaymentInstrument().getStrIdentifier().equals("")) {
                    LOG.error("processAuthentication: {}", AuthorizerError.MISSING_PAYER_PI);
                    throw new ModelNotFoundException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.MISSING_PAYER_PI);
                }

                //GET BANK ACCOUNTS BY OCB_USER
                boolean isFoundPI = false;
                //Boolean isFoundPI = true;
                /*List<PaymentInstrument> accountsUser = bankService.getBankAccountsByUserId(txn.getPayer().getUsername());
                if(!accountsUser.isEmpty()) {
                    for (PaymentInstrument pi : accountsUser) {
                        if (pi.getStrIdentifier().trim().equals(txn.getPayerPaymentInstrument().getStrIdentifier().trim())) {
                            isFoundPI = true;
                        }
                    }
                }

                if(!isFoundPI){
                    LOG.error(AuthorizerError.MISSING_ACCOUNT_FROM_BANK.toString() + txn.toString());
                    throw new ModelNotFoundException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.MISSING_ACCOUNT_FROM_BANK);
                }*/

                PaymentInstrument payerPi = paymentInstrumentService.getPaymentInstrumentByStrIdentifier(txn.getPayerPaymentInstrument().getStrIdentifier());
                //PaymentInstrument payerPi = paymentInstrumentService.getPaymentInstrumentByCustomerAndStrIdentifier(payer, txn.getPayerPaymentInstrument().getStrIdentifier());
                if (payerPi == null) {
                    txn.getPayerPaymentInstrument().setCustomer(payer);
                    payerPi = paymentInstrumentService.create(txn.getPayerPaymentInstrument());
                }

                txn.setPayer(payer);
                txn.setPayee(payee);
                txn.setPayerPaymentInstrument(payerPi);
                break;
            case Constants.INT_WITHDRAW_VOUCHER_USE_CASE:
                //find customer
                if (txn.getPayer() == null || txn.getPayer().getMsisdn() == null || txn.getPayer().getMsisdn().equals("") ||
                        !utilComponent.isValidPhoneNumber(txn.getPayer().getMsisdn())) {
                    LOG.error("processAuthentication: NumberPhone does not come properly in request, error: {}", AtmError.ERROR_14);
                    throw new ModelNotFoundException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_14);
                }

                Customer cst = customer.getByMsisdn(txn.getPayer().getMsisdn());
                if (cst == null) {
                    LOG.error("processAuthentication: Customer with numberPhone specified in request not fount, error: {}", AtmError.ERROR_25);
                    throw new ModelNotFoundException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_25);
                }

                Customer userATM = customer.getById(Constants.ATM_USER_ID);
                if (userATM == null || !userATM.getName().trim().equals(Constants.ATM_USER_STR)) {
                    LOG.error("processAuthentication: ATM User not configured {}", AtmError.ERROR_03);
                    throw new ModelAtmErrorException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_03);
                }
                //TODO Improve assigment to pi for both participants
                PaymentInstrument accountATMBASA = paymentInstrumentService.getById(Constants.PI_ATM_USER_ID);
                if (accountATMBASA == null) {
                    LOG.error("processAuthentication: account ATM User not configured {}", AtmError.ERROR_N3);
                    throw new ModelAtmErrorException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_N3);
                }

                txn.setPayer(cst);
                txn.setPayee(userATM);
                txn.setPayeePaymentInstrument(accountATMBASA);
                break;
            default:

        }
        return this.update(txn);
    }

    private Transaction processAuthorization(Transaction txn){
        switch (txn.getUseCase().getId().intValue()){
            case Constants.INT_VOUCHER_USE_CASE:
                //Todo verifyBasaUser , here code things related with permissions, privileges, user roles etc...
                //Todo account
                break;
            case Constants.INT_WITHDRAW_VOUCHER_USE_CASE:

                break;
            default:

        }
        return txn;
    }

    private Transaction processVerification(Transaction txn){
        switch (txn.getUseCase().getId().intValue()){
            case Constants.INT_VOUCHER_USE_CASE:
                if(!this.verifyTxnParticipants(txn) && !this.verifyTxnLimits(txn)){
                    throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.ERROR_ON_VERIFY);
                }
                break;
            case Constants.INT_WITHDRAW_VOUCHER_USE_CASE:
                txn.setStrAuthorizationCode(utilComponent.createAuthorizationCode(Constants.LENGTH_AUTH_CODE));
                break;
            default:

        }
        return txn;
    }

    private Transaction processConfirm(Transaction txn) {
        switch (txn.getUseCase().getId().intValue()) {
            case Constants.INT_VOUCHER_USE_CASE:
                //Freeze founds for ocb user
                PaymentInstrument cstBank = paymentInstrumentService.getById(txn.getPayer().getId());
                LOG.info(" {} account number of customer {}",cstBank, txn.getPayer().getId());
                //Account Payer, Amount, comment
                String _customComment = Constants.STR_ID_RETIRO_SIN_TARGETA + Constants.STR_DASH_SEPARATOR + txn.getPayee().getMsisdn();
                String _coreRef = bankService.freezeFounds(txn.getPayerPaymentInstrument().getStrIdentifier(), txn.getAmount(), txn.getId(), Constants.BANK_ACTION_FREEZE, txn.getPayer().getUsername(), _customComment);
                txn.setCoreReference(_coreRef);
                //Update balance payer
                //Double newBalance = accountATMBASA.getBalance() + txn.getAmount();
                //accountATMBASA.setBalance(newBalance);
                //paymentInstrumentService.update(accountATMBASA);
                break;
            case Constants.INT_WITHDRAW_VOUCHER_USE_CASE:
                Transaction creatorTxn = txn.getVoucher().getTxnCreatedBy();
                PaymentInstrument payerPI = creatorTxn.getPayerPaymentInstrument();
                PaymentInstrument accountATMBASA = paymentInstrumentService.getById(Constants.PI_ATM_USER_ID);
                Customer payee = creatorTxn.getPayee();

                //Account Payer, Account Payee, Amount = cta contable qa => 750099900684 RT-USECASE-CTA_ORIGEN-MSISDN_DESTINO
                //this is a bank transfer with a implicit defrost
                String customComment = Constants.STR_ID_RETIRO_SIN_TARGETA + Constants.STR_DASH_SEPARATOR + txn.getId() + Constants.STR_DASH_SEPARATOR + txn.getUseCase().getId() + Constants.STR_DASH_SEPARATOR + payerPI.getStrIdentifier() + Constants.STR_DASH_SEPARATOR + payee.getMsisdn();
                String coreRef = bankService.transferMoney(payerPI.getStrIdentifier(), accountATMBASA.getStrIdentifier(), txn.getAmount(), creatorTxn.getId(), Constants.BANK_ACTION_DEFROST, customComment);
                txn.setCoreReference(coreRef);
                //Update balance payee
                Double newBalance = accountATMBASA.getBalance() + txn.getAmount();
                accountATMBASA.setBalance(newBalance);
                LOG.info("CORE REFERENCE: {}", coreRef);
                //Update balance payer
                /*Double nwBalance = payerPI.getBalance() - txn.getAmount();
                payerPI.setBalance(nwBalance);
                paymentInstrumentService.update(payerPI);*/

                txn.setPayerPaymentInstrument(payerPI);
                txn.setPayeePaymentInstrument(accountATMBASA);
                break;
            default:

        }
        return txn;
    }

    private Transaction processBatchConfirm(Transaction txn) {
        Transaction creatorTxn = txn.getVoucher().getTxnCreatedBy();
        PaymentInstrument payerPI = creatorTxn.getPayerPaymentInstrument();
        PaymentInstrument accountATMBASA = paymentInstrumentService.getById(Constants.PI_ATM_USER_ID);
        Customer payee = creatorTxn.getPayee();

        String customComment = Constants.STR_ID_RETIRO_SIN_TARGETA + Constants.STR_DASH_SEPARATOR + txn.getUseCase().getId() + Constants.STR_DASH_SEPARATOR + payerPI.getStrIdentifier() + Constants.STR_DASH_SEPARATOR + payee.getMsisdn();
        String coreRef = bankService.transferMoneyProcess(payerPI.getStrIdentifier(), accountATMBASA.getStrIdentifier(), txn.getAmount(), creatorTxn.getId(), Constants.BANK_ACTION_DEFROST, customComment);
        txn.setCoreReference(coreRef);
        //Update balance payee
        if (!coreRef.equals(Constants.STR_CUSTOM_ERR) && !coreRef.equals(Constants.STR_EXCEPTION_ERR) && !coreRef.equals(Constants.STR_DASH_SEPARATOR) && !coreRef.equals(Constants.STR_ZERO)) {
            Double newBalance = accountATMBASA.getBalance() + txn.getAmount();
            accountATMBASA.setBalance(newBalance);
            LOG.info("CORE REFERENCE: {}", coreRef);
            txn.setPayerPaymentInstrument(payerPI);
            txn.setPayeePaymentInstrument(accountATMBASA);
            txn.setTxnStatus(status.getById(Constants.CONFIRM_TXN_STATUS));
        }
        return this.update(txn);
    }

    private Transaction processCancelConfirm(Transaction txn) {
        switch (txn.getUseCase().getId().intValue()) {
            case Constants.INT_VOUCHER_USE_CASE:
                //Defrost founds user
                LOG.info("Cancel txn {}", txn.getId());
                //Account Payer, Amount, comment
                String customComment = Constants.STR_DEFROST_SERVICE_NAME + Constants.STR_DASH_SEPARATOR + txn.getId() + Constants.STR_DASH_SEPARATOR + txn.getPayerPaymentInstrument().getStrIdentifier() + Constants.STR_DASH_SEPARATOR + txn.getPayee().getMsisdn();
                String coreRef = bankService.freezeFounds(txn.getPayerPaymentInstrument().getStrIdentifier(), txn.getAmount(), txn.getId(), Constants.BANK_ACTION_DEFROST, txn.getPayer().getUsername(), customComment);
                txn.setCoreReference(coreRef);

                //Cancel Voucher
                Voucher voucher = voucherService.getVoucherByCreatorTransaction(txn);
                voucher.setActive(false);
                voucher.setCanceled(true);
                voucher.setExpired(true);
                voucherService.update(voucher);
                txn.setExpirationDate(LocalDateTime.now());
                break;
            case Constants.INT_WITHDRAW_VOUCHER_USE_CASE:
                PaymentInstrument payerPI = txn.getPayerPaymentInstrument();
                PaymentInstrument atmPI = txn.getPayeePaymentInstrument();
                //Account Payer, Account Payee, Amount
                //String coreRef = bankService.transferMoney(atmPI.getStrIdentifier(),payerPI.getStrIdentifier(),txn.getAmount(), " txn ref: " + txn.getId());
                //txn.setCoreReference(coreRef);
                //Update balance payer
                Double newBalance = atmPI.getBalance() - txn.getAmount();
                atmPI.setBalance(newBalance);

                //Update balance payee
                /*Double nwBalance = payerPI.getBalance() + txn.getAmount();
                payerPI.setBalance(nwBalance);
                paymentInstrumentService.update(payerPI);*/
                break;
            default:

        }
        return txn;
    }
}
