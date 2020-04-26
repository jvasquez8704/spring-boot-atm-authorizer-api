package com.bancatlan.atmauthorizer.service;

import com.bancatlan.atmauthorizer.model.Customer;
import com.bancatlan.atmauthorizer.model.Limit;
import com.bancatlan.atmauthorizer.model.Transaction;

public interface ILimitService extends ICRUD<Limit> {
    Boolean verifyGlobalLimits(Customer customer);
    Boolean verifyCustomerTypeLimits(Customer customer);
    Boolean verifyTxnParticipantsLimits(Transaction txn);
    Boolean verifyTxnLimits(Transaction txn);
}
