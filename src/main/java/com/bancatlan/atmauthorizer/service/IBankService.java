package com.bancatlan.atmauthorizer.service;

import com.bancatlan.atmauthorizer.model.PaymentInstrument;
import com.bancatlan.atmauthorizer.model.Transaction;

import java.util.List;

public interface IBankService {
    Boolean verifyAccountByOcbUser(String ocbUser, String strAccount);
    Boolean sendNotification(String email, String subject, String body, String typeNotification);
    String transferMoney(String accountDebit, String accountCredit, Double amount, Long ref, String action, String customComment,String useCase);
    String transferMoneyProcess(String accountDebit, String accountCredit, Double amount, Long ref, String action, String customComment,String useCase);
    String transferMoneyProcess(Transaction txn);
    String freezeFoundsProcess(String accountDebit, Double amount, Long ref, String action, String userName, String customComment, String useCase);
    String freezeFounds(String accountDebit, Double amount, Long ref, String action, String userName, String customComment,String useCase);
    List<PaymentInstrument> getBankAccountsByUserId(String userId);
    Boolean sendWithdrawalToAccountingClosing(Transaction startTxn, Transaction withdrawalTxn);
}
