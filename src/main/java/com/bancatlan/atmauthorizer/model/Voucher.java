package com.bancatlan.atmauthorizer.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amountInitial", nullable = false, length = 20)
    private Double amountInitial;

    @Column(name = "amountCurrent", nullable = false, length = 20)
    private Double amountCurrent;

    @Column(name = "pickupCode", nullable = false, length = 8)
    private String pickupCode;

    @Column(name = "secretCode", nullable = false, length = 8)
    private String secretCode;

    @Column(name = "atmReference", nullable = true, length = 40)
    private String atmReference;

    @ManyToOne
    @JoinColumn(name = "id_customer", nullable = true, foreignKey = @ForeignKey(name = "fk_voucher_customer"))
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "id_payer", nullable = true, foreignKey = @ForeignKey(name = "fk_voucher_payer"))
    private Customer payer;

    @OneToOne
    @JoinColumn(name = "id_txn_paid_out_by", nullable = true, foreignKey = @ForeignKey(name = "fk_txn_paid_out_by"))
    private Transaction txnPaidOutBy;

    @OneToOne
    @JoinColumn(name = "id_txn_created_by", nullable = false, foreignKey = @ForeignKey(name = "fk_txn_created_by"))
    private Transaction txnCreatedBy;

    @Column(name = "expirationDate", nullable = true, length = 30)
    private LocalDateTime expirationDate;

    @Column(name = "creationDate", nullable = true, length = 30)
    private LocalDateTime creationDate;

    @Column(name = "updateDate", nullable = true, length = 30)
    private LocalDateTime updateDate;

    @ManyToOne
    @JoinColumn(name = "id_customer_creation", nullable = true, foreignKey = @ForeignKey(name = "fk_voucher_customer_c"))
    private Customer customerCreation;

    @ManyToOne
    @JoinColumn(name = "id_customer_update", nullable = true, foreignKey = @ForeignKey(name = "fk_voucher_customer_u"))
    private Customer customerUpdate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmountInitial() {
        return amountInitial;
    }

    public void setAmountInitial(Double amountInitial) {
        this.amountInitial = amountInitial;
    }

    public Double getAmountCurrent() {
        return amountCurrent;
    }

    public void setAmountCurrent(Double amountCurrent) {
        this.amountCurrent = amountCurrent;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }

    public String getSecretCode() {
        return secretCode;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }

    public String getAtmReference() {
        return atmReference;
    }

    public void setAtmReference(String atmReference) {
        this.atmReference = atmReference;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Customer getPayer() {
        return payer;
    }

    public void setPayer(Customer payer) {
        this.payer = payer;
    }

    public Transaction getTxnPaidOutBy() {
        return txnPaidOutBy;
    }

    public void setTxnPaidOutBy(Transaction _txnPaidOutBy) {
        txnPaidOutBy = _txnPaidOutBy;
    }

    public Transaction getTxnCreatedBy() {
        return txnCreatedBy;
    }

    public void setTxnCreatedBy(Transaction _txnCreatedBy) {
        txnCreatedBy = _txnCreatedBy;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public Customer getCustomerCreation() {
        return customerCreation;
    }
    public void setCustomerCreation(Customer customerCreation) {
        this.customerCreation = customerCreation;
    }

    public Customer getCustomerUpdate() {
        return customerUpdate;
    }

    public void setCustomerUpdate(Customer customerUpdate) {
        this.customerUpdate = customerUpdate;
    }
}
