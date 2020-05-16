package com.bancatlan.atmauthorizer.component;


public interface IUtilComponent {
    String getPickupCodeByCellPhoneNumber(String cellPhoneNumber);
    Boolean isValidPhoneNumber(String cellPhoneNumber);
    Boolean isValidStringAmount(String amount);
    Double convertAmountWithDecimals(Double amount);
}
