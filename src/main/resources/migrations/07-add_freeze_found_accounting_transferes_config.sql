
--add default config for FREEZE_FOUND_ACCOUNTING_TRANSFERS
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('FREEZE_FOUND_ACCOUNTING_TRANSFERS', 'FREEZE_FOUND_ACCOUNTING_TRANSFERS_ID_DEFAULT', '100347', 'STRING', 1, 0, GETDATE());

--add config for 174 FREEZE_FOUND_ACCOUNTING_TRANSFERS
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('FREEZE_FOUND_ACCOUNTING_TRANSFERS', 'FREEZE_FOUND_ACCOUNTING_TRANSFERS_ID_174', '100347', 'STRING', 1, 0, GETDATE());

--add config for 176 FREEZE_FOUND_ACCOUNTING_TRANSFERS
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('FREEZE_FOUND_ACCOUNTING_TRANSFERS', 'FREEZE_FOUND_ACCOUNTING_TRANSFERS_ID_176', '100347', 'STRING', 1, 0, GETDATE());

--add config for 177 FREEZE_FOUND_ACCOUNTING_TRANSFERS
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('FREEZE_FOUND_ACCOUNTING_TRANSFERS', 'FREEZE_FOUND_ACCOUNTING_TRANSFERS_ID_177', '100410', 'STRING', 1, 0, GETDATE());
