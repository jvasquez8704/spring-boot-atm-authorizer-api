
--MIGRATIONS FOR NEW STATUSES
INSERT INTO authorizerdb.dbo.txn_status (id, creation_date, description, is_active, is_canceled, is_deleted, title, update_date, id_customer_creation, id_customer_update)
VALUES(21,GETDATE(), 'status for new job, the goal is re-precess txns with freeze errors', 0, 0, 0, 'Pre - cancelled', null, null, null);

--FORCE SET ID ON INSERTS
--SET IDENTITY_INSERT dbo.use_case OFF;
--SET IDENTITY_INSERT dbo.txn_status OFF;
