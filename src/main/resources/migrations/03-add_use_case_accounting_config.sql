
--add default config for USE_CASE
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('CASH_OUT_ACCOUNTING', 'ACCOUNT_USE_CASE_DEFAULT', '750099999659', 'STRING', 1, 0, GETDATE());

--add config for 174 USE_CASE
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('CASH_OUT_ACCOUNTING', 'ACCOUNT_USE_CASE_174', '750099999659', 'STRING', 1, 0, GETDATE());

--add config for 176 USE_CASE
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('CASH_OUT_ACCOUNTING', 'ACCOUNT_USE_CASE_176', '750099999659', 'STRING', 1, 0, GETDATE());

--add config for 177 USE_CASE
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('CASH_OUT_ACCOUNTING', 'ACCOUNT_USE_CASE_177', '750099900540', 'STRING', 1, 0, GETDATE());
