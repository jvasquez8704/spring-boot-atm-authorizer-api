package com.bancatlan.atmauthorizer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "txn")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", nullable = true, length = 300)
    private String description;

    @Column(name = "amount", nullable = false, length = 20)
    private Double amount;

    @Column(name = "atmReference", nullable = true, length = 50)
    private String atmReference;

    @Column(name = "citizenIdentity", nullable = true, length = 50)
    private String citizenIdentity;

    @Column(name = "coreReference", nullable = true, length = 50)
    private String coreReference;

    @Column(name = "integrationBusReference", nullable = true, length = 50)
    private String integrationBusReference;

    @Column(name = "channelReference", nullable = true, length = 50)
    private String channelReference;

    @Column(name = "channelId", nullable = true, length = 50)
    private String channelId;

    @Column(name = "applicationId", nullable = true, length = 50)
    private String applicationId;

    @Column(name = "id_order", nullable = true, length = 30)
    private String id_order;

    /*Associations*/
    @ManyToOne
    @JoinColumn(name = "id_use_case", nullable = true, foreignKey = @ForeignKey(name = "fk_txn_use_case"))
    private UseCase useCase;

    @ManyToOne
    @JoinColumn(name = "id_status", nullable = true, foreignKey = @ForeignKey(name = "fk_txn_status"))
    private TxnStatus txnStatus;

    @ManyToOne
    @JoinColumn(name = "id_error", nullable = true, foreignKey = @ForeignKey(name = "fk_txn_error"))
    private Error error;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_voucher", nullable = true, foreignKey = @ForeignKey(name = "fk_txn_voucher"))
    private Voucher voucher;/*This reference is for to list all transaction related with a voucher*/

    @ManyToOne
    @JoinColumn(name = "id_currency", nullable = true, foreignKey = @ForeignKey(name = "fk_txn_currency"))
    private Currency currency;

    @ManyToOne
    @JoinColumn(name = "id_payer", nullable = true, foreignKey = @ForeignKey(name = "fk_payer_txn"))
    private Customer payer;

    @ManyToOne
    @JoinColumn(name = "id_payee", nullable = true, foreignKey = @ForeignKey(name = "fk_payee_txn"))
    private Customer payee;

    @ManyToOne
    @JoinColumn(name = "id_payer_pi", nullable = true, foreignKey = @ForeignKey(name = "fk_payer_pi_txn"))
    private PaymentInstrument payerPaymentInstrument;

    @ManyToOne
    @JoinColumn(name = "id_payee_pi", nullable = true, foreignKey = @ForeignKey(name = "fk_payee_pi_txn"))
    private PaymentInstrument payeePaymentInstrument;

    @ManyToOne
    @JoinColumn(name = "id_channel", nullable = true, foreignKey = @ForeignKey(name = "fk_channel_txn"))
    private Channel channel;

    @Column(name = "expirationDate", nullable = true, length = 30)
    private LocalDateTime expirationDate;

    /*metadata*/

    @Column(name = "creationDate", nullable = true, length = 30)
    private LocalDateTime creationDate;

    @Column(name = "updateDate", nullable = true, length = 30)
    private LocalDateTime updateDate;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_customer_creation", nullable = true, foreignKey = @ForeignKey(name = "fk_txn_customer_c"))
    private Customer customerCreation;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_customer_update", nullable = true, foreignKey = @ForeignKey(name = "fk_txn_customer_u"))
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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getAtmReference() {
        return atmReference;
    }

    public void setAtmReference(String atmReference) {
        this.atmReference = atmReference;
    }

    public String getCitizenIdentity() {
        return citizenIdentity;
    }

    public void setCitizenIdentity(String citizenIdentity) {
        this.citizenIdentity = citizenIdentity;
    }

    public String getCoreReference() {
        return coreReference;
    }

    public void setCoreReference(String coreReference) {
        this.coreReference = coreReference;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getIntegrationBusReference() {
        return integrationBusReference;
    }

    public void setIntegrationBusReference(String integrationBusReference) {
        this.integrationBusReference = integrationBusReference;
    }

    public String getChannelReference() {
        return channelReference;
    }

    public void setChannelReference(String channelReference) {
        this.channelReference = channelReference;
    }

    public String getId_order() {
        return id_order;
    }

    public void setId_order(String id_order) {
        this.id_order = id_order;
    }

    public UseCase getUseCase() {
        return useCase;
    }

    public void setUseCase(UseCase useCase) {
        this.useCase = useCase;
    }

    public TxnStatus getTxnStatus() {
        return txnStatus;
    }

    public void setTxnStatus(TxnStatus txnStatus) {
        this.txnStatus = txnStatus;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Customer getPayer() {
        return payer;
    }

    public void setPayer(Customer payer) {
        this.payer = payer;
    }

    public Customer getPayee() {
        return payee;
    }

    public void setPayee(Customer payee) {
        this.payee = payee;
    }

    public PaymentInstrument getPayerPaymentInstrument() {
        return payerPaymentInstrument;
    }

    public void setPayerPaymentInstrument(PaymentInstrument payerPaymentInstrument) {
        this.payerPaymentInstrument = payerPaymentInstrument;
    }

    public PaymentInstrument getPayeePaymentInstrument() {
        return payeePaymentInstrument;
    }

    public void setPayeePaymentInstrument(PaymentInstrument payeePaymentInstrument) {
        this.payeePaymentInstrument = payeePaymentInstrument;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
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
