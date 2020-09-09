package com.bancatlan.atmauthorizer.service;

import com.bancatlan.atmauthorizer.model.Transaction;

public interface IIDmissionService {
    Boolean updateTransaction(Transaction txn);
}
