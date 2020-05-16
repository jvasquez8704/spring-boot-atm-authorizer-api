package com.bancatlan.atmauthorizer.service;

import com.bancatlan.atmauthorizer.model.PaymentInstrument;

import java.util.List;

public interface IBankService {
    Boolean verifyAccountByOcbUser(String ocbUser, String strAccount);
    Boolean sendNotification(String email, String subject, String body, String typeNotification);
    String transferMoney(String accountDebit, String accountCredit, Double amount, String customComment);
    String freezeFounds(String accountDebit, Double amount, String customComment);
    List<PaymentInstrument> getBankAccountsByUserId(String userId);
}
