
--add default config for ACCOUNTING_TRANSFERS
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('ACCOUNTING_TRANSFERS', 'ACCOUNTING_TRANSFERS_ID_DEFAULT ', '100408', 'STRING', 1, 0, GETDATE());

--add config for 174 ACCOUNTING_TRANSFERS
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('ACCOUNTING_TRANSFERS', 'ACCOUNTING_TRANSFERS_ID_174', '100408', 'STRING', 1, 0, GETDATE());

--add config for 176 ACCOUNTING_TRANSFERS
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('ACCOUNTING_TRANSFERS', 'ACCOUNTING_TRANSFERS_ID_176', '100408', 'STRING', 1, 0, GETDATE());

--add config for 177 ACCOUNTING_TRANSFERS
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('ACCOUNTING_TRANSFERS', 'ACCOUNTING_TRANSFERS_ID_177', '100409', 'STRING', 1, 0, GETDATE());
