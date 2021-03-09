package com.bancatlan.atmauthorizer.service.impl;

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

@Service
public class IDmissionServiceImpl implements IIDmissionService {
    Logger LOG = LoggerFactory.getLogger(IDmissionServiceImpl.class);
    /**
     * references Files and Endpoints WSDL's
     */
    @Value("${spring.resources.static-locations}")
    String absolutePathWSDLResources;

    @Value("${bus-integration.wsdl.update-txn-name}")
    String updateTxnWSDLName;

    @Value("${bank.service.po.dev1.username}")
    String busIntegrationDevUsername;

    @Value("${bank.service.po.dev1.password}")
    String busIntegrationDevPassword;

    @Value("${bus-integration.wsdl.update-txn-endpoint}")
    String updateTxnSOAPEndpoint;

    @Override
    public Boolean updateTransaction(Transaction txn) {
        boolean retVal = true;
        LOG.info("updateTransaction: txn {} ", txn.getId());

        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(busIntegrationDevUsername,
                        busIntegrationDevPassword.toCharArray());
            }
        });

        try {
            String urlService = absolutePathWSDLResources + updateTxnWSDLName;
            LOG.info("URL: {}", urlService);
            URL url;
            url = new URL(urlService);

            SIOSUpdateTransactionService port = new SIOSUpdateTransactionService(url);
            SIOSUpdateTransaction updateTransactionService = port.getHTTPPort();

            /**
             * identificadorColeccion
             */
            DTIdentificadorColeccion identificadorColeccion = new DTIdentificadorColeccion();
            identificadorColeccion.setNumeroTransaccion("90833");

            /**
             * parametroAdicionalColeccion
             */
            DTParametroAdicionalColeccion parametroAdicionalColeccion = new DTParametroAdicionalColeccion();
            DTParametroAdicionalItem username = new DTParametroAdicionalItem();
            username.setLinea(new BigInteger("1"));
            username.setTipoRegistro("Login_Id");
            username.setValor("atlmymo_integ");
            parametroAdicionalColeccion.getParametroAdicionalItem().add(username);


            DTParametroAdicionalItem password = new DTParametroAdicionalItem();
            password.setLinea(new BigInteger("2"));
            password.setTipoRegistro("Application_Code");
            password.setValor("CUX");
            parametroAdicionalColeccion.getParametroAdicionalItem().add(password);

            /**
             * updateTransactionColeccion
             */
            DTUpdateTransactionColeccion updateTransactionColeccion = new DTUpdateTransactionColeccion();
            DTUpdateTransactionItem transactionItem = new DTUpdateTransactionItem();
            transactionItem.setFechaHora("16/07/2020 13:11:18");
            transactionItem.setTipoAccion("TRANSFER");
            transactionItem.setAccion("Approved");
            transactionItem.setMonto("100.0");
            transactionItem.setMoneda("USD");
            transactionItem.setPais("United States");
            transactionItem.setFuente("KBAPP");
            transactionItem.setFlujoAtm("Y");
            transactionItem.setGeneraUrl("Y");
            transactionItem.setCuenta("10111004791");
            transactionItem.setBanco("Banco Atltida");
            transactionItem.setTipoCuenta("Default");

            /**
             * parametroAdicionalColeccion to updateTransaction
             */
            /*DTParametroAdicionalColeccion parameterAdditionalCollectionUpdateTransaction = new DTParametroAdicionalColeccion();
            DTParametroAdicionalItem parametroAdicionalItem = new DTParametroAdicionalItem();
            parametroAdicionalItem.setLinea(new BigInteger("0"));
            parametroAdicionalItem.setTipoRegistro("?");
            parametroAdicionalItem.setValor("?");
            parameterAdditionalCollectionUpdateTransaction.getParametroAdicionalItem().add(parametroAdicionalItem);
            transactionItem.setParametroAdicionalColeccion(parameterAdditionalCollectionUpdateTransaction);*/
            updateTransactionColeccion.getUpdateTransactionItem().add(transactionItem);

            DTUpdateTransaction updateTransaction = new DTUpdateTransaction();
            updateTransaction.setVersion("6.4.7.11");
            updateTransaction.setLlaveSesion("Merchant#123");
            updateTransaction.setToken("14196");

            /**
             * Adding all objects to main body
             */
            updateTransaction.setIdentificadorColeccion(identificadorColeccion);
            updateTransaction.setParametroAdicionalColeccion(parametroAdicionalColeccion);
            updateTransaction.setUpdateTransactionColeccion(updateTransactionColeccion);

            DTResponse response = updateTransactionService.siOSUpdateTransaction(updateTransaction);

            if (response != null && response.getEstado() != null && response.getEstado().getCodigo() != null
                    && response.getEstado().getCodigo().equals("0000")) {
                LOG.info("Successful Response IDmission txn => {}", response.getEstado().getDetalleTecnico());
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

}
