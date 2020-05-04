package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.component.Constants;
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

    @Override
    public Transaction initTxn(Transaction txn) {
        Transaction initTxn = new Transaction();
        if (txn.getUseCase() == null || txn.getUseCase().getId() == null) {
            throw new ModelCustomErrorException(Constants.PARAMETER_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_FOUND_USE_CASE);
        }

        if (txn.getCurrency() == null || txn.getCurrency().getCode() == null || txn.getCurrency().getCode().equals("")) {
            throw new ModelCustomErrorException(Constants.PARAMETER_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_FOUND_CURRENCY_IN_REQ);
        }

        Currency currency = currencyService.getCurrencyByCode(txn.getCurrency().getCode());
        if(currency == null){
            throw new ModelCustomErrorException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_FOUND_CURRENCY);
        }

        initTxn.setTxnStatus(status.getById(Constants.INITIAL_TXN_STATUS));
        initTxn.setAmount(txn.getAmount());
        initTxn.setUseCase(txn.getUseCase());
        initTxn.setCurrency(currency);
        return this.create(initTxn);
    }

    @Override
    public Transaction preAuthorizationTxn(Transaction txn) {
        this.processPreAuthorizationTxn(txn);
        txn.setTxnStatus(status.getById(Constants.PRE_AUTHORIZED_TXN_STATUS));
        return this.update(txn);
    }

    @Override
    public Transaction authorizationTxn(Transaction txn) {
        this.processAuthenticationTxn(txn);
        txn.setTxnStatus(status.getById(Constants.AUTHENTICATED_TXN_STATUS));
        return this.update(txn);
    }

    @Override
    public Transaction verifyTxn(Transaction txn) {
        if(!this.verifyTxnParticipants(txn) && !this.verifyTxnLimits(txn)){
            throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.ERROR_ON_VERIFY);
        }
        txn.setTxnStatus(status.getById(Constants.AUTHORIZED_TXN_STATUS));
        return this.update(txn);
    }

    @Override
    public Transaction confirmTxn(Transaction txn) {
        Transaction txnRet = this.processConfirmTxn(txn);
        txnRet.setTxnStatus(status.getById(Constants.CONFIRM_TXN_STATUS));
        return this.update(txnRet);
    }

    @Override
    public Transaction cancelConfirmTxn(Transaction txn) {
        txn.setTxnStatus(status.getById(Constants.CANCEL_CONFIRM_TXN_STATUS));
        txn.setUpdateDate(LocalDateTime.now());
        return repo.save(txn);
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

    private Transaction processAuthenticationTxn(Transaction txn) {
        switch (txn.getUseCase().getId().intValue()) {
            case Constants.INT_VOUCHER_USE_CASE:
                Customer payer = customer.getByOcbUser(txn.getPayer().getOcbUser());
                if (payer == null) {
                    payer = customer.register(txn.getPayer());
                }

                Customer payee = customer.checkIfCustomerExist(txn.getPayee().getMsisdn());
                if (payee == null) {
                    payee = customer.register(txn.getPayee());
                }

                //CHECK PAYER PI
                if (txn.getPayerPaymentInstrument() == null || txn.getPayerPaymentInstrument().getStrIdentifier() == null || txn.getPayerPaymentInstrument().getStrIdentifier().equals("")) {
                    LOG.error(AuthorizerError.MISSING_PAYER_PI.toString() + txn);
                    throw new ModelNotFoundException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.MISSING_PAYER_PI);
                }
                //CHECK PAYER OCB_USER
                if (txn.getPayer() == null || txn.getPayer().getOcbUser() == null || txn.getPayer().getOcbUser().equals("")) {
                    LOG.error(AuthorizerError.MISSING_OCB_USER.toString() + txn);
                    throw new ModelNotFoundException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.MISSING_OCB_USER);
                }

                //GET BANK ACCOUNTS BY OCB_USER
                Boolean isFoundPI = false;
                //Boolean isFoundPI = true;
                List<PaymentInstrument> accountsUser = bankService.getBankAccountsByUserId(txn.getPayer().getOcbUser());
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
                }

                PaymentInstrument payerPi = paymentInstrumentService.getPaymentInstrumentByStrIdentifier(txn.getPayerPaymentInstrument().getStrIdentifier());
                //PaymentInstrument payerPi = paymentInstrumentService.getPaymentInstrumentByCustomerAndStrIdentifier(payer, txn.getPayerPaymentInstrument().getStrIdentifier());
                if (payerPi == null) {
                    payerPi = paymentInstrumentService.create(txn.getPayerPaymentInstrument());
                }

                txn.setPayer(payer);
                txn.setPayee(payee);
                txn.setPayerPaymentInstrument(payerPi);
                break;
            case Constants.INT_WITHDRAW_VOUCHER_USE_CASE:
                Customer clientAtmPayer = customer.checkIfCustomerExist(txn.getPayer().getMsisdn());
                if (clientAtmPayer == null) {
                    throw new ModelCustomErrorException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR,
                            AuthorizerError.NOT_FOUND_PAYEE_ATM);
                }

                Customer userATM = customer.getById(Constants.ATM_USER_ID);
                if (userATM == null || userATM.getName().trim().equals(Constants.ATM_USER_STR)) {
                    throw new ModelCustomErrorException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR,
                            AuthorizerError.NOT_FOUND_PAYEE_ATM);
                }

                txn.setPayer(clientAtmPayer);
                txn.setPayee(userATM);
                break;
            default:

        }
        return this.update(txn);
    }

    private Transaction processPreAuthorizationTxn(Transaction txn){
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

    private Transaction processConfirmTxn(Transaction txn) {
        switch (txn.getUseCase().getId().intValue()) {
            case Constants.INT_VOUCHER_USE_CASE:
                PaymentInstrument accountATMBASA = paymentInstrumentService.getById(Constants.PI_ATM_USER_ID);
                //Account Payer, Account Payee, Amount
                String coreRef = bankService.transferMoney(txn.getPayerPaymentInstrument().getStrIdentifier(),accountATMBASA.getStrIdentifier(),txn.getAmount(), "");
                txn.setCoreReference(coreRef);
                //Update balance payer
                Double newBalance = accountATMBASA.getBalance() + txn.getAmount();
                accountATMBASA.setBalance(newBalance);
                paymentInstrumentService.update(accountATMBASA);
                break;
            case Constants.INT_WITHDRAW_VOUCHER_USE_CASE:

                break;
            default:

        }
        return txn;
    }
}
