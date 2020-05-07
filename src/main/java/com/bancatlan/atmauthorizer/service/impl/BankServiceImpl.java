package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.component.impl.UtilComponentImpl;
import com.bancatlan.atmauthorizer.exception.AuthorizerError;
import com.bancatlan.atmauthorizer.exception.ModelCustomErrorException;
import com.bancatlan.atmauthorizer.model.PaymentInstrument;
import com.bancatlan.atmauthorizer.model.PaymentInstrumentType;
import com.bancatlan.atmauthorizer.service.IBankService;
import infatlan.hn.entrust.core.external.message.*;
import och.infatlan.hn.ws.acd088.out.transferenciacontable.*;
import och.infatlan.hn.ws.acd088.out.transferenciacontable.DTEstado;
import och.infatlan.hn.ws.acd088.out.transferenciacontable.DTIdentificadorColeccion;
import och.infatlan.hn.ws.acd101.out.consultasaldov2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class BankServiceImpl implements IBankService {
    Logger LOG = LoggerFactory.getLogger(BankServiceImpl.class);
    private static final String SUCCESS_VALUE = "S1";

    @Value("${bank.service.po.username}")
    String busIntegrationUsername;

    @Value("${bank.service.po.password}")
    String busIntegrationPassword;

    /*Balance*/
    @Value("${bank.service.account.list.transaction.id}")
    String transactionId;

    @Value("${bank.service.account.list.application.id}")
    String applicationId;

    @Value("${bank.service.account.list.canal.id}")
    String channelId;

    @Value("${bank.service.account.list.register.type}")
    String registerType;

    /*Notification*/
    @Value("${bank.service.notification.transaction.id}")
    String notificationTransactionId;

    @Value("${bank.service.notification.application.id}")
    String notificationApplicationId;

    @Value("${bank.service.notification.canal.id}")
    String notificationChannelId;

    @Value("${bank.service.notification.line}")
    String notificationLine;

    @Value("${bank.service.notification.category}")
    String notificationCategory;

    /*transfer*/
    @Value("${bank.service.transfer.transaction.id}")
    String transferTransactionId;

    @Value("${bank.service.transfer.application.id}")
    String transferApplicationId;

    @Value("${bank.service.transfer.canal.id}")
    String transferChannelId;

    @Value("${bank.service.transfer.action}")
    String transferAction;

    @Value("${bank.service.transfer.validate}")
    String transferValidate;

    @Value("${bank.service.transfer.debit.currency}")
    String transferDebitCurrency;

    @Value("${bank.service.transfer.debit.movement}")
    String transferDebitMovement;

    @Value("${bank.service.transfer.credit.currency}")
    String transferCreditCurrency;

    @Value("${bank.service.transfer.credit.movement}")
    String transferCreditMovement;

    @Value("${bank.service.transfer.field.type}")
    String transferFieldType;

    @Value("${bank.service.transfer.source}")
    String transferSource;

    @Value("${bank.service.transfer.line}")
    String transferLine;

    @Value("${spring.resources.static-locations}")
    String absolutePathWSDLResources;

    @Value("${bus-integration.wsdl.balance-name}")
    String balanceWSDLName;

    @Value("${bus-integration.wsdl.transfer-name}")
    String transferWSDLName;

    @Value("${bus-integration.wsdl.notification-name}")
    String notificationWSDLName;

    @Override
    public Boolean verifyAccountByOcbUser(String ocbUser, String strAccount) {
        LOG.info("verifyAccountByOcbUser: ");
        LOG.info("ocbUser: " + ocbUser + " account: " + strAccount);
        return true;
    }

    @Override
    public Boolean sendNotification(String cellPhone, String subject, String body, String typeNotification) {
        LOG.info("sendNotificationEmail:");
        LOG.info("cellPhone or email: " + cellPhone);
        LOG.info("subject: " + subject);
        LOG.info("body: " + body);
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(busIntegrationUsername,
                        busIntegrationPassword.toCharArray());
            }
        });
        String urlS = absolutePathWSDLResources + notificationWSDLName;
        //String urlS = "file:/Users/walletmachine/projects/bank-git-repository/atm-authorizer-mysql/src/main/resources/wsdl/SI_OS_EjecutarEnvioNotificacionService.wsdl";

        URL url;
        try {
            url = new URL(urlS);
            SIOSEjecutarEnvioNotificacionService port = new SIOSEjecutarEnvioNotificacionService(url);
            SIOSEjecutarEnvioNotificacion sms = port.getHTTPPort();

            DTDestinoItem dtditem = new DTDestinoItem();
            dtditem.setCategoria(notificationCategory);
            dtditem.setIdentificador(cellPhone);
            dtditem.setTipo(typeNotification);

            DTDestinatarioColeccion dtDestCol = new DTDestinatarioColeccion();
            dtDestCol.getDestinoItem().add(dtditem);

            DTMensajeItem dtItem = new DTMensajeItem();
            dtItem.setLinea(notificationLine);
            dtItem.setMensaje(body);
            dtItem.setTipo(typeNotification);
            dtItem.setDestinatarioColeccion(dtDestCol);

            DTMensajeColeccion dtMensaje = new DTMensajeColeccion();
            dtMensaje.getMensajeItem().add(dtItem);

            DTPeticionGeneral peticion = new DTPeticionGeneral();
            peticion.setTransaccionId(Integer.valueOf(notificationTransactionId));
            peticion.setCanalId(notificationChannelId);
            peticion.setAplicacionId(notificationApplicationId);

            DTEjecutarEnvioNotificacion smsData = new DTEjecutarEnvioNotificacion();
            smsData.setMensajeColeccion(dtMensaje);
            smsData.setPeticionGeneral(peticion);

            DTEjecutarEnvioNotificacionResponse response = sms.siOSEjecutarEnvioNotificacion(smsData);
            if (!response.getRespuesta().getEstado().getCodigo().equals(Constants.BANK_SUCCESS_STATUS_CODE)) {
                LOG.error(AuthorizerError.EXTERNAL_SMS_ERROR + " || "
                        + response.getRespuesta().getEstado().getCodigo() + "|"
                        + response.getRespuesta().getEstado().getDescripcion() + "|"
                        + response.getRespuesta().getEstado().getDetalleTecnico() + "||");
                /*throw new ModelCustomErrorException(response.getRespuesta().getEstado().getCodigo() + ":"
                        + response.getRespuesta().getEstado().getDescripcion(),AuthorizerError.EXTERNAL_SMS_ERROR);*/
            }
        } catch (ModelCustomErrorException ent) {
            LOG.error(ent.getMessage(), ent);
            throw ent;
        } catch (MalformedURLException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ModelCustomErrorException(ex.getMessage(), AuthorizerError.MALFORMED_URL);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ModelCustomErrorException(e.getMessage(), AuthorizerError.UNEXPECTED_ERROR);
        }

        return true;
    }

    @Override
    public String transferMoney(String accountDebit, String accountCredit, Double amount, String customComment) {
        LOG.info("transferMoney: ");
        LOG.info("accountDebit: " + accountDebit + " accountCredit: " + accountCredit + " amount: " + amount + " comment: " + customComment);
        //String toAccNum, String fromAccNum, double payerNetAmount, Long transactionId, Integer useCaseId, String customDesc, Long customerId
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(busIntegrationUsername,
                        busIntegrationPassword.toCharArray());
            }
        });
        String urlS = absolutePathWSDLResources + transferWSDLName;
        //String urlS = "file:/Users/walletmachine/projects/bank-git-repository/atm-authorizer-mysql/src/main/resources/wsdl/SI_OS_TransferenciaContableService.wsdl";
        String uniqueTransNum = "";
        URL url;
        try {
            url = new URL(urlS);
            SIOSTransferenciaContableService port = new SIOSTransferenciaContableService(url);
            SIOSTransferenciaContable coreBankingTransferClient = port.getHTTPPort();

            DTTransferenciaContable mtTransferenciaContable = new DTTransferenciaContable();
            mtTransferenciaContable.setActivarMultipleEntrada(BigInteger.ZERO);
            mtTransferenciaContable.setActivarParametroAdicional("");
            mtTransferenciaContable.setTransaccionId(transferTransactionId);
            mtTransferenciaContable.setAplicacionId(transferApplicationId);
            mtTransferenciaContable.setPaisId(BigInteger.ZERO);
            mtTransferenciaContable.setEmpresaId(BigInteger.ZERO);
            mtTransferenciaContable.setCanalId(transferChannelId);

            DTIdentificadorColeccion identificadorColeccion = new DTIdentificadorColeccion();
            //identificadorColeccion.setOmniCanal(transactionId.toString());
            identificadorColeccion.setOmniCanal("");
            mtTransferenciaContable.getIdentificadorColeccion().add(identificadorColeccion);

            DTTransferenciaContableColeccion transferenciaContableColeccion = new DTTransferenciaContableColeccion();

            DTTransferenciaContableItem transferenciaContableItem = new DTTransferenciaContableItem();

            transferenciaContableItem.setLinea(BigInteger.ONE);
            transferenciaContableItem.setAccion(transferAction);
            transferenciaContableItem.setValidar(transferValidate);
            transferenciaContableItem.setCuentaDebito(accountDebit);
            transferenciaContableItem.setMonedaDebito(transferDebitCurrency);

            //transferenciaContableItem.setDebitoDescripcion("Transferencia Atlántida Money");
            transferenciaContableItem.setDebitoDescripcion(Constants.MSG_ATM_TO_BANK_ACCOUNT);
            transferenciaContableItem.setMovimientoDebito(transferDebitMovement);

            transferenciaContableItem.setCuentaCredito(accountCredit);
            transferenciaContableItem.setMonedaCredito(transferCreditCurrency);
            transferenciaContableItem.setMontoOriginal(amount);
            transferenciaContableItem.setMovimientoCredito(transferCreditMovement);

            transferenciaContableItem.setMontoDebito(0);
            transferenciaContableItem.setMontoCredito(0);

            transferenciaContableItem.setFuente(transferSource);
            transferenciaContableItem.setSucursalCredito(BigInteger.valueOf(101));
            transferenciaContableItem.setSucursalDebito(BigInteger.ZERO);
            transferenciaContableItem.setNumeroTransaccionUnico(Long.valueOf(0));
            transferenciaContableItem.setNumeroReferencia(Long.valueOf(0));

            och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoColeccion campoCollection = new och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoColeccion();
            och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoItem campoItem = new och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoItem();
            campoItem.setLinea(new BigInteger(transferLine));
            campoItem.setTipoCampo(transferFieldType);

            campoItem.setValor("174");//HERE
            campoCollection.getCampoItem().add(campoItem);

            transferenciaContableItem.setCampoColeccion(campoCollection);

            transferenciaContableColeccion.setTransferenciaContableItem(transferenciaContableItem);
            mtTransferenciaContable.setTransferenciaContableColeccion(transferenciaContableColeccion);

            DTTransferenciaContableResponse response = coreBankingTransferClient
                    .siOSTransferenciaContable(mtTransferenciaContable);

            DTTransferenciaContableItem responseTransferenciaContableItem = response.getRespuesta()
                    .getTransferenciaContableColeccion().getTransferenciaContableItem();
            LOG.debug("Core Banking Transfer Service Response -> Comment : "
                    + responseTransferenciaContableItem.getComentario());
            DTEstado responseState = response.getRespuesta().getEstado();
            LOG.debug("Core Banking Transfer Service Response -> State-> Description : " + responseState.getDescripcion());
            uniqueTransNum = String.valueOf(responseTransferenciaContableItem.getNumeroTransaccionUnico());
            // Check if the response is successful

            if (SUCCESS_VALUE.equals(responseState.getTipo())) {
                // Response is successful.
                LOG.debug("Core Banking Unique Transaction Number:{} ", uniqueTransNum);
            } else {
                uniqueTransNum = "-1";// indicates Error.
                LOG.error(AuthorizerError.CUSTOM_ERROR_ACCOUNTING_TRANSFER_ESB.toString() + " Error type: " + responseState.getTipo() +
                        " Code: " + responseState.getCodigo() + " description: " + responseState.getDescripcion());
                throw new ModelCustomErrorException(
                        Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.CUSTOM_ERROR_ACCOUNTING_TRANSFER_ESB);
            }

        }catch (Exception e) {
            LOG.error(AuthorizerError.ERROR_ACCOUNTING_TRANSFER_FROM_BANK.toString(), e);
            throw new ModelCustomErrorException(
                    e.getMessage(), AuthorizerError.ERROR_ACCOUNTING_TRANSFER_FROM_BANK);
        }

        return uniqueTransNum;
    }

    @Override
    public List<PaymentInstrument> getBankAccountsByUserId(String userId) {
        // verify if ocbUser exists inside authorizer system when it will be required
        List<PaymentInstrument> retVal = new ArrayList<>();
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(busIntegrationUsername,
                        busIntegrationPassword.toCharArray());
            }
        });
        String urlS = absolutePathWSDLResources + balanceWSDLName;
        //String urlS = "file:/Users/walletmachine/projects/bank-git-repository/atm-authorizer-mysql/src/main/resources/wsdl/SI_OS_ConsultaSaldoV2Service.wsdl";
        String sessionKey = UtilComponentImpl.getSessionKey();
        URL url;
        try {
            url = new URL(urlS);
            SIOSConsultaSaldoV2Service port = new SIOSConsultaSaldoV2Service(url);
            SIOSConsultaSaldoV2 coreConsultaClient = port.getHTTPPort();

            DTConsultaSaldoV2 poRequest = new DTConsultaSaldoV2();
            poRequest.setActivarMultipleEntrada(new BigInteger("1"));
            poRequest.setTransaccionId(transactionId);
            poRequest.setAplicacionId(applicationId);
            poRequest.setCanalId(channelId);
            poRequest.setLlaveSesion(sessionKey);
            poRequest.setUsuarioId(userId);
            //poRequest.setTransaccionId(preferences.get("service.bankaccountlist.transactionId", "100128"));
            //poRequest.setAplicacionId(preferences.get("service.bankaccountlist.applicationId", "001"));
            // poRequest.setCanalId(preferences.get("service.bankaccountlist.canalId", "001"));
            //poRequest.setUsuarioId(citizenId);

            och.infatlan.hn.ws.acd101.out.consultasaldov2.DTParametroAdicionalItem adicionalColeccion = new och.infatlan.hn.ws.acd101.out.consultasaldov2.DTParametroAdicionalItem();
            adicionalColeccion.setLinea(new BigInteger("0"));
            adicionalColeccion.setTipoRegistro(registerType);
            adicionalColeccion.setValor(userId);

            och.infatlan.hn.ws.acd101.out.consultasaldov2.DTParametroAdicionalColeccion dtParametroAdicionalColeccion = new och.infatlan.hn.ws.acd101.out.consultasaldov2.DTParametroAdicionalColeccion();
            dtParametroAdicionalColeccion.getParametroAdicionalItem().add(adicionalColeccion);
            poRequest.setParametroAdicionalColeccion(dtParametroAdicionalColeccion);

            och.infatlan.hn.ws.acd101.out.consultasaldov2.DTConsultaSaldoColeccion consultaSaldoColeccion = new och.infatlan.hn.ws.acd101.out.consultasaldov2.DTConsultaSaldoColeccion();
            consultaSaldoColeccion.setTipoCuenta(new BigInteger("0"));
            poRequest.setConsultaSaldoColeccion(consultaSaldoColeccion);

            DTConsultaSaldoV2Response poResponse = coreConsultaClient.siOSConsultaSaldoV2(poRequest);

            if (poResponse != null && poResponse.getRespuesta().getEstado().getCodigo().equals(Constants.BANK_SUCCESS_STATUS_CODE)) {
                List<DTCuentaItemV2> dtCuentas =
                        poResponse.getRespuesta().getProductoColeccionV2().getCuentaColeccionV2() != null ?
                                poResponse.getRespuesta().getProductoColeccionV2().getCuentaColeccionV2().getCuentaItemV2() : new ArrayList<DTCuentaItemV2>();

                for (DTCuentaItemV2 currentAccount : dtCuentas) {
                    PaymentInstrument newBankAccount = new PaymentInstrument();
                    PaymentInstrumentType piType = new PaymentInstrumentType();
                    piType.setId(Constants.PI_TYPE_BANK_ACCOUNT_ID);
                    if ((currentAccount.getTipo().equals(Constants.BANK_ACCOUNT_TYPE_1) || currentAccount.getTipo().equals(Constants.BANK_ACCOUNT_TYPE_2)) && currentAccount.getMoneda().equals(Constants.HN_CURRENCY)) {
                        newBankAccount.setStrIdentifier(currentAccount.getNumeroCuenta());
                        newBankAccount.setAlias(currentAccount.getAlias());
                        Double balance = currentAccount.getSaldoColeccion().getDisponibleLps();
                        newBankAccount.setBalance(balance);
                        newBankAccount.setPaymentInstrumentType(piType);
                        retVal.add(newBankAccount);
                    }
                }
            }else{
                LOG.error(AuthorizerError.CUSTOM_ERROR_GETTING_ACCOUNTS_SERVICE + " || "
                        + poResponse.getRespuesta().getEstado().getCodigo() + "|"
                        + poResponse.getRespuesta().getEstado().getDescripcion() + "|"
                        + poResponse.getRespuesta().getEstado().getDetalleTecnico() + "||");
                throw new ModelCustomErrorException(poResponse.getRespuesta().getEstado().getCodigo() + ":"
                        + poResponse.getRespuesta().getEstado().getDescripcion(),AuthorizerError.CUSTOM_ERROR_GETTING_ACCOUNTS_SERVICE);
            }

        } catch (ModelCustomErrorException ent) {
            throw ent;
        } catch (MalformedURLException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ModelCustomErrorException(ex.getMessage(), AuthorizerError.MALFORMED_URL_GETTING_ACCOUNT_INFORMATION_ESB);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ModelCustomErrorException(e.getMessage(), AuthorizerError.UNEXPECTED_ERROR_GETTING_ACCOUNT_INFORMATION_ESB);
        }

        return retVal;
    }
}