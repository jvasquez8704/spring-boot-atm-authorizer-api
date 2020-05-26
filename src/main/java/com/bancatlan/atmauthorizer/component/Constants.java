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
    public static final String BANK_STRING_ZERO= "";
    public static final String BANK_HN_CURRENCY = "LPS";
    public static final String HN_CURRENCY = "HNL";
    public static final String BANK_ACCOUNT_TYPE_1 = "1";
    public static final String BANK_ACCOUNT_TYPE_2 = "2";
    public static final String BANK_ACTION_VERIFY = "C";
    public static final String BANK_ACTION_PAYMENT = "P";
    public static final String ITM_PROCESS_CODE_WITHDRAW = "0200";
    public static final String ITM_PROCESS_CODE_REVERSE_WITHDRAW = "0400";

    /*STATUS TXN CODES*/
    public static final long INITIAL_TXN_STATUS = 1;
    public static final long PRE_AUTHORIZED_TXN_STATUS = 2;
    public static final long AUTHENTICATED_TXN_STATUS = 10;
    public static final long AUTHORIZED_TXN_STATUS = 20;
    public static final long CONFIRM_TXN_STATUS = 30;
    public static final long CANCEL_CONFIRM_TXN_STATUS = 70;

    /*TXN USE CASE CODES*/
    public static final long VOUCHER_USE_CASE =  174;
    public static final long WITHDRAW_VOUCHER_USE_CASE =  800;
    public static final int INT_VOUCHER_USE_CASE =  174;
    public static final int INT_WITHDRAW_VOUCHER_USE_CASE =  800;

    /*CURRENCY CODES*/
    public static final long HN_CURRENCY_ID =  1;
    public static final long US_CURRENCY_ID =  2;

    /*DEFAULT LIMIT CONFIGURED*/
    public static final long DEFAULT_LIMIT =  1;
    public static final int LENGTH_AUTH_CODE = 8;
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

    /*NOTIFICATION TYPES*/
    public static final String BANK_NOTIFICATION_SMS =  "SMS";
    public static final String BANK_NOTIFICATION_EMAIL =  "EMAIL";
    //public static String TEMPLATE_NOTIFICATION_SMS =  "Envio de dinero Atlantida Retiro sin tarjeta. Monto: L. %s Codigo de retiro %s";
    public static String TEMPLATE_NOTIFICATION_SMS =  "Retiro de dinero sin tarjeta Atlantida. Monto: L. %s Codigo de retiro %s";


}
