package com.bancatlan.atmauthorizer.service;

import com.bancatlan.atmauthorizer.model.Customer;
import com.bancatlan.atmauthorizer.model.Transaction;
import com.bancatlan.atmauthorizer.model.UseCase;

import java.time.LocalDateTime;
import java.util.List;

public interface ITransactionService extends ICRUD<Transaction> {
    Transaction initTxn(Transaction txn);
    Transaction preAuthorizationTxn(Transaction txn);
    Transaction authorizationTxn(Transaction txn);
    Transaction verifyTxn(Transaction txn);
    Transaction confirmTxn(Transaction txn);
    Transaction cancelConfirmTxn(Transaction txn);
    Boolean verifyTxnParticipants(Transaction txn);
    Boolean verifyTxnLimits(Transaction txn);
    Transaction getTransactionByAtmReference(String atmReference);
    List<Transaction> getTransactionsByCustomerAndRangeTime(Customer customer, UseCase useCase, LocalDateTime startDateTime, LocalDateTime endDateTime, Boolean isDebit);
}
