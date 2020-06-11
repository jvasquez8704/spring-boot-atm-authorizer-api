package com.bancatlan.atmauthorizer.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "error")
public class Error {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = true, length = 50)
    private String title;

    @Column(name = "strCode", nullable = true, length = 50)
    private String strCode;

    @Column(name = "code", nullable = true, length = 50)
    private Long code;

    @Column(name = "description", nullable = true, length = 100)
    private String description;

    /*Metadata*/
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
    @JoinColumn(name = "id_customer_creation", nullable = true, foreignKey = @ForeignKey(name = "fk_error_creator"))
    private Customer customerCreation;

    @ManyToOne
    @JoinColumn(name = "id_customer_update", nullable = true, foreignKey = @ForeignKey(name = "fk_error_updater"))
    private Customer customerUpdate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStrCode() {
        return strCode;
    }

    public void setStrCode(String strCode) {
        this.strCode = strCode;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
