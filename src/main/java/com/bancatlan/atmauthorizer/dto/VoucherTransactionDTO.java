package com.bancatlan.atmauthorizer.dto;

import com.bancatlan.atmauthorizer.model.Identification;
import com.bancatlan.atmauthorizer.model.Identity;
import com.bancatlan.atmauthorizer.model.Transaction;
import com.bancatlan.atmauthorizer.model.Voucher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.LocalDateTime;

public class VoucherTransactionDTO {
    private Voucher voucher;
    private Transaction transaction;
    private String sessionKey;
    private String action;
    private String validatePayeeMsisdn;
    private String amountKey;
    private String amount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime startDateExpired;
    private LocalDateTime endDateExpired;
    private Identification payerIdentification;
    private Identification payeeIdentification;
    private Identity payerIdentity;
    private Identity payeeIdentity;

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

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getStartDateExpired() {
        return startDateExpired;
    }

    public void setStartDateExpired(LocalDateTime startDateExpired) {
        this.startDateExpired = startDateExpired;
    }

    public LocalDateTime getEndDateExpired() {
        return endDateExpired;
    }

    public void setEndDateExpired(LocalDateTime endDateExpired) {
        this.endDateExpired = endDateExpired;
    }

    public Identification getPayerIdentification() {
        return payerIdentification;
    }

    public void setPayerIdentification(Identification payerIdentification) {
        this.payerIdentification = payerIdentification;
    }

    public Identification getPayeeIdentification() {
        return payeeIdentification;
    }

    public void setPayeeIdentification(Identification payeeIdentification) {
        this.payeeIdentification = payeeIdentification;
    }

    public Identity getPayerIdentity() {
        return payerIdentity;
    }

    public void setPayerIdentity(Identity payerIdentity) {
        this.payerIdentity = payerIdentity;
    }

    public Identity getPayeeIdentity() {
        return payeeIdentity;
    }

    public void setPayeeIdentity(Identity payeeIdentity) {
        this.payeeIdentity = payeeIdentity;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "";
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            jsonString = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonString;
    }
}
