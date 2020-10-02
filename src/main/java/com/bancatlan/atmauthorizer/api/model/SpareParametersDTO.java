package com.bancatlan.atmauthorizer.api.model;

import com.bancatlan.atmauthorizer.model.Identification;
import com.bancatlan.atmauthorizer.model.Identity;

public class SpareParametersDTO {
    private Identification payerIdentification;
    private Identification payeeIdentification;
    private Identity payerIdentity;
    private Identity payeeIdentity;

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
}
