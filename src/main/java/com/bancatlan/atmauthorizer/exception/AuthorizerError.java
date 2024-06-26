package com.bancatlan.atmauthorizer.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AuthorizerError implements IError {
    /**
    * ESB SUFFIX MINTS ENTERPRISE SERVICE BUS
     * SERVICE SUFFIX TO BANK SERVICES
    * */
    OK(0,"Success"),
    MALFORMED_URL(1,"URL Malformed"),
    SERVICE_ERROR(2,"Service Error"),
    BINDING_ERROR(3,"Binding error"),
    REMOTE_ERROR(4,"Remote error"),
    LOGIN_ERROR(5,"Login into Entrust Failed"),
    ADMIN_LOGIN_ERROR(6,"Login into Entrust failed"),
    ADMIN_LOGOUT_ERROR(7,"LogOut failed!"),
    ADMIN_USER_LIST_ERROR(8,"Error when retrieving the user list"),
    ADMIN_CREATE_USER_ERROR(9,"Error when creating the user"),
    GUIP_TXN_NOT_FOUND(10,"Mymo Txn not found"),
    VOUCHER_ALREADY_INACTIVE_ERROR(11,"Voucher is already inactive or so"),
    UNEXPECTED_ERROR(12,"Error en el servicio, por favor intenta de nuevo, si el problema persiste contacta nuestro call center."),/*Unexpected exception*/
    DUPLICATE_USER_ERROR(13,"More than one user found with the same id, cannot continue"),
    MISSING_AMOUNT_TO_TRANSFER_FIELD(14,"Seleccione monto."),
    EXTERNAL_USER_DATA_ERROR(15,"Error when recovering the user data"),
    EXTERNAL_SMS_ERROR(16,"Basa Custom error when tried sending the sms"),
    NOT_SUPPORT_USE_CASE(17,"Use case not support"),
    EXTERNAL_USER_DATA_NOT_FOUND_ERROR(18,"Data not defined for the user"),
    PARSING_ERROR_VALIDATING_COMMUNICATION_COMPANY(19,"Error validando compañia telefónica."),
    ENCRYPT_ERROR(20,"Error when generating the encryption"),
    NOT_FOUND_BANK_PAYMENT_SERVICE_ACTION(21,"The action not found in request"),
    FILE_NOT_FOUND(22,"Properties file not found"),
    IO_EXCEPTION(23,"I/O Exception when recovering the properties file"),
    REQUEST_NULL(24,"The request cannot be null"),
    MISSING_CONFIRM_TARGET_TELEPHONE_FIELD(25,"Confirma teléfono destino."),
    MISSING_TARGET_TELEPHONE_FIELD(26,"Ingresa teléfono destino."),
    NOT_MATCH_CONFIRM_TARGET_TELEPHONE(27,"Campos de teléfono no coinciden."),
    BAD_FORMAT_TARGET_TELEPHONE(28,"Favor validar los campos de número de teléfono. Debe cumplir con valores númericos de 8 dígitos."),
    MISSING_WRITTEN_AMOUNT(29,"Ingresa el monto."),
    NOT_MATCH_AMOUNT_WITH_ATM(30,"Los montos deben ser múltiplos de LPS. 100, máximo LPS.5,000."),
    USER_NOT_EXIST(31,"The user does not exist"),
    MISSING_SECRET_CODE_FIELD(32,"Ingresa clave de seguridad."),
    BAD_FORMAT_SECRET_CODE(33,"El campo clave de retiro debe ser un número de 4 dígitos."),
    REQUEST_UNLOCK_USER_ERROR(34,"The unlock user code cannot be null or empty"),
    CUSTOM_ERROR_NOT_SUPPORTED_COMMUNICATION_COMPANY(35,"Campos de teléfono deben iniciar con 3, 7, 8 y 9."),
    MISSING_PAYER_MSISDN(36,"Actualiza el número móvil de tu banca en línea, contacta nuestro call center."),/*Todo: error 36 => system message: Payer msisdn it's missed */
    BAD_FORMAT_PAYER_MSISDN(37,"Actualiza el número móvil de tu banca en línea, contacta nuestro call center."),/*Todo: error 37 => system message: Payer msisdn it's bad format */
    REQUEST_PASSWORD_NULL(38,"The user password cannot be empty or null"),
    ADMIN_SYNC_ERROR(39,"Error when syncronizing the user token"),
    REQUEST_SYNC_NULL(40,"The syncronization request cannot be null"),
    CUSTOM_ERROR_NOT_VALID_ACCOUNT_TYPE(41,"Selecciona una cuenta."),
    PARSING_ERROR_VALIDATING_INSUFFICIENT_FUNDS(42,"Error al validar la cuenta seleccionada."),
    CUSTOM_ERROR_INSUFFICIENT_FUNDS(43,"Fondos insuficientes en la cuenta seleccionada."),
    CUSTOM_ERROR_NOT_VALID_ACCOUNT_STATUS(44,"Selecciona una cuenta valida."),
    AMOUNT_KEY_DOES_NOT_EXIST(45,"Error getting amount key, this key does not exist"),
    MALFORMED_URL_GETTING_ACCOUNT_INFORMATION_ESB(46,"URL Malformed when trying to get account info at esb"),
    UNEXPECTED_ERROR_GETTING_ACCOUNT_INFORMATION_ESB(47,"Unexpected error when trying to get account info at esb"),
    VOUCHER_NOT_FOUND(48,"Error when find voucher, it seems voucher does not exist"),
    /*PAYER_DAILY_DEBIT_LIMIT_EXCEEDED(49,"payer exceeded daily limit"),
    PAYEE_DAILY_DEBIT_LIMIT_EXCEEDED(50,"payee exceeded daily limit"),
    PAYER_MONTHLY_DEBIT_LIMIT_EXCEEDED(51,"payer exceeded monthly limit"),
    PAYEE_MONTHLY_DEBIT_LIMIT_EXCEEDED(52,"payee exceeded monthly limit"),
    PAYER_WEEKLY_DEBIT_LIMIT_EXCEEDED(53,"payer exceeded weekly limit"),
    PAYEE_WEEKLY_DEBIT_LIMIT_EXCEEDED(54,"payee exceeded weekly limit"),*/
    PAYER_DAILY_DEBIT_LIMIT_EXCEEDED(49,"Has alcanzado el límite de envíos diarios."),
    PAYEE_DAILY_CREDIT_LIMIT_EXCEEDED(50,"Tu beneficiario ha alcanzado el límite de envíos diarios."),
    PAYER_MONTHLY_DEBIT_LIMIT_EXCEEDED(51,"Has alcanzado el límite de envíos mensuales."),
    PAYEE_MONTHLY_CREDIT_LIMIT_EXCEEDED(52,"Tu beneficiario ha alcanzado el límite de envíos mensuales."),
    PAYER_WEEKLY_DEBIT_LIMIT_EXCEEDED(53,"Has alcanzado el límite de envíos semanales."),
    PAYEE_WEEKLY_CREDIT_LIMIT_EXCEEDED(54,"Tu beneficiario ha alcanzado el límite de envíos semanales."),
    VERIFYING_PARTICIPANTS(55,"Unexpected error when checking participants"),
    AMOUNT_SINGLE_DEBIT_LIMIT_EXCEEDED(56,"txn exceeded amount single debit limit"),
    AMOUNT_SINGLE_CREDIT_LIMIT_EXCEEDED(57,"txn exceeded amount single credit limit"),
    AMOUNT_SINGLE_DEBIT_MINIMUM_EXCEEDED(58,"txn exceeded amount single debit minimum"),
    AMOUNT_SINGLE_CREDIT_MINIMUM_EXCEEDED(59,"txn exceeded amount single credit minimum"),
    ERROR_ON_VERIFY(60,"txn got and error on verify"),
    NOT_PROPERLY_CONFIGURATION_ON_PAYER(61,"Payer is not configured properly, please check it."),
    NOT_PROPERLY_CONFIGURATION_ON_PAYEE(62,"Payee is not configured properly, please check it."),
    NOT_CONFIGURATION_LIMITS_ON_CST_TYPE_PAYER(63,"Limits on payer customer type not properly configured, please check it."),
    NOT_CONFIGURATION_LIMITS_ON_CST_TYPE_PAYEE(64,"Limits on payer customer type not properly configured, please check it."),
    NOT_FOUND_PAYEE_ATM(65,"Payer not found for ATM"),
    NOT_FOUND_PI(66,"Payer PI not found"),
    MISSING_PAYER_PI(67,"Payer not found in request"),
    MISSING_OCB_USER(68,"OCB USER not found in request"),
    MISSING_ACCOUNT_FROM_BANK(69,"Any account not found in Bank"),
    ERROR_ACCOUNTING_TRANSFER_FROM_BANK(70,"Error invoking SIOSTransferenciaContable service"),
    MISSING_OCB_SESSION_KEY(71,"Ocb user session key is required"),
    CUSTOM_ERROR_GETTING_ACCOUNTS_SERVICE(72,"Custom error getting accounts when esb got the service response"),
    EXP_ERROR_PO_GETTING_ACCOUNTS(73,"Error getting accounts when esb call was executed"),
    MISSING_TXN_ON_REQUEST(74,"Error getting confirm request transaction or id transaction is missing"),
    MISSING_TXN_DOES_NOT_EXIST(75,"Error finding transaction, it seems it does not exist"),
    CUSTOM_ERROR_ACCOUNTING_TRANSFER_ESB(76, "Custom error accounting transfer when esb got the service response"),
    NOT_FOUND_USE_CASE(76, "use case not found"),
    ENCRYPTED_CODE_ERROR(77, "The code is incorrect for this user"),
    NOT_SUPPORTED_VOUCHER_ACTION(92, "action or mti not supported"),
    NOT_SUPPORTED_TXN_ID_BANK_PAYMENT_SERVICE_ACTION(79, "This action doest not support an txn id"),
    ALREADY_PROCESSED_TXN(80, "Transaction already processed"),
    NOT_FOUND_CURRENCY_IN_REQ(81, "Currency not found in request"),
    NOT_FOUND_CURRENCY(82, "Currency not found"),
    ERROR_SERVICE_BANK_FREEZE(83,"Error invoking SIOSCongelamientoCuentas service"),
    CUSTOM_ERROR_SERVICE_BANK_FREEZE(84,"Custom error in response SIOSCongelamientoCuentas service"),
    NOT_SUPPORT_CURRENCY_DIFFERENT_HN(85, "Selecciona una cuenta en LPS."),
    NOT_HAVE_PRIVILEGE_TO_USE_THIS_ACCOUNT(203,"Not have privilege to use this account")
    ;


    private Integer code;
    private String message;

    /**
     * Constructor generico. Recibe el codigo y la descripcion del enum
     * @param code codigo
     * @param message mensaje
     */
    AuthorizerError(final Integer code, final String message){
        this.code = code;
        this.message = message;
    }

    /**
     * Obtiene el codigo del enum
     * @return el codigo
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Define el codigo del enum
     * @param code el nuevo codigo
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * Obtiene el mensaje del enum
     * @return el mensaje
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Define el mensaje del enum
     * @param message el nuevo mensaje
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Retorna el codigo y descripcion del enum para impresion en logs
     * @return el string con la informacion
     */
    @Override
    public String toString(){
        return getCode() + "|" + getMessage();
    }
}
