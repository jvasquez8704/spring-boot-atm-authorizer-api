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

    @Column(name = "soported_values")
    private String soported_values;

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

}
