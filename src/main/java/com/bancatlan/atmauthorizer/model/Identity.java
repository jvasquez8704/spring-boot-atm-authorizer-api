package com.bancatlan.atmauthorizer.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "identity")
public class Identity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_customer", foreignKey = @ForeignKey(name = "fk_identity_customer"))
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "id_identity_type", foreignKey = @ForeignKey(name = "fk_identity_type"))
    private IdentityType type;

    @Column(name = "strIdentity")
    private String strIdentity;

    @Column(name = "strIssuer")
    private String strIssuer;

    @ManyToOne
    @JoinColumn(name = "id_status", foreignKey = @ForeignKey(name = "fk_identity_status"))
    private EntityStatus status;

    @Column(name = "issuedDate", length = 30)
    private LocalDateTime issuedDate;

    @ManyToOne
    @JoinColumn(name = "id_issued_country", foreignKey = @ForeignKey(name = "fk_identity_country"))
    private Country issuedCountry;

    /*Metadata*/
    @Column(name = "isActive")
    private Boolean isActive;

    @Column(name = "isCanceled")
    private Boolean isCanceled;

    @Column(name = "isDeleted")
    private Boolean isDeleted;

    @Column(name = "expirationDate", length = 30)
    private LocalDateTime expirationDate;

    @Column(name = "creationDate", length = 30)
    private LocalDateTime creationDate;

    @Column(name = "updateDate", length = 30)
    private LocalDateTime updateDate;

    @ManyToOne
    @JoinColumn(name = "id_customer_creation", foreignKey = @ForeignKey(name = "fk_identity_creator"))
    private Customer customerCreation;

    @ManyToOne
    @JoinColumn(name = "id_customer_update", foreignKey = @ForeignKey(name = "fk_identity_updater"))
    private Customer customerUpdate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public IdentityType getType() {
        return type;
    }

    public void setType(IdentityType type) {
        this.type = type;
    }

    public String getStrIdentity() {
        return strIdentity;
    }

    public void setStrIdentity(String strIdentity) {
        this.strIdentity = strIdentity;
    }

    public String getStrIssuer() {
        return strIssuer;
    }

    public void setStrIssuer(String strIssuer) {
        this.strIssuer = strIssuer;
    }

    public EntityStatus getStatus() {
        return status;
    }

    public void setStatus(EntityStatus status) {
        this.status = status;
    }

    public LocalDateTime getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(LocalDateTime issuedDate) {
        this.issuedDate = issuedDate;
    }

    public Country getIssuedCountry() {
        return issuedCountry;
    }

    public void setIssuedCountry(Country issuedCountry) {
        this.issuedCountry = issuedCountry;
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
