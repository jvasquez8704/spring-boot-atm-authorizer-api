
--add default config for ACCOUNTING_TRANSFERS_CHANNEL_ID
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('ACCOUNTING_TRANSFERS_CHANNEL_ID', 'ACCOUNTING_TRANSFERS_CHANNEL_ID_DEFAULT  ', '108', 'STRING', 1, 0, GETDATE());

--add config for 174 ACCOUNTING_TRANSFERS_CHANNEL_ID
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('ACCOUNTING_TRANSFERS_CHANNEL_ID', 'ACCOUNTING_TRANSFERS_CHANNEL_ID_174', '108', 'STRING', 1, 0, GETDATE());

--add config for 176 ACCOUNTING_TRANSFERS_CHANNEL_ID
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('ACCOUNTING_TRANSFERS_CHANNEL_ID', 'ACCOUNTING_TRANSFERS_CHANNEL_ID_176', '107', 'STRING', 1, 0, GETDATE());

--add config for 177 ACCOUNTING_TRANSFERS_CHANNEL_ID
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('ACCOUNTING_TRANSFERS_CHANNEL_ID', 'ACCOUNTING_TRANSFERS_CHANNEL_ID_177', '108', 'STRING', 1, 0, GETDATE());
