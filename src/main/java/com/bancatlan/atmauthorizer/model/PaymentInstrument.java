package com.bancatlan.atmauthorizer.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name ="paymentInstrument")
public class PaymentInstrument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "strIdentifier", nullable = true, length = 25)
    private String strIdentifier;

    @Column(name = "strCustomBalance", nullable = true, length = 15)
    private String strCustomBalance;

    @Column(name = "strCustomStatus", nullable = true, length = 10)
    private String strCustomStatus;

    @Column(name = "alias", nullable = true, length = 25)
    private String alias;

    @Column(name = "balance", nullable = true, length = 25)
    private Double balance;

    @Column(name = "description", nullable = true, length = 50)
    private String description;
    /*Associations*/

    @ManyToOne
    @JoinColumn(name = "id_pi_type", nullable = true, foreignKey = @ForeignKey(name = "fk_pi_pi_type"))
    private PaymentInstrumentType paymentInstrumentType;

    @ManyToOne
    @JoinColumn(name = "id_customer", nullable = true, foreignKey = @ForeignKey(name = "fk_pi_customer"))
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "id_currency", nullable = true, foreignKey = @ForeignKey(name = "fk_pi_currency"))
    private Currency currency;

    /*metadata*/
    @Column(name = "isActive", nullable = true)
    private Boolean isActive;

    @Column(name = "isCanceled", nullable = true)
    private Boolean isCanceled;

    @Column(name = "isDeleted", nullable = true)
    private Boolean isDeleted;

    @Column(name = "creationDate", nullable = true, length = 30)
    private LocalDateTime creationDate;

    @Column(name = "updateDate", nullable = true, length = 30)
    private LocalDateTime updateDate;

    @ManyToOne
    @JoinColumn(name = "id_customer_creation", nullable = true, foreignKey = @ForeignKey(name = "fk_pi_creator"))
    private Customer customerCreation;

    @ManyToOne
    @JoinColumn(name = "id_customer_update", nullable = true, foreignKey = @ForeignKey(name = "fk_pi_updater"))
    private Customer customerUpdate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStrIdentifier() {
        return strIdentifier;
    }

    public void setStrIdentifier(String strIdentifier) {
        this.strIdentifier = strIdentifier;
    }

    public String getStrCustomBalance() {
        return strCustomBalance;
    }

    public void setStrCustomBalance(String strCustomBalance) {
        this.strCustomBalance = strCustomBalance;
    }

    public String getStrCustomStatus() {
        return strCustomStatus;
    }

    public void setStrCustomStatus(String strCustomStatus) {
        this.strCustomStatus = strCustomStatus;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String title) {
        this.alias = alias;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PaymentInstrumentType getPaymentInstrumentType() {
        return paymentInstrumentType;
    }

    public void setPaymentInstrumentType(PaymentInstrumentType paymentInstrumentType) {
        this.paymentInstrumentType = paymentInstrumentType;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Boolean getCanceled() {
        return isCanceled;
    }

    public void setCanceled(Boolean canceled) {
        isCanceled = canceled;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
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
