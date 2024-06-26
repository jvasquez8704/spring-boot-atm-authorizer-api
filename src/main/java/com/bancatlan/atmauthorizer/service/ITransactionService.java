package com.bancatlan.atmauthorizer.service;

import com.bancatlan.atmauthorizer.model.Customer;
import com.bancatlan.atmauthorizer.model.Transaction;
import com.bancatlan.atmauthorizer.model.TxnStatus;
import com.bancatlan.atmauthorizer.model.UseCase;

import java.time.LocalDateTime;
import java.util.List;

public interface ITransactionService extends ICRUD<Transaction> {
    Transaction init(Transaction txn);
    Transaction authentication(Transaction txn);
    Transaction authorization(Transaction txn);
    Transaction verify(Transaction txn);
    Transaction preConfirm(Transaction txn);
    Transaction confirm(Transaction txn);
    Transaction cancelConfirm(Transaction txn);
    Boolean processBatchCancelConfirm(Transaction txn);
    Boolean verifyTxnParticipants(Transaction txn);
    Boolean verifyTxnLimits(Transaction txn);
    Boolean verifyMixedTxnLimits(Transaction txn);
    List<Transaction> getTransactionByAtmReference(String atmReference);
    Transaction getTransactionByAtmReference(String atmReference, Long txnStatus);
    Transaction getTransactionByApplicationIdAndChannelReference(String applicationId, String channelReference);
    List<Transaction> getTransactionsByCustomerAndRangeTime(Customer customer, UseCase useCase, TxnStatus status, LocalDateTime startDateTime, LocalDateTime endDateTime, Boolean isDebit);
    void executeAllConfirmedWithDrawls();
    void executeFailedWithDrawls();
    void reverseExpiredTransactions();
}
