package com.bancatlan.atmauthorizer.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = true, length = 50)
    private String name;

    @Column(name = "email", nullable = true, length = 70)
    private String email;

    @Column(name = "msisdn", nullable = true, length = 20)
    private String msisdn;

    @Column(name = "username", nullable = true, length = 25)
    private String username;

    @Column(name = "birthday", nullable = true, length = 30)
    private LocalDateTime birthday;

    /*Associations*/
    @ManyToOne
    @JoinColumn(name = "id_cancellationReason", nullable = true, foreignKey = @ForeignKey(name = "fk_customer_cancellation_reason"))
    private CancellationReason cancellationReason;

    @ManyToOne
    @JoinColumn(name = "id_blackListReason", nullable = true, foreignKey = @ForeignKey(name = "fk_customer_black_list_reason"))
    private BlackListReason blackListReason;

    @ManyToOne
    @JoinColumn(name = "id_customer_type", nullable = true, foreignKey = @ForeignKey(name = "fk_customer_type"))
    private CustomerType customerType;

    @ManyToOne
    @JoinColumn(name = "id_parent", nullable = true, foreignKey = @ForeignKey(name = "fk_customer_parent"))
    private Customer parent;

    @ManyToOne
    @JoinColumn(name = "id_language", nullable = true, foreignKey = @ForeignKey(name = "fk_customer_language"))
    private Language language;

    @ManyToOne
    @JoinColumn(name = "id_country", nullable = true, foreignKey = @ForeignKey(name = "fk_customer_country"))
    private Country country;

    @ManyToOne
    @JoinColumn(name = "id_notification_mode", nullable = true, foreignKey = @ForeignKey(name = "fk_customer_notification_mode"))
    private NotificationMode  notificationMode;

    /*
     RiskCategory riskCategory;
     FeeSet
     LimitSet
     TimeZone
     ReferralCustomer
    */

    /*Metadata*/
    @Column(name = "isActive", nullable = true)
    private Boolean isActive;

    @Column(name = "isCanceled", nullable = true)
    private Boolean isCanceled;

    @Column(name = "isDeleted", nullable = true)
    private Boolean isDeleted;

    @Column(name = "isTest", nullable = true)
    private Boolean isTest;

    @Column(name = "creationDate", nullable = true, length = 30)
    private LocalDateTime creationDate;

    @Column(name = "updateDate", nullable = true, length = 30)
    private LocalDateTime updateDate;

    @ManyToOne
    @JoinColumn(name = "id_customer_creation", nullable = true, foreignKey = @ForeignKey(name = "fk_customer_creator"))
    private Customer customerCreation;

    @ManyToOne
    @JoinColumn(name = "id_customer_update", nullable = true, foreignKey = @ForeignKey(name = "fk_customer_updater"))
    private Customer customerUpdate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDateTime birthday) {
        this.birthday = birthday;
    }

    public CancellationReason getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(CancellationReason cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public BlackListReason getBlackListReason() {
        return blackListReason;
    }

    public void setBlackListReason(BlackListReason blackListReason) {
        this.blackListReason = blackListReason;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }

    public Customer getParent() {
        return parent;
    }

    public void setParent(Customer parent) {
        this.parent = parent;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public NotificationMode getNotificationMode() {
        return notificationMode;
    }

    public void setNotificationMode(NotificationMode notificationMode) {
        this.notificationMode = notificationMode;
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

    public Boolean getTest() {
        return isTest;
    }

    public void setTest(Boolean test) {
        isTest = test;
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
