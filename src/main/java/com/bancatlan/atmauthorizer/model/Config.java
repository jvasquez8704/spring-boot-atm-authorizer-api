package com.bancatlan.atmauthorizer.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "config")
public class Config {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "description", length = 50)
    private String description;

    @Column(name = "property_name", length = 100)
    private String propertyName;

    @Column(name = "property_value")
    private String propertyValue;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "supported_values")
    private String supported_values;

    @Column(name = "data_type", length = 25)
    private String data_type;

    @ManyToOne
    @JoinColumn(name = "id_service", foreignKey = @ForeignKey(name = "fk_config_service"))
    private Service service;

    @Column(name = "service_path")
    private String servicePath;

    @Column(name = "is_read_only")
    private Boolean isReadOnly;

    @Column(name = "is_validation_required")
    private Boolean isValidationRequired;

    @Column(name = "is_encryption_required")
    private Boolean isEncryptionRequired;

    @Column(name = "is_restart_required")
    private Boolean isRestartRequired;

    /*Metadata*/
    @Column(name = "isActive")
    private Boolean isActive;

    @Column(name = "isDeleted")
    private Boolean isDeleted;

    @Column(name = "creationDate", length = 30)
    private LocalDateTime creationDate;

    @Column(name = "updateDate", length = 30)
    private LocalDateTime updateDate;

    @ManyToOne
    @JoinColumn(name = "id_customer_creation", foreignKey = @ForeignKey(name = "fk_config_creator"))
    private Customer customerCreation;

    @ManyToOne
    @JoinColumn(name = "id_customer_update", foreignKey = @ForeignKey(name = "fk_config_updater"))
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getSupported_values() {
        return supported_values;
    }

    public void setSupported_values(String supported_values) {
        this.supported_values = supported_values;
    }

    public String getData_type() {
        return data_type;
    }

    public void setData_type(String data_type) {
        this.data_type = data_type;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public Boolean getReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        isReadOnly = readOnly;
    }

    public Boolean getValidationRequired() {
        return isValidationRequired;
    }

    public void setValidationRequired(Boolean validationRequired) {
        isValidationRequired = validationRequired;
    }

    public Boolean getEncryptionRequired() {
        return isEncryptionRequired;
    }

    public void setEncryptionRequired(Boolean encryptionRequired) {
        isEncryptionRequired = encryptionRequired;
    }

    public Boolean getRestartRequired() {
        return isRestartRequired;
    }

    public void setRestartRequired(Boolean restartRequired) {
        isRestartRequired = restartRequired;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
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
