package com.bancatlan.atmauthorizer.api.http;

import com.bancatlan.atmauthorizer.model.Transaction;
import com.bancatlan.atmauthorizer.model.Voucher;

public class BankCustomRequest {
    private Voucher voucher;
    private Transaction transaction;
    private String sessionKey;
    private String action;
    private String validatePayeeMsisdn;
    private String amountKey;
    private String amount;


    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getValidatePayeeMsisdn() {
        return validatePayeeMsisdn;
    }

    public void setValidatePayeeMsisdn(String validatePayeeMsisdn) {
        this.validatePayeeMsisdn = validatePayeeMsisdn;
    }

    public String getAmountKey() {
        return amountKey;
    }

    public void setAmountKey(String amountKey) {
        this.amountKey = amountKey;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
