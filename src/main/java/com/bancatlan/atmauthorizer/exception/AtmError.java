package com.bancatlan.atmauthorizer.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AtmError implements IError{
    OK("00","Success"),
    ERROR_01("01","Refer to card issuer"),
    ERROR_02("02","Refer to card issuer, special condition"),
    ERROR_03("03","ATM user not properly configured"),/*Invalid merchant or service provider*/
    ERROR_04("04","Pickup card"),
    ERROR_05("05","Do not honor"),
    ERROR_06("06","Error"),
    ERROR_07("07","Pickup card special condition other than less/stolen card"),
    ERROR_10("10","Partial Approval"),
    ERROR_11("51","V.I.P. approval"),
    ERROR_12("12","Unexpected error, check the stack trace"),/*Invalid transaction*/
    ERROR_13("13","Invalid amount"),/*Invalid amount (currency conversion field overflow)*/
    ERROR_14("14","Invalid phone number (msisdn)"),/*Invalid account number (no such number)*/
    ERROR_15("15","No such issuer"),
    ERROR_17("17","customer cancellation"),
    ERROR_19("19","re-enter transaction"),
    ERROR_20("20","Invalid response"),
    ERROR_21("21","No action taken (unable to back our prior transaction)"),
    ERROR_22("22","Suspected malfunction"),
    ERROR_25("25","Payer doesn't exist in system"), /*Unable to locate record in file, or account number is missing from the inquiry*/
    ERROR_28("28","File is temporally unavailable"),
    ERROR_30("30","Format error"),
    ERROR_41("41","Pickup card (lost card)"),
    ERROR_43("43","Pickup card (stolen card)"),
    ERROR_51("51","Insufficient founds"),/**/
    ERROR_52("52","No checking account"),
    ERROR_53("53","No savings account"),
    ERROR_54("54","Expired card"),
    ERROR_55("55","Incorrect PIN"),/**/
    ERROR_57("57","Transaction not permitted to cardholder"), /*Customer system blocked*/
    ERROR_58("58","Transaction not allow at terminal"),
    ERROR_59("59","Suspected fraud"),
    ERROR_61("61","Activity amount limit exceeded"),/**/
    ERROR_62("62","Restricted card [For example in country exclusion table]"),
    ERROR_63("63","Security violation"),/**/
    ERROR_65("65","Activity count limit exceeded"),/**/
    ERROR_68("68","Response received too late"),/*When a voucher already expired*/
    ERROR_75("75","Allowable number of PIN entry tries exceeded"), /*Attempts validation*/
    ERROR_76("76","missing reference in atm request, this process require a unique reference"), /* Unavailable to located previous message (Not match on retrieval reference number)*/
    ERROR_77("77","atm reference not found"),/*Previous message located for a repeat or reversal, but repeat or reversal data are inconsistent with original message*/
    ERROR_78("78","Block first used. The transaction is from a new cardholder, and the card has not been properly unblock"),
    ERROR_80("80","VISA transactions credit issuer unavailable, private label and check acceptance: Invalid Date"),
    ERROR_81("81","It's missing or invalid some field in atm request (check voucher request or => secret/pickup code)"),/*PIN cryptographic error found [error found by VIC security module during PIN decryption]*/
    ERROR_82("82","Negative CAM, dCVV, ICVV, or CVV results"),
    ERROR_83("83","Unavailable to verify PIN"),
    ERROR_85("85","No reason to decline a request for account number verification, address verification, CVV2 verification or a credit voucher or merchandise return"),
    ERROR_91("91","Issuer unavailable or switch inoperative (STIP not applicable or available for this transaction)"),
    ERROR_92("92","Destination can not be found for routing"),
    ERROR_93("93","Transaction can not be completed, violation of law"),
    ERROR_94("94","atm reference already exists in authorizer BASA"),/*Duplicate transactions*/
    ERROR_95("95","Reconcile error"),
    ERROR_96("96","System malfunction, System malfunction or certain field error conditions"),
    ERROR_B1("B1","Suncharge amount not permitted on VISA cards (U.S. acquires only)"),
    ERROR_N0("N0","Force STIP"),
    ERROR_N3("N3","ATM account"),/*Cash service not available*/
    ERROR_N4("N4","Cash back request exceeds issuer limit"),
    ERROR_N7("N7","Decline for CVV2 failure"),
    ERROR_P2("P2","Invalid biller information"),
    ERROR_P5("P5","PIN change/unblock request declined"),
    ERROR_P6("P6","Unsafe PIN"),
    ERROR_Q1("Q1","Card authentication failed"),
    ERROR_R0("R0","Stop payment order"),
    ERROR_R1("R0","In reverse Voucher not found or not valid"),/*Revocation of authorization order*/
    ERROR_R3("R3","Revocation of all authorization order"),
    ERROR_XA("XA","Forward to issuer"),
    ERROR_XD("XD","Forward to issuer"),
    ERROR_Z3("Z3","Unavailable to go online"),
    ;

    private String code;
    private String message;

    AtmError(final String code, final String message){
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString(){
        return getCode() + "|" + getMessage();
    }
}
