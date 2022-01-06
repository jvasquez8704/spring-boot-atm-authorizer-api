package com.bancatlan.atmauthorizer.service.impl;
import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.exception.AuthorizerError;
import com.bancatlan.atmauthorizer.exception.ModelCustomErrorException;
import com.bancatlan.atmauthorizer.model.*;
import com.bancatlan.atmauthorizer.repo.ILimitRepo;
import com.bancatlan.atmauthorizer.service.ILimitService;
import com.bancatlan.atmauthorizer.service.ITransactionService;
import com.bancatlan.atmauthorizer.service.ITxnStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class LimitServiceImpl implements ILimitService {
    private static Logger LOG = LoggerFactory.getLogger(LimitServiceImpl.class);
    @Autowired
    ILimitRepo repo;

    @Autowired
    ITransactionService transaction;

    @Autowired
    ITxnStatusService txnStatusService;

    @Override
    public Limit create(Limit obj) {
        return null;
    }

    @Override
    public Limit update(Limit obj) {
        return null;
    }

    @Override
    public List<Limit> getAll() {
        return null;
    }

    @Override
    public Limit getById(Long id) {
        Optional<Limit> limit = repo.findById(id);
        return limit.isPresent() ? limit.get() : null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public Boolean verifyGlobalLimits(Customer customer) {
        return null;
    }

    @Override
    public Boolean verifyCustomerTypeLimits(Customer customer) {
        return null;
    }

    @Override
    public Boolean verifyTxnParticipantsLimits(Transaction txn) {
        //checking customer, customerType and global limits
        return isWithInCustomerDebitLimits(txn.getPayer()) && isWithInCustomerCreditLimits(txn.getPayee()) &&
                isWithInCustomerTypeDebitLimits(txn) && isWithInCustomerTypeCreditLimits(txn) &&
                isWithInCustomerTypeCreditMixedLimits(txn) && isWithInCustomerTypeDebitMixedLimits(txn) &&
                isWithInGlobalDebitLimits(txn.getPayer()) && isWithInGlobalCreditLimits(txn.getPayee());
    }

    @Override
    public Boolean verifyTxnLimits(Transaction txn) {
        Limit limit = getById(Constants.DEFAULT_LIMIT);

        if(txn.getAmount() > limit.getAmountSingleDebitLimit()){
            throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.AMOUNT_SINGLE_DEBIT_LIMIT_EXCEEDED);
        }

        if(txn.getAmount() > limit.getAmountSingleCreditLimit()){
            throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.AMOUNT_SINGLE_CREDIT_LIMIT_EXCEEDED);
        }

        if(txn.getAmount() < limit.getAmountSingleDebitMinimum()){
            throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.AMOUNT_SINGLE_DEBIT_MINIMUM_EXCEEDED);
        }

        if(txn.getAmount() < limit.getAmountSingleCreditMinimum()){
            throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.AMOUNT_SINGLE_CREDIT_MINIMUM_EXCEEDED);
        }

        return true;
    }

    /*
    * Get total of debits or credits for a customer in period time specified
    * */
    private Double getMovementAmountByCurrentDateRange(Transaction currentTxn ,Customer customer, UseCase useCase, String dateRangeType, Boolean isDebit) {
//        Double amountResult = currentTxn.getAmount();
        Double amountResult = 0.0;
        TxnStatus status = txnStatusService.getById(Constants.CONFIRM_TXN_STATUS);
        LocalDateTime startDay = null;
        LocalDateTime endDay = null;
        LocalDate localDate = LocalDate.now();

        switch (dateRangeType) {
            case Constants.DAILY_RANGE:
                startDay = LocalDateTime.of(localDate, LocalTime.MIDNIGHT);
                endDay = LocalDateTime.of(localDate, LocalTime.MAX);
                break;
            case Constants.WEEKLY_RANGE:
                // The French (France) week starts on MONDAY and ends on SUNDAY
                LocalDate now = LocalDate.now();
                TemporalField fieldISO = WeekFields.of(Locale.FRANCE).dayOfWeek();
                startDay = LocalDateTime.of(now.with(fieldISO, 1), LocalTime.MIDNIGHT);
                endDay = LocalDateTime.of(now.with(fieldISO, 7), LocalTime.MIN);
                break;
            case Constants.MONTHLY_RANGE:
                //todo check this if(localDate.isLeapYear())
                startDay = LocalDateTime.of(localDate.withDayOfMonth(1), LocalTime.MIDNIGHT);
                endDay = LocalDateTime.of(localDate.withDayOfMonth(localDate.getMonth().length(localDate.isLeapYear())), LocalTime.MAX);
                break;
        }

        LOG.info("isDebit " + isDebit);
        LOG.info("Query for getTransactionsByPayerAndRangeTime " + customer.getId() + " startDay " + startDay.toString() + " endDay " + endDay.toString());
        List<Transaction> txnList = transaction.getTransactionsByCustomerAndRangeTime(customer, useCase, status, startDay, endDay,isDebit);
       if(!txnList.isEmpty()){
           for (Transaction txn : txnList) {
               amountResult += txn.getAmount();
           }
       }
        return amountResult;
    }

    private Boolean isWithInCustomerDebitLimits(Customer payee){
        return true;
    }
    private Boolean isWithInCustomerCreditLimits(Customer payee){
        return true;
    }
    private Boolean isWithInCustomerTypeDebitLimits(Transaction txn){
        if (txn.getPayer() == null) {
            LOG.info("{},  error: {}", Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_PROPERLY_CONFIGURATION_ON_PAYER);
            throw new ModelCustomErrorException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_PROPERLY_CONFIGURATION_ON_PAYER);
        }

        if (txn.getPayer().getCustomerType() == null) {
            LOG.info("{},  error: {}", Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_PROPERLY_CONFIGURATION_ON_PAYER);
            throw new ModelCustomErrorException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_PROPERLY_CONFIGURATION_ON_PAYER);
        }

        if (txn.getPayer().getCustomerType().getLimit() == null) {
            LOG.info("{},  error: {}", Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_CONFIGURATION_LIMITS_ON_CST_TYPE_PAYER);
            throw new ModelCustomErrorException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_CONFIGURATION_LIMITS_ON_CST_TYPE_PAYER);
        }
        Limit limit = txn.getPayer().getCustomerType().getLimit();
        if(limit != null && limit.getActive()){
            if (getMovementAmountByCurrentDateRange(txn, txn.getPayer(), txn.getUseCase(), Constants.DAILY_RANGE, true) > limit.getDailyDebitLimit()) {
                LOG.info("{},  error: {}", Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYER_DAILY_DEBIT_LIMIT_EXCEEDED);
                throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYER_DAILY_DEBIT_LIMIT_EXCEEDED);
            }
            if (getMovementAmountByCurrentDateRange(txn, txn.getPayer(), txn.getUseCase(), Constants.WEEKLY_RANGE, true) > limit.getWeeklyDebitLimit()) {
                LOG.info("{},  error: {}", Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYER_WEEKLY_DEBIT_LIMIT_EXCEEDED);
                throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYER_WEEKLY_DEBIT_LIMIT_EXCEEDED);
            }
            if (getMovementAmountByCurrentDateRange(txn, txn.getPayer(), txn.getUseCase(), Constants.MONTHLY_RANGE, true) > limit.getMonthlyDebitLimit()) {
                LOG.info("{},  error: {}", Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYER_MONTHLY_DEBIT_LIMIT_EXCEEDED);
                throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYER_MONTHLY_DEBIT_LIMIT_EXCEEDED);
            }
        }
        return true;
    }
    private Boolean isWithInCustomerTypeCreditLimits(Transaction txn){
        if (txn.getPayee() == null) {
            LOG.info("{},  error: {}", Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_PROPERLY_CONFIGURATION_ON_PAYEE);
            throw new ModelCustomErrorException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_PROPERLY_CONFIGURATION_ON_PAYEE);
        }

        if (txn.getPayee().getCustomerType() == null) {
            LOG.info("{},  error: {}", Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_PROPERLY_CONFIGURATION_ON_PAYEE);
            throw new ModelCustomErrorException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_PROPERLY_CONFIGURATION_ON_PAYEE);
        }

        if (txn.getPayee().getCustomerType().getLimit() == null) {
            LOG.info("{},  error: {}", Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_CONFIGURATION_LIMITS_ON_CST_TYPE_PAYEE);
            throw new ModelCustomErrorException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.NOT_CONFIGURATION_LIMITS_ON_CST_TYPE_PAYEE);
        }
        Limit limit = txn.getPayee().getCustomerType().getLimit();
        if(limit != null && limit.getActive()){
            if (getMovementAmountByCurrentDateRange(txn, txn.getPayee(), txn.getUseCase(), Constants.DAILY_RANGE, false) > limit.getDailyCreditLimit()) {
                LOG.info("{},  error: {}", Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYEE_DAILY_CREDIT_LIMIT_EXCEEDED);
                throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYEE_DAILY_CREDIT_LIMIT_EXCEEDED);
            }
            if (getMovementAmountByCurrentDateRange(txn, txn.getPayee(), txn.getUseCase(), Constants.WEEKLY_RANGE, false) > limit.getWeeklyCreditLimit()) {
                LOG.info("{},  error: {}", Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYEE_WEEKLY_CREDIT_LIMIT_EXCEEDED);
                throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYEE_WEEKLY_CREDIT_LIMIT_EXCEEDED);
            }
            if (getMovementAmountByCurrentDateRange(txn, txn.getPayee(), txn.getUseCase(), Constants.MONTHLY_RANGE, false) > limit.getMonthlyCreditLimit()) {
                LOG.info("{},  error: {}", Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYEE_MONTHLY_CREDIT_LIMIT_EXCEEDED);
                throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYEE_MONTHLY_CREDIT_LIMIT_EXCEEDED);
            }
        }
        return true;
    }
    private Boolean isWithInCustomerTypeCreditMixedLimits(Transaction txn) {
        //Check config if exist
        List<Long> mixedUseCaseLimitConfigured = new ArrayList<>();
        mixedUseCaseLimitConfigured.add(new Long(174));
        mixedUseCaseLimitConfigured.add(new Long(176));
        mixedUseCaseLimitConfigured.add(new Long(177));

        Limit payeeLimitConfig = txn.getPayee().getCustomerType().getLimit();
        if(payeeLimitConfig != null && payeeLimitConfig.getActive()) {
            Double dailyCreditAmountCounter = new Double(0);
            for(Long useCase: mixedUseCaseLimitConfigured) {
                UseCase currentUC = new UseCase();
                currentUC.setId(useCase);
                dailyCreditAmountCounter += getMovementAmountByCurrentDateRange(txn, txn.getPayee(), currentUC, Constants.DAILY_RANGE, false);
            }
            dailyCreditAmountCounter+= txn.getAmount();
            if (dailyCreditAmountCounter > payeeLimitConfig.getDailyCreditLimit()) {
                LOG.info("{},  error: {}", Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYEE_DAILY_CREDIT_LIMIT_EXCEEDED);
                throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYEE_DAILY_CREDIT_LIMIT_EXCEEDED);
            }
            Double WeeklyCreditAmountCounter = new Double(0);
            for(Long useCase: mixedUseCaseLimitConfigured) {
                UseCase currentUC = new UseCase();
                currentUC.setId(useCase);
                WeeklyCreditAmountCounter += getMovementAmountByCurrentDateRange(txn, txn.getPayee(), currentUC, Constants.WEEKLY_RANGE, false);
            }
            WeeklyCreditAmountCounter += txn.getAmount();
            if (WeeklyCreditAmountCounter > payeeLimitConfig.getWeeklyCreditLimit()) {
                LOG.info("{},  error: {}", Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYEE_WEEKLY_CREDIT_LIMIT_EXCEEDED);
                throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYEE_WEEKLY_CREDIT_LIMIT_EXCEEDED);
            }

            Double monthlyCreditAmountCounter = new Double(0);
            for(Long useCase: mixedUseCaseLimitConfigured) {
                UseCase currentUC = new UseCase();
                currentUC.setId(useCase);
                monthlyCreditAmountCounter += getMovementAmountByCurrentDateRange(txn, txn.getPayee(), currentUC, Constants.MONTHLY_RANGE, false);
            }
            monthlyCreditAmountCounter += txn.getAmount();
            if (monthlyCreditAmountCounter > payeeLimitConfig.getMonthlyCreditLimit()) {
                LOG.info("{},  error: {}", Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYEE_MONTHLY_CREDIT_LIMIT_EXCEEDED);
                throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYEE_MONTHLY_CREDIT_LIMIT_EXCEEDED);
            }
        }
        return true;
    }
    private Boolean isWithInCustomerTypeDebitMixedLimits(Transaction txn) {
        //Check config if exist
        List<Long> mixedUseCaseLimitConfigured = new ArrayList<>();
        mixedUseCaseLimitConfigured.add(new Long(174));
        mixedUseCaseLimitConfigured.add(new Long(176));
        mixedUseCaseLimitConfigured.add(new Long(177));

        Limit payerLimitConfig = txn.getPayer().getCustomerType().getLimit();
        if(payerLimitConfig != null && payerLimitConfig.getActive()) {
            Double dailyDebitAmountCounter = new Double(0);
            for(Long useCase: mixedUseCaseLimitConfigured) {
                UseCase currentUC = new UseCase();
                currentUC.setId(useCase);
                dailyDebitAmountCounter += getMovementAmountByCurrentDateRange(txn, txn.getPayer(), currentUC, Constants.DAILY_RANGE, true);
            }
            dailyDebitAmountCounter+= txn.getAmount();
            if (dailyDebitAmountCounter > payerLimitConfig.getDailyCreditLimit()) {
                LOG.info("{},  error: {}", Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYER_DAILY_DEBIT_LIMIT_EXCEEDED);
                throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYER_DAILY_DEBIT_LIMIT_EXCEEDED);
            }
            Double WeeklyDebitAmountCounter = new Double(0);
            for(Long useCase: mixedUseCaseLimitConfigured) {
                UseCase currentUC = new UseCase();
                currentUC.setId(useCase);
                WeeklyDebitAmountCounter += getMovementAmountByCurrentDateRange(txn, txn.getPayer(), currentUC, Constants.WEEKLY_RANGE, true);
            }
            WeeklyDebitAmountCounter += txn.getAmount();
            if (WeeklyDebitAmountCounter > payerLimitConfig.getWeeklyCreditLimit()) {
                LOG.info("{},  error: {}", Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYER_WEEKLY_DEBIT_LIMIT_EXCEEDED);
                throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYER_WEEKLY_DEBIT_LIMIT_EXCEEDED);
            }

            Double monthlyDebitAmountCounter = new Double(0);
            for(Long useCase: mixedUseCaseLimitConfigured) {
                UseCase currentUC = new UseCase();
                currentUC.setId(useCase);
                monthlyDebitAmountCounter += getMovementAmountByCurrentDateRange(txn, txn.getPayer(), currentUC, Constants.MONTHLY_RANGE, true);
            }
            monthlyDebitAmountCounter += txn.getAmount();
            if (monthlyDebitAmountCounter > payerLimitConfig.getMonthlyCreditLimit()) {
                LOG.info("{},  error: {}", Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYER_MONTHLY_DEBIT_LIMIT_EXCEEDED);
                throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PAYER_MONTHLY_DEBIT_LIMIT_EXCEEDED);
            }
        }
        return true;
    }
    private Boolean isWithInGlobalDebitLimits(Customer payee){
        return true;
    }
    private Boolean isWithInGlobalCreditLimits(Customer payee){
        return true;
    }
}


