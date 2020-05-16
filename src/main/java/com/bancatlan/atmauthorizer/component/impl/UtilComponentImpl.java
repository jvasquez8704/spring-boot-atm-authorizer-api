package com.bancatlan.atmauthorizer.component.impl;

import com.bancatlan.atmauthorizer.component.IUtilComponent;
import com.bancatlan.atmauthorizer.exception.AuthorizerError;
import com.bancatlan.atmauthorizer.exception.ModelCustomErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Component
public class UtilComponentImpl implements IUtilComponent {
    Logger LOG = LoggerFactory.getLogger(UtilComponentImpl.class);
    public static String sessionKey;
    private Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    @Override
    public String getPickupCodeByCellPhoneNumber(String cellPhoneNumber) {
        return this.encrypt(cellPhoneNumber);
    }

    @Override
    public Boolean isValidPhoneNumber(String cellPhoneNumber) {
        if (cellPhoneNumber == null) {
            return false;
        }

        if (cellPhoneNumber.length() != 8) {
            return false;
        }
        return pattern.matcher(cellPhoneNumber).matches();
    }

    @Override
    public Boolean isValidStringAmount(String amount) {
        if (amount == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(amount);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @Override
    public Double convertAmountWithDecimals(Double amount) {
        Integer conversionFactor = 100; //Todo preference from DB
        return amount/conversionFactor;
    }

    /**
     *It generates a code for verification in a measure time configured
     * @param value it should be cellphone number, email or user
     * @return a otp (pickup code)
     * @throws ModelCustomErrorException exception
     */
    private String encrypt(final String value) throws ModelCustomErrorException {
        String val = "";

        String addedValue = value + LocalDateTime.now().getDayOfMonth() + LocalDateTime.now().getMinute();

        val = encryptString(addedValue);

        return val;
    }

    /**
     * Verify the encrypted code against a new one using the
     * same seed. Use a validity of X minutes (configurable)
     *
     * @param code pickup code sent
     * @param value seed value
     * @param validity code "life" time (in minutes)
     * @return true: correctly verify the code false: cannot find
     * equal incidences.
     * @throws ModelCustomErrorException exception
     */
    public boolean checkEncryptedValue(final String code, final String value, final String validity) throws ModelCustomErrorException {
        String val = "";

        int minute = LocalDateTime.now().getMinute();
        String addedValue = value + LocalDateTime.now().getDayOfMonth();
        int validityMin = Integer.valueOf(validity);
        for (int x = 0; x <= validityMin; x++) {
            if ((minute - x) < 0) {
                minute = 60;
            }
            val = addedValue + (minute - x);
            if (code.equals(encryptString(val))) {
                return true;
            }
        }

        return false;
    }

    /**
     * It makes a encrypt value using a sign method as MD5 or SHA1
     * @param value value to encrypt
     * @return valor encrypted value
     * @throws ModelCustomErrorException exception
     */
    private String encryptString(final String value) throws ModelCustomErrorException {
        String encrypted = "";
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(value.getBytes(), 0, value.length());
            encrypted = "" + new BigInteger(1, md.digest()).toString();
            md.reset();
            encrypted = encrypted.substring(0, 5);
        } catch (NoSuchAlgorithmException ex) {
            LOG.error(AuthorizerError.ENCRYPT_ERROR.toString(), ex);
            throw new ModelCustomErrorException(ex.getMessage(), AuthorizerError.ENCRYPT_ERROR);
        }
        return encrypted;
    }

    public static String getSessionKey() {
        return sessionKey;
    }

    public static void setSessionKey(String _sessionKey) {
        sessionKey = _sessionKey;
    }
}
