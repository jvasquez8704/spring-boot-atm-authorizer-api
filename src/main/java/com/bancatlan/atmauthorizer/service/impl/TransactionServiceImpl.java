package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.component.IUtilComponent;
import com.bancatlan.atmauthorizer.exception.AtmError;
import com.bancatlan.atmauthorizer.exception.AuthorizerError;
import com.bancatlan.atmauthorizer.exception.ModelCustomErrorException;
import com.bancatlan.atmauthorizer.exception.ModelNotFoundException;
import com.bancatlan.atmauthorizer.model.*;
import com.bancatlan.atmauthorizer.repo.ITransactionRepo;
import com.bancatlan.atmauthorizer.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    public Transaction getTransactionByAtmReference(String atmReference) {
        return repo.getTransactionByAtmReference(atmReference);
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
                initTxn.setAmount(txn.getAmount());
                break;
            default:
        }
        //set currency
        if (txn.getCurrency() == null || txn.getCurrency().getCode() == null || txn.getCurrency().getCode().equals("")) {
            throw new ModelCustomErrorException(Constants.PARAMETER_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_FOUND_CURRENCY_IN_REQ);
        }
        Currency currency = currencyService.getCurrencyByCode(txn.getCurrency().getCode());
        if(currency == null){
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
                    LOG.error(AuthorizerError.MISSING_OCB_USER.toString());
                    throw new ModelNotFoundException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.MISSING_OCB_USER);
                }

                if (txn.getPayee() == null || txn.getPayee().getMsisdn() == null || txn.getPayer().getMsisdn().equals("")) {
                    LOG.error(AuthorizerError.MISSING_PAYER_PI.toString());
                    throw new ModelNotFoundException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.MISSING_PAYER_PI);
                }
                //business rules, if users not exits, create them as pre-registered customers
                Customer payer = customer.getByUsername(txn.getPayer().getUsername());
                if (payer == null) {
                    txn.getPayer().setCountry(null);//hack
                    //Set creator user
                    payer = customer.register(txn.getPayer());
                }

                Customer payee = customer.checkIfCustomerExist(txn.getPayee().getMsisdn());
                if (payee == null) {
                    txn.getPayee().setCountry(null);//hack
                    //Set creator user
                    payee = customer.register(txn.getPayee());
                }

                //CHECK PAYER PI
                if (txn.getPayerPaymentInstrument() == null || txn.getPayerPaymentInstrument().getStrIdentifier() == null || txn.getPayerPaymentInstrument().getStrIdentifier().equals("")) {
                    LOG.error(AuthorizerError.MISSING_PAYER_PI.toString() + txn);
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
                    LOG.error("NumberPhone does not come properly in request", txn);
                    throw new ModelNotFoundException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_14);
                }

                Customer cst = customer.getByMsisdn(txn.getPayer().getMsisdn());
                if (cst == null) {
                    LOG.error("Customer with numberPhone specified in request not fount ", txn);
                    throw new ModelNotFoundException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_25);
                }

                Customer userATM = customer.getById(Constants.ATM_USER_ID);
                if (userATM == null || !userATM.getName().trim().equals(Constants.ATM_USER_STR)) {
                    throw new ModelCustomErrorException(Constants.ATM_EXCEPTION_TYPE, AtmError.ERROR_03);
                }

                txn.setPayer(cst);
                txn.setPayee(userATM);
                break;
            default:

        }
        return this.update(txn);
    }

    private Transaction processAuthorization(Transaction txn){
        switch (txn.getUseCase().getId().intValue()){
            case Constants.INT_VOUCHER_USE_CASE:
                //Todo verifyBasaUser
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
                //Not do nothing for now
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
                String Ref = bankService.freezeFounds(txn.getPayerPaymentInstrument().getStrIdentifier(),txn.getAmount(), " txn ref: " + txn.getId());
                //txn.setCoreReference(coreRef);
                //Update balance payer
                //Double newBalance = accountATMBASA.getBalance() + txn.getAmount();
                //accountATMBASA.setBalance(newBalance);
                //paymentInstrumentService.update(accountATMBASA);
                break;
            case Constants.INT_WITHDRAW_VOUCHER_USE_CASE:
                Transaction creatorTxn = txn.getVoucher().getTxnCreatedBy();
                PaymentInstrument payerPI = creatorTxn.getPayerPaymentInstrument();
                PaymentInstrument accountATMBASA = paymentInstrumentService.getById(Constants.PI_ATM_USER_ID);


                //Account Payer, Account Payee, Amount
                String coreRef = bankService.transferMoney(payerPI.getStrIdentifier(),accountATMBASA.getStrIdentifier(),txn.getAmount(), " txn ref: " + txn.getId());
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

    private Transaction processCancelConfirm(Transaction txn) {
        switch (txn.getUseCase().getId().intValue()) {
            case Constants.INT_VOUCHER_USE_CASE:
                //Not do nothing
                break;
            case Constants.INT_WITHDRAW_VOUCHER_USE_CASE:
                PaymentInstrument payerPI = txn.getPayerPaymentInstrument();
                PaymentInstrument atmPI = txn.getPayeePaymentInstrument();
                //Account Payer, Account Payee, Amount
                String coreRef = bankService.transferMoney(atmPI.getStrIdentifier(),payerPI.getStrIdentifier(),txn.getAmount(), " txn ref: " + txn.getId());
                txn.setCoreReference(coreRef);
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
