package com.bancatlan.atmauthorizer.component.impl;

import com.bancatlan.atmauthorizer.api.http.AtmBody;
import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.component.IUtilComponent;
import com.bancatlan.atmauthorizer.exception.AuthorizerError;
import com.bancatlan.atmauthorizer.exception.ModelCustomErrorException;
import com.bancatlan.atmauthorizer.exception.ModelNotFoundException;
import com.bancatlan.atmauthorizer.model.Config;
import com.bancatlan.atmauthorizer.service.IConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class UtilComponentImpl implements IUtilComponent {
    Logger LOG = LoggerFactory.getLogger(UtilComponentImpl.class);
    public static String sessionKey;
    public static AtmBody atmBody;
    private Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    private Map<String, Double> amountValues = new HashMap<>();
    private SecretKeySpec secretKey;
    private IvParameterSpec ivspec;


    @Value("${secret.qr.phrase}")
    private String secretPhrase;

    public static final String IV = "1234567890123456";

    @Autowired
    private IConfigService configService;

    @Override
    public String getPickupCodeByCellPhoneNumber(String cellPhoneNumber) {
        /**
         * use getCode => to make less collision-able the pickupCode for a customer
         */
        return this.getRandomNumber(Constants.PIVOT_PICKUP_CODE_RANGE) + this.getOTP(cellPhoneNumber);
    }

    @Override
    public String getSecretCodeByCellPhoneNumber(String seed) {
        return this.getOTP(seed);
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
    public Boolean isValidMsisdn(String cellPhoneNumber) {
        if (cellPhoneNumber == null) {
            return false;
        }

        cellPhoneNumber = cellPhoneNumber.replaceAll("[^0-9]", "");

        if (!(cellPhoneNumber.length() == 8 || cellPhoneNumber.length() == 11)) {
            return false;
        }

        if (cellPhoneNumber.length() == 11 && !cellPhoneNumber.startsWith(Constants.HND_MSISDN_CODE)) {
            return false;
        }

        return true;
    }

    @Override
    public Boolean isValidAvailableBalance(String availableBalance, Double txnAmount) {
        if (pattern.matcher(availableBalance).matches()) {
            Double _availableBalance = Double.parseDouble(availableBalance);
            if (_availableBalance >= txnAmount) {
                return true;
            }
        } else {
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PARSING_ERROR_VALIDATING_INSUFFICIENT_FUNDS);
        }
        return false;
    }

    @Override
    public Boolean isValidCommunicationCompany(String telephone) {
        if (telephone == null || telephone.equals("")) {
            throw new ModelNotFoundException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.PARSING_ERROR_VALIDATING_COMMUNICATION_COMPANY);
        }
        String firstNumber = telephone.substring(0, 1);
        if (firstNumber.equals("3") || firstNumber.equals("8") || firstNumber.equals("9")) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean isANumber(String cellPhoneNumber) {
        if (cellPhoneNumber == null) {
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
    public Boolean isValidAmountWithAtm(String amount) {
        double doubleAmount;
        if (amount == null) {
            return false;
        }
        try {
            doubleAmount = Double.parseDouble(amount);
        } catch (NumberFormatException nfe) {
            return false;
        }

        if (!this.isValidAmountAccordingATMRules(doubleAmount)) {
            return false;
        }
        return true;
    }

    @Override
    public Double convertAmountWithDecimals(Double amount) {
        Integer conversionFactor = 1; //Todo preference from DB
        return amount/conversionFactor;
    }

    @Override
    public Double getAmountFromKey(String key) {
        amountValues.put("1",100.00);
        amountValues.put("2",200.00);
        amountValues.put("3",300.00);
        amountValues.put("5",500.00);
        amountValues.put("6",1000.00);
        amountValues.put("7",2000.00);
        amountValues.put("8",4000.00);
        return amountValues.get(key);
    }

    @Override
    public String createAuthorizationCode(int length) {
        // chose a Character random from this String
        //String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String AlphaNumericString = "0123456789";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }
        return sb.toString();
    }

    @Override
    public String adjustingTelephone(String telephone) {
        if (telephone != null && telephone.length() > 8) {
            int init = telephone.length() - 8;
            telephone = telephone.substring(init);
        }
        return telephone;
    }

    @Override
    public String generateAtmReference(String key1, String key2) {
        Optional<String> k1 = Optional.ofNullable(key1);
        Optional<String> k2 = Optional.ofNullable(key2);
        return (LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE) + k1.orElse(Constants.BANK_STRING_ZERO) + k2.orElse(Constants.BANK_STRING_ZERO)).trim();
    }

    @Override
    public String encryptCode(String str) {
        try {
            setKeyAndIV(secretPhrase);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(str.getBytes("UTF-8")));
        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
            throw new ModelCustomErrorException("Error while cipher Code: ", AuthorizerError.ENCRYPT_ERROR);
        }
    }

    @Override
    public String decryptCode(String strToDecrypt) {
        try {
            setKeyAndIV(secretPhrase);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            LOG.error("decryptCode {}", e.getMessage());
            throw new ModelCustomErrorException("decryptCode error", AuthorizerError.ENCRYPT_ERROR);
        }
    }

    @Override
    public String getBankCommentPrefix(int useCase) {
        switch (useCase) {
            case Constants.INT_VOUCHER_USE_CASE:
                return Constants.PREFIX_RTS_DEFAULT;
            case Constants.INT_VOUCHER_USE_CASE_QR:
                return Constants.PREFIX_RTS_QR;
            case Constants.INT_CASH_OUT_KEYBOARD_USE_CASE:
                return Constants.PREFIX_RTS_MYMO;
        }
        return Constants.PREFIX_RTS_DEFAULT;
    }

    @Override
    public String getProcessingCode(String rawCode) {
        Optional<String> code = Optional.ofNullable(rawCode);
        return code.orElse(Constants.DEFAULT_PROCESSING_CODE).substring(0,2);
    }

    @Override
    public String getProcessingTime(String processingDate) {
        Optional<String> processDate = Optional.ofNullable(processingDate);
        String defaultTime = LocalDateTime.now().format(DateTimeFormatter.ISO_TIME).replace(":", "").replace(".","");
        String result = processDate.orElse(defaultTime.substring(0, Constants.INT_DATE_TIME_SIZE));
        int length =  result.length();
        int sizeResult = (Constants.INT_DATE_TIME_SIZE > length ? length : Constants.INT_DATE_TIME_SIZE);

        result = result.substring(length - sizeResult, length);
        return result;
    }

    @Override
    public String getProcessingDate(LocalDateTime processingDate) {
        Optional<LocalDateTime> processDate = Optional.ofNullable(processingDate);
        return processDate.orElse(LocalDateTime.now()).format(DateTimeFormatter.ofPattern("YYMMdd"));
    }

    @Override
    public String getProcessingDate(String processingDate) {
        String defaultDate = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        defaultDate = defaultDate.substring(defaultDate.length() - Constants.INT_DATE_TIME_SIZE);
        Optional<String> processDate = Optional.ofNullable(processingDate);
        return processDate.orElse(defaultDate).substring(0,Constants.INT_DATE_TIME_SIZE);
    }

    @Override
    public String getCurrencyTransactionCode(String currency) {
        if(currency.equals(Constants.HN_CURRENCY)){
           return Constants.HN_CURRENCY_TRANSACTION_CODE;
        };
        return Constants.HN_CURRENCY_TRANSACTION_CODE;
    }

    /**
     *It generates a code for verification in a measure time configured
     * @param value it should be cellphone number, email or user
     * @return a otp (pickup code)
     * @throws ModelCustomErrorException exception
     */
    private String getOTP(final String value) throws ModelCustomErrorException {
        String val = "";

        String addedValue = value + LocalDateTime.now().getDayOfMonth() + LocalDateTime.now().getMinute() + LocalDateTime.now().getSecond();

        val = encryptString(addedValue, Constants.SIZE_PICKUP_CODE);

        return val;
    }

    private String getCode(final String value) throws ModelCustomErrorException {
        String val = "";

        String addedValue = value + LocalDateTime.now().getDayOfMonth() + LocalDateTime.now().getMinute() + LocalDateTime.now().getSecond();

        val = encryptString(addedValue, Constants.SIZE_PICKUP_CODE);

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
            if (code.equals(encryptString(val, Constants.SIZE_PICKUP_CODE))) {
                return true;
            }
        }

        return false;
    }

    /**
     * It makes a encrypt value using a sign method as MD5 or SHA1
     * @param value value to encrypt
     * @param size value to encrypt
     * @return valor encrypted value
     * @throws ModelCustomErrorException exception
     */
    private String encryptString(final String value, int size) throws ModelCustomErrorException {
        String encrypted = "";
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(value.getBytes(), 0, value.length());
            encrypted = "" + new BigInteger(1, md.digest()).toString();
            md.reset();
            encrypted = encrypted.substring(0, size);
        } catch (NoSuchAlgorithmException ex) {
            LOG.error(AuthorizerError.ENCRYPT_ERROR.toString(), ex);
            throw new ModelCustomErrorException(ex.getMessage(), AuthorizerError.ENCRYPT_ERROR);
        }
        return encrypted;
    }

    private String getRandomNumber(int range) {
        Random random = new Random();
        Integer randomInteger = random.nextInt(range);
        return randomInteger.toString();
    }

    public static String getSessionKey() {
        return sessionKey;
    }

    public static void setSessionKey(String _sessionKey) {
        sessionKey = _sessionKey;
    }

    public static AtmBody getAtmBody() {
        return atmBody;
    }

    public static void setAtmBody(AtmBody atmBody) {
        UtilComponentImpl.atmBody = atmBody;
    }

    private boolean isValidAmountAccordingATMRules(Double amount) {
        if (amount % 100 != 0 || amount > 5000) { //Todo make this parameterizable
            return false;
        }
        return true;
    }

    private void setKeyAndIV(String myKey) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[][] keys2 = GetHashKeys(myKey);

        ivspec = new IvParameterSpec(keys2[1]);
        secretKey = new SecretKeySpec(keys2[0], "AES");
    }
    private byte[][] GetHashKeys(String key) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        try {
            byte[][] result = new byte[2][];
            MessageDigest sha2 = MessageDigest.getInstance("SHA-256");
            byte[] rawKey = key.getBytes("UTF-8");
            byte[] rawIV = key.getBytes("UTF-8");
            byte[] hashKey = sha2.digest(rawKey);
            byte[] hashIV = sha2.digest(rawIV);

            byte[] hashIV2 = Arrays.copyOf(hashIV, 16);

            result[0] = hashKey;
            result[1] = hashIV2;

            return result;
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public  String getAccountingTranferChannelId(String useCase){
        Config configIssuer = configService.getConfigByPropertyName(Constants.STR_ACCOUNTING_TRANSFERS_CHANNEL_ID + useCase);
        String accountingTransfersChannelId = (configIssuer != null && configIssuer.getPropertyValue() != null && !configIssuer.getPropertyValue().equals("")) ? configIssuer.getPropertyValue() : configService.getConfigByPropertyName(Constants.STR_ACCOUNTING_TRANSFERS_CHANNEL_ID_DEFAULT ).getPropertyValue();

        return accountingTransfersChannelId;
    }

    @Override
    public String  getConfigValueByPropertyName(String propertyName, String propertyNameDefault){
        Config configIssuer = configService.getConfigByPropertyName(propertyName);
        String propertyValue = (configIssuer != null && configIssuer.getPropertyValue() != null && !configIssuer.getPropertyValue().equals("")) ? configIssuer.getPropertyValue() : configService.getConfigByPropertyName(propertyNameDefault).getPropertyValue();
        return propertyValue;
    }
}
