
--add URL for privilege
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('URL_SERVICE_PRIVILEGE', 'URL_SERVICE_PRIVILEGE', 'http://localhost:8081', 'STRING', 1, 0, GETDATE());

--add validation if it is a scheduled savings account
-- 1 is enable and 0 is disable
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('IS_SCHEDULED_SAVINGS_ACCOUNTS', 'IS_SCHEDULED_SAVINGS_ACCOUNTS', '0', 'STRING', 1, 0, GETDATE());

--add activation or deactivation of the privilege service
-- 1 is enable and 0 is disable
INSERT INTO authorizerdb.dbo.config
(name, property_name, property_value, data_type, is_active, is_deleted, creation_date)
VALUES('IS_PRIVILEGE_VALIDATION_ACTIVE', 'IS_PRIVILEGE_VALIDATION_ACTIVE', '0', 'STRING', 1, 0, GETDATE());

