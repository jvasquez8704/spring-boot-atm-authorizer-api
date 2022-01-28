
--MIGRATIONS FOR QR AND KEYBOARD ATLANTIDA
INSERT INTO authorizerdb.dbo.use_case (id,title,is_active,is_canceled,is_deleted,creation_date) values (176,'WITHDRAW_CASH_OUT_KEYBOARD',1,0,0,GETDATE());
INSERT INTO authorizerdb.dbo.use_case (id,title,is_active,is_canceled,is_deleted,creation_date) values (177,'WITHDRAW_CASH_OUT_QR',1,0,0,GETDATE());

--MIGRATIONS FOR NEW STATUSES
INSERT INTO authorizerdb.dbo.txn_status (id, creation_date, description, is_active, is_canceled, is_deleted, title, update_date, id_customer_creation, id_customer_update) VALUES(26,GETDATE(), null, 0, 0, 0, 'Manually cancelled', null, null, null);
INSERT INTO authorizerdb.dbo.txn_status (id, creation_date, description, is_active, is_canceled, is_deleted, title, update_date, id_customer_creation, id_customer_update) VALUES(61,GETDATE(), null, 0, 0, 0, 'Pre - cancelled', null, null, null);

--FORCE SET ID ON INSERTS
--SET IDENTITY_INSERT dbo.use_case OFF;
--SET IDENTITY_INSERT dbo.txn_status OFF;
