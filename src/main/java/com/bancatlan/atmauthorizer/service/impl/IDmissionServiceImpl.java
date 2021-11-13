package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.model.Transaction;
import com.bancatlan.atmauthorizer.service.IIDmissionService;
import mymo.infatlan.hn.ws.mymo001.out.updatetransaction.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.ws.BindingProvider;
import java.math.BigInteger;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.time.LocalDateTime;

@Service
public class IDmissionServiceImpl implements IIDmissionService {
    Logger LOG = LoggerFactory.getLogger(IDmissionServiceImpl.class);
    String SUCCESS_TXN_STATUS = "Approved";
    String FAIL_TXN_STATUS = "DISPENSE_FAIL";
    String IDMISSION_TXN_TYPE = "Default";
    /**
     * references Files and Endpoints WSDL's
     */
    @Value("${spring.resources.static-locations}")
    String absolutePathWSDLResources;

    @Value("${bus-integration.wsdl.update-txn-name}")
    String updateTxnWSDLName;


    @Value("${idmission.service.po.username}")
    String busIntegrationUsername;

    @Value("${idmission.service.po.password}")
    String busIntegrationPassword;

    @Value("${idmission.auth.password}")
    String idmissionAuthPassword;

    @Value("${idmission.auth.merchantid}")
    String idmissionAuthMerchantId;

    @Value("${idmission.auth.loginid}")
    String idmissionAuthLoginId;

    @Value("${idmission.auth.appcode}")
    String idmissionAuthAppCode;

    @Value("${idmission.source}")
    String idmissionSource;

    @Value("${idmission.isATMProcess}")
    String idmissionisATMProcess;

    @Value("${idmission.isCreatedURL}")
    String idmissionisCreatedURL;

    @Value("${bus-integration.wsdl.update-txn-endpoint}")
    String updateTxnSOAPEndpoint;

    private Boolean updateTransaction(Transaction txn, String status) {
        boolean retVal = true;
        LOG.info("updateTransaction: txn {} , id_mission_status: {}", txn.getId(), status);

        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(busIntegrationUsername,
                        busIntegrationPassword.toCharArray());
            }
        });

        try {
            String urlService = absolutePathWSDLResources + updateTxnWSDLName;
            LOG.info("URL: {}", urlService);
            URL url;
            url = new URL(urlService);

            SIOSUpdateTransactionService port = new SIOSUpdateTransactionService(url);
            SIOSUpdateTransaction updateTransactionService = port.getHTTPPort();

            BindingProvider provider = (BindingProvider) updateTransactionService;
            provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, updateTxnSOAPEndpoint);

            /**
             * identificadorColeccion
             */
            DTIdentificadorColeccion identificadorColeccion = new DTIdentificadorColeccion();
            identificadorColeccion.setNumeroTransaccion(txn.getChannelReference());

            /**
             * parametroAdicionalColeccion
             */
            DTParametroAdicionalColeccion parametroAdicionalColeccion = new DTParametroAdicionalColeccion();
            DTParametroAdicionalItem username = new DTParametroAdicionalItem();
            username.setLinea(new BigInteger("1"));
            username.setTipoRegistro("Login_Id");
            username.setValor(idmissionAuthLoginId);
            parametroAdicionalColeccion.getParametroAdicionalItem().add(username);


            DTParametroAdicionalItem password = new DTParametroAdicionalItem();
            password.setLinea(new BigInteger("2"));
            password.setTipoRegistro("Application_Code");
            password.setValor(idmissionAuthAppCode);
            parametroAdicionalColeccion.getParametroAdicionalItem().add(password);

            /**
             * updateTransactionColeccion
             */
            DTUpdateTransactionColeccion updateTransactionColeccion = new DTUpdateTransactionColeccion();
            DTUpdateTransactionItem transactionItem = new DTUpdateTransactionItem();
            transactionItem.setFechaHora(LocalDateTime.now().toString());
            transactionItem.setTipoAccion(Constants.STR_WITHDRAW_MYMO);
            transactionItem.setAccion(status);//Approved --- DISPENSE_FAIL
            transactionItem.setMonto(txn.getAmount().toString());
            transactionItem.setMoneda(txn.getCurrency().getCode());
            transactionItem.setFuente(idmissionSource);
            transactionItem.setFlujoAtm(idmissionisATMProcess);
            transactionItem.setGeneraUrl(idmissionisCreatedURL);
            transactionItem.setCuenta(txn.getPayerPaymentInstrument().getStrIdentifier());
            transactionItem.setTipoCuenta(IDMISSION_TXN_TYPE);
            /**
             * parametroAdicionalColeccion to updateTransaction
             */
            updateTransactionColeccion.getUpdateTransactionItem().add(transactionItem);

            DTUpdateTransaction updateTransaction = new DTUpdateTransaction();
            updateTransaction.setLlaveSesion(idmissionAuthPassword);
            updateTransaction.setToken(idmissionAuthMerchantId);

            /**
             * Adding all objects to main body
             */
            updateTransaction.setIdentificadorColeccion(identificadorColeccion);
            updateTransaction.setParametroAdicionalColeccion(parametroAdicionalColeccion);
            updateTransaction.setUpdateTransactionColeccion(updateTransactionColeccion);

            DTResponse response = updateTransactionService.siOSUpdateTransaction(updateTransaction);

            if (response != null && response.getEstado() != null && response.getEstado().getCodigo() != null
                    && response.getEstado().getCodigo().equals(Constants.BANK_SUCCESS_STATUS_CODE)) {
                LOG.info("Successful Response IDmission txn => {}", response.getEstado().getDescripcion());
            } else {
                LOG.info("Custom response error IDmission txn => {}", response.getEstado().getDescripcion());
                retVal = false;
            }

        } catch (Exception ex) {
            LOG.error("Fail Response => message, {}", ex.getMessage());
            retVal = false;
        }

        return retVal;
    }

    @Override
    public Boolean setSuccessTransaction(Transaction txn) {
        return updateTransaction(txn, SUCCESS_TXN_STATUS);
    }

    @Override
    public Boolean setFailTransaction(Transaction txn) {
        return updateTransaction(txn, FAIL_TXN_STATUS);
    }
}
