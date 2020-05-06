package com.bancatlan.atmauthorizer.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "identification")
public class Identification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_customer", foreignKey = @ForeignKey(name = "fk_identification_customer"))
    private Customer customer;

    @Column(name = "strIdentification", length = 45)
    private String strIdentification;

    @ManyToOne
    @JoinColumn(name = "id_identification_type", foreignKey = @ForeignKey(name = "fk_identification_type"))
    private IdentificationType type;

    @ManyToOne
    @JoinColumn(name = "id_identification_status", foreignKey = @ForeignKey(name = "fk_identification_status"))
    private EntityStatus status;

    @ManyToOne
    @JoinColumn(name = "id_org_unit", foreignKey = @ForeignKey(name = "fk_identification_org_unit"))
    private OrgUnit orgUnit;

    /*metadata*/
    @Column(name = "creationDate", length = 30)
    private LocalDateTime creationDate;

    @Column(name = "updateDate", length = 30)
    private LocalDateTime updateDate;

    @ManyToOne
    @JoinColumn(name = "id_customer_creation", foreignKey = @ForeignKey(name = "fk_identification_creator"))
    private Customer customerCreation;

    @ManyToOne
    @JoinColumn(name = "id_customer_update", foreignKey = @ForeignKey(name = "fk_identification_updater"))
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

    public String getStrIdentification() {
        return strIdentification;
    }

    public void setStrIdentification(String strIdentification) {
        this.strIdentification = strIdentification;
    }

    public IdentificationType getType() {
        return type;
    }

    public void setType(IdentificationType type) {
        this.type = type;
    }

    public EntityStatus getStatus() {
        return status;
    }

    public void setStatus(EntityStatus status) {
        this.status = status;
    }

    public OrgUnit getOrgUnit() {
        return orgUnit;
    }

    public void setOrgUnit(OrgUnit orgUnit) {
        this.orgUnit = orgUnit;
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
