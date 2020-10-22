package com.bancatlan.atmauthorizer.api.http;

import com.bancatlan.atmauthorizer.api.model.Auth;
import com.bancatlan.atmauthorizer.api.model.SpareParametersDTO;
import com.bancatlan.atmauthorizer.model.Transaction;
import com.bancatlan.atmauthorizer.model.Voucher;

public class OcbRequest implements IOcbRequest {
    private Voucher voucher;
    private Transaction transaction;
    private Auth auth;
    private SpareParametersDTO spareParameters;

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

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public SpareParametersDTO getSpareParameters() {
        return spareParameters;
    }

    public void setSpareParameters(SpareParametersDTO spareParameters) {
        this.spareParameters = spareParameters;
    }
}
