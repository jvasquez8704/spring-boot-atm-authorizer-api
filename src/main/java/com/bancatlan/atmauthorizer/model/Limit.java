package com.bancatlan.atmauthorizer.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "limitClass")
public class Limit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", nullable = true, length = 50)
    private String description;

    @Column(name = "isIndividual", nullable = true)
    private Boolean isIndividual;

    @Column(name = "dailyDebitLimit", nullable = true)
    private Double dailyDebitLimit;

    @Column(name = "weeklyDebitLimit", nullable = true)
    private Double weeklyDebitLimit;

    @Column(name = "monthlyDebitLimit", nullable = true)
    private Double monthlyDebitLimit;

    @Column(name = "absoluteDebitLimit", nullable = true)
    private Double absoluteDebitLimit;

    @Column(name = "dailyCreditLimit", nullable = true)
    private Double dailyCreditLimit;

    @Column(name = "weeklyCreditLimit", nullable = true)
    private Double weeklyCreditLimit;

    @Column(name = "monthlyCreditLimit", nullable = true)
    private Double monthlyCreditLimit;

    @Column(name = "absoluteCreditLimit", nullable = true)
    private Double absoluteCreditLimit;

    @Column(name = "amountDailyDebitLimit", nullable = true)
    private Double amountDailyDebitLimit;

    @Column(name = "amountWeeklyDebitLimit", nullable = true)
    private Double amountWeeklyDebitLimit;

    @Column(name = "amountMonthlyDebitLimit", nullable = true)
    private Double amountMonthlyDebitLimit;

    @Column(name = "amountAbsoluteDebitLimit", nullable = true)
    private Double amountAbsoluteDebitLimit;

    @Column(name = "amountSingleDebitLimit", nullable = true)
    private Double amountSingleDebitLimit;

    @Column(name = "amountSingleDebitMinimum", nullable = true)
    private Double amountSingleDebitMinimum;

    @Column(name = "amountDailyCreditLimit", nullable = true)
    private Double amountDailyCreditLimit;

    @Column(name = "amountWeeklyCreditLimit", nullable = true)
    private Double amountWeeklyCreditLimit;

    @Column(name = "amountMonthlyCreditLimit", nullable = true)
    private Double amountMonthlyCreditLimit;

    @Column(name = "amountAbsoluteCreditLimit", nullable = true)
    private Double amountAbsoluteCreditLimit;

    @Column(name = "amountSingleCreditLimit", nullable = true)
    private Double amountSingleCreditLimit;

    @Column(name = "amountSingleCreditMinimum", nullable = true)
    private Double amountSingleCreditMinimum;

    /*relations*/
    @Column(name = "isActive", nullable = true)
    private Boolean isActive;

    @Column(name = "isCanceled", nullable = true)
    private Boolean isCanceled;

    @Column(name = "isDeleted", nullable = true)
    private Boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "id_currency", nullable = true, foreignKey = @ForeignKey(name = "fk_limit_class_currency"))
    private Currency currency;

    /*metadata*/
    @Column(name = "creationDate", nullable = true, length = 30)
    private LocalDateTime creationDate;

    @Column(name = "updateDate", nullable = true, length = 30)
    private LocalDateTime updateDate;

    @ManyToOne
    @JoinColumn(name = "id_customer_creation", nullable = true, foreignKey = @ForeignKey(name = "fk_limit_class_creator"))
    private Customer customerCreation;

    @ManyToOne
    @JoinColumn(name = "id_customer_update", nullable = true, foreignKey = @ForeignKey(name = "fk_limit_class_updater"))
    private Customer customerUpdate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIndividual() {
        return isIndividual;
    }

    public void setIndividual(Boolean individual) {
        isIndividual = individual;
    }

    public Double getDailyDebitLimit() {
        return dailyDebitLimit;
    }

    public void setDailyDebitLimit(Double dailyDebitLimit) {
        this.dailyDebitLimit = dailyDebitLimit;
    }

    public Double getWeeklyDebitLimit() {
        return weeklyDebitLimit;
    }

    public void setWeeklyDebitLimit(Double weeklyDebitLimit) {
        this.weeklyDebitLimit = weeklyDebitLimit;
    }

    public Double getMonthlyDebitLimit() {
        return monthlyDebitLimit;
    }

    public void setMonthlyDebitLimit(Double monthlyDebitLimit) {
        this.monthlyDebitLimit = monthlyDebitLimit;
    }

    public Double getAbsoluteDebitLimit() {
        return absoluteDebitLimit;
    }

    public void setAbsoluteDebitLimit(Double absoluteDebitLimit) {
        this.absoluteDebitLimit = absoluteDebitLimit;
    }

    public Double getDailyCreditLimit() {
        return dailyCreditLimit;
    }

    public void setDailyCreditLimit(Double dailyCreditLimit) {
        this.dailyCreditLimit = dailyCreditLimit;
    }

    public Double getWeeklyCreditLimit() {
        return weeklyCreditLimit;
    }

    public void setWeeklyCreditLimit(Double weeklyCreditLimit) {
        this.weeklyCreditLimit = weeklyCreditLimit;
    }

    public Double getMonthlyCreditLimit() {
        return monthlyCreditLimit;
    }

    public void setMonthlyCreditLimit(Double monthlyCreditLimit) {
        this.monthlyCreditLimit = monthlyCreditLimit;
    }

    public Double getAbsoluteCreditLimit() {
        return absoluteCreditLimit;
    }

    public void setAbsoluteCreditLimit(Double absoluteCreditLimit) {
        this.absoluteCreditLimit = absoluteCreditLimit;
    }

    public Double getAmountDailyDebitLimit() {
        return amountDailyDebitLimit;
    }

    public void setAmountDailyDebitLimit(Double amountDailyDebitLimit) {
        this.amountDailyDebitLimit = amountDailyDebitLimit;
    }

    public Double getAmountWeeklyDebitLimit() {
        return amountWeeklyDebitLimit;
    }

    public void setAmountWeeklyDebitLimit(Double amountWeeklyDebitLimit) {
        this.amountWeeklyDebitLimit = amountWeeklyDebitLimit;
    }

    public Double getAmountMonthlyDebitLimit() {
        return amountMonthlyDebitLimit;
    }

    public void setAmountMonthlyDebitLimit(Double amountMonthlyDebitLimit) {
        this.amountMonthlyDebitLimit = amountMonthlyDebitLimit;
    }

    public Double getAmountAbsoluteDebitLimit() {
        return amountAbsoluteDebitLimit;
    }

    public void setAmountAbsoluteDebitLimit(Double amountAbsoluteDebitLimit) {
        this.amountAbsoluteDebitLimit = amountAbsoluteDebitLimit;
    }

    public Double getAmountSingleDebitLimit() {
        return amountSingleDebitLimit;
    }

    public void setAmountSingleDebitLimit(Double amountSingleDebitLimit) {
        this.amountSingleDebitLimit = amountSingleDebitLimit;
    }

    public Double getAmountSingleDebitMinimum() {
        return amountSingleDebitMinimum;
    }

    public void setAmountSingleDebitMinimum(Double amountSingleDebitMinimum) {
        this.amountSingleDebitMinimum = amountSingleDebitMinimum;
    }

    public Double getAmountDailyCreditLimit() {
        return amountDailyCreditLimit;
    }

    public void setAmountDailyCreditLimit(Double amountDailyCreditLimit) {
        this.amountDailyCreditLimit = amountDailyCreditLimit;
    }

    public Double getAmountWeeklyCreditLimit() {
        return amountWeeklyCreditLimit;
    }

    public void setAmountWeeklyCreditLimit(Double amountWeeklyCreditLimit) {
        this.amountWeeklyCreditLimit = amountWeeklyCreditLimit;
    }

    public Double getAmountMonthlyCreditLimit() {
        return amountMonthlyCreditLimit;
    }

    public void setAmountMonthlyCreditLimit(Double amountMonthlyCreditLimit) {
        this.amountMonthlyCreditLimit = amountMonthlyCreditLimit;
    }

    public Double getAmountAbsoluteCreditLimit() {
        return amountAbsoluteCreditLimit;
    }

    public void setAmountAbsoluteCreditLimit(Double amountAbsoluteCreditLimit) {
        this.amountAbsoluteCreditLimit = amountAbsoluteCreditLimit;
    }

    public Double getAmountSingleCreditLimit() {
        return amountSingleCreditLimit;
    }

    public void setAmountSingleCreditLimit(Double amountSingleCreditLimit) {
        this.amountSingleCreditLimit = amountSingleCreditLimit;
    }

    public Double getAmountSingleCreditMinimum() {
        return amountSingleCreditMinimum;
    }

    public void setAmountSingleCreditMinimum(Double amountSingleCreditMinimum) {
        this.amountSingleCreditMinimum = amountSingleCreditMinimum;
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
