package com.bancatlan.atmauthorizer.component;


public interface IUtilComponent {
    String getPickupCodeByCellPhoneNumber(String cellPhoneNumber);
    String getSecretCodeByCellPhoneNumber(String seed);
    Boolean isValidPhoneNumber(String cellPhoneNumber);
    Boolean isValidMsisdn(String cellPhoneNumber);
    Boolean isValidAvailableBalance(String availableBalance, Double txnAmount);
    Boolean isValidCommunicationCompany(String telephone);
    Boolean isANumber(String cellPhoneNumber);
    Boolean isValidStringAmount(String amount);
    Boolean isValidAmountWithAtm(String amount);
    Double convertAmountWithDecimals(Double amount);
    Double getAmountFromKey(String key);
    String createAuthorizationCode(int length);
    String adjustingTelephone(String telephone);
    String generateAtmReference(String key1, String key2);
    String encryptCode(String code);
    String decryptCode(String encryptedCode);
    String getBankCommentPrefix(int useCase);
    String getProcessingCode(String rawCode);
    String getProcessingDateTime(String processingDate);
}
