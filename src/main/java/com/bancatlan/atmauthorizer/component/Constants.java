package com.bancatlan.atmauthorizer.component;

public class Constants {
    /*Error messages*/
    public static final String CUSTOM_MESSAGE_ERROR = "Custom handle error";
    public static final String MODEL_NOT_FOUND_MESSAGE_ERROR = "Model not found";
    public static final String PARAMETER_NOT_FOUND_MESSAGE_ERROR = "Parameter not found";
    public static final String GENERIC_ERROR_STATUS_CODE = "9999";
    public static final String GENERIC_EXCEPTION_TYPE = "GENERIC_EXCEPTION";
    public static final String CUSTOM_EXCEPTION_TYPE = "CUSTOM_EXCEPTION_TYPE";
    public static final String BANK_EXCEPTION_TYPE = "BANK_EXCEPTION_TYPE";
    public static final String ATM_EXCEPTION_TYPE = "ATM_CUSTOM_EXCEPTION_TYPE";
    public static final String CUSTOM_BANK_EXCEPTION_TYPE = "CUSTOM_BANK_EXCEPTION_TYPE";
    public static final String NOT_FOUND_EXCEPTION_TYPE = "NOT_FOUND_EXCEPTION_TYPE";

    /*BASA SERVICES CONSTANTS*/
    public static final String BANK_SUCCESS_STATUS_CODE = "0000";
    public static final String BANK_SUCCESS_TYPE = "S1";
    public static final String ATM_ANOTHER_AMOUNT_KEY = "999";
    public static final String ATM_SUCCESS_STATUS_CODE = "00";
    public static final int INT_BANK_SUCCESS_STATUS_CODE = 0;
    public static final String BANK_SUCCESS_STATUS_TYPE = "S1";
    public static final String BANK_SUCCESS_STATUS_MESSAGE = "Satisfactorio";
    public static final String STR_QUESTION_MARK = "?";
    public static final String BANK_STR_MARK_TRUE = "S";
    public static final String BANK_STRING_ZERO= "";
    public static final String BANK_HN_CURRENCY = "LPS";
    public static final String BANK_CURRENCY_STATUS_ACTIVE = "A";
    public static final String HN_CURRENCY = "HNL";
    public static final String STR_VALUE_1 = "1";
    public static final String BANK_ACCOUNT_TYPE_1 = "1";
    public static final String BANK_ACCOUNT_TYPE_2 = "2";
    public static final String BANK_ACTION_CANCEL = "R";
    public static final String BANK_ACTION_VERIFY = "C";
    public static final String BANK_ACTION_FREEZE = "C";
    public static final String BANK_ACTION_GUIP = "N";
    public static final String BANK_ACTION_DEFROST = "D";
    public static final String BANK_ACTION_PAYMENT = "P";
    public static final String ITM_MTI_WITHDRAW = "0200";
    public static final String ITM_MTI_REVERSE_WITHDRAW = "0400";
    public static final String STR_CUSTOM_ERR = "-1";
    public static final String STR_EXCEPTION_ERR = "-2";
    public static final String STR_ZERO = "0";

    /*STATUS TXN CODES*/
    public static final long INITIAL_TXN_STATUS = 1;
    public static final long PRE_AUTHORIZED_TXN_STATUS = 2;
    public static final long AUTHENTICATED_TXN_STATUS = 10;
    public static final long AUTHORIZED_TXN_STATUS = 20;
    public static final long WAITING_AUTOMATIC_PROCESS_TXN_STATUS = 25;
    public static final long CONFIRM_TXN_STATUS = 30;
    public static final long CANCEL_CONFIRM_TXN_STATUS = 70;

    /*TXN USE CASE CODES*/
    public static final long VOUCHER_USE_CASE =  174;
    public static final long VOUCHER_USE_CASE_QR =  177;
    public static final long WITHDRAW_VOUCHER_USE_CASE =  800;
    public static final int INT_VOUCHER_USE_CASE =  174;
    public static final int INT_VOUCHER_USE_CASE_QR =  177;
    public static final int INT_CASH_OUT_KEYBOARD_USE_CASE =  176;
    public static final int INT_WITHDRAW_VOUCHER_USE_CASE =  800;
    public static final String STR_WITHDRAW_MYMO =  "WITHDRAWAL";

    /*CURRENCY CODES*/
    public static final long HN_CURRENCY_ID =  1;
    public static final long US_CURRENCY_ID =  2;

    /*COUNTRY CODES*/
    public static final String HN_COUNTRY2CODE =  "HN";
    public static final String HN_MSISDN_CODE = "+504";
    public static final String HND_MSISDN_CODE = "504";

    /*DEFAULT LIMIT CONFIGURED*/
    public static final long DEFAULT_LIMIT =  1;
    public static final int LENGTH_AUTH_CODE = 6;
    public static int SIZE_PICKUP_CODE = 4;
    public static int PIVOT_PICKUP_CODE_RANGE = 5;

    /*DEFAULT LIMIT CONFIGURED*/
    public static final String DAILY_RANGE = "DAILY_RANGE";
    public static final String MONTHLY_RANGE = "MONTHLY_RANGE";
    public static final String WEEKLY_RANGE = "WEEKLY_RANGE";

    /*ATM USER ID*/
    public static final long ATM_USER_ID = 1;
    public static final long CUSTOMER_TYPE_CONSUMER_ID = 2;
    public static final String ATM_USER_STR = "ATM_USER";

    /*PI TYPES*/
    public static final long PI_TYPE_BANK_ACCOUNT_ID =  40;

    /*IMPORT PI*/
    public static final long PI_ATM_USER_ID =  1;

    /*GENERIC MESSAGES*/
    public static final String MSG_ATM_TO_BANK_ACCOUNT =  "TRANSFER OCB USER ACCOUNT TO ATM AUTHORIZER BANK ACCOUNT";
    public static final String STR_ID_RETIRO_SIN_TARGETA =  "RETIRO_SIN_TARJETA";
    public static final String STR_ID_RETIRO_SIN_TARGETA_QR =  "RQR";
    public static final String STR_RTS_MYMO_CORE_DESC =  "RST Teclado Atlantida";
    public static final String STR_DASH_SEPARATOR =  "-";

    /*NOTIFICATION TYPES*/
    public static final String BANK_NOTIFICATION_SMS =  "SMS";
    public static final String BANK_NOTIFICATION_EMAIL =  "EMAIL";
    //public static String TEMPLATE_NOTIFICATION_SMS =  "Envio de dinero Atlantida Retiro sin tarjeta. Monto: L. %s Codigo de retiro %s";
    public static String TEMPLATE_NOTIFICATION_SMS =  "Retiro de dinero sin tarjeta Atlantida Monto: L. %s, codigo de beneficiario %s Aprende a utilizarlo en tutoriales.bancatlan.hn No compartas tu informacion personal";

    /**/
    public static final String STR_ACCOUNTING_TRANSFER_SERVICE_NAME = "Transferencia Contable";
    public static final String STR_FREEZE_SERVICE_NAME = "Congelamiento";
    public static final String STR_DEFROST_SERVICE_NAME = "RST-DEFROST";
    public static final String STR_NOTIFICATION_SERVICE_NAME = "Envio Notificacion";

    /*application Id*/
    public static final String OCB_APP_ID = "001";
    public static final String ID_MISSION_APP_ID = "2278";
    public static final String VOUCHER_BASA_APP_ID = "2272";

}
