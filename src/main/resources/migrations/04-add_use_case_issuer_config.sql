
--add default config for USE_CASE
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('CASH_OUT_ACCOUNTING', 'ISSUER_USE_CASE_DEFAULT', 'CVA', 'STRING', 1, 0, GETDATE());

--add config for 174 USE_CASE
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('CASH_OUT_ACCOUNTING', 'ISSUER_USE_CASE_174', 'CVA', 'STRING', 1, 0, GETDATE());

--add config for 176 USE_CASE
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('CASH_OUT_ACCOUNTING', 'ISSUER_USE_CASE_176', 'CVA', 'STRING', 1, 0, GETDATE());

--add config for 177 USE_CASE
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('CASH_OUT_ACCOUNTING', 'ISSUER_USE_CASE_177', 'CQR', 'STRING', 1, 0, GETDATE());
