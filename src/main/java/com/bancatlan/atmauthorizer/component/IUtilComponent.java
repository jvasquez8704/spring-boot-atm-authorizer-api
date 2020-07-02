package com.bancatlan.atmauthorizer.component;


public interface IUtilComponent {
    String getPickupCodeByCellPhoneNumber(String cellPhoneNumber);
    Boolean isValidPhoneNumber(String cellPhoneNumber);
    Boolean isValidAvailableBalance(String availableBalance, Double txnAmount);
    Boolean isValidCommunicationCompany(String telephone);
    Boolean isANumber(String cellPhoneNumber);
    Boolean isValidStringAmount(String amount);
    Boolean isValidAmountWithAtm(String amount);
    Double convertAmountWithDecimals(Double amount);
    Double getAmountFromKey(String key);
    String createAuthorizationCode(int length);
    String fitTelephone(String telephone);
}
