package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.component.IUtilComponent;
import com.bancatlan.atmauthorizer.component.impl.UtilComponentImpl;
import com.bancatlan.atmauthorizer.exception.AuthorizerError;
import com.bancatlan.atmauthorizer.exception.ModelCustomErrorException;
import com.bancatlan.atmauthorizer.model.*;
import com.bancatlan.atmauthorizer.service.IBankService;
import com.bancatlan.atmauthorizer.service.IConfigService;
import com.bancatlan.atmauthorizer.service.IPaymentInstrumentService;
import hn.bancatlan.rat002._1_0.out.registrotransaccionatm.*;
import infatlan.hn.acd169.out.congelamientocuentas.*;
import infatlan.hn.entrust.core.external.message.*;
import infatlan.hn.entrust.core.external.message.DTPeticionGeneral;
import och.infatlan.hn.ws.acd088.out.transferenciacontable.*;
import och.infatlan.hn.ws.acd088.out.transferenciacontable.DTEstado;
import och.infatlan.hn.ws.acd088.out.transferenciacontable.DTParametroAdicionalColeccion;
import och.infatlan.hn.ws.acd088.out.transferenciacontable.DTParametroAdicionalItem;
import och.infatlan.hn.ws.acd101.out.consultasaldov2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.ws.BindingProvider;
import java.math.BigInteger;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Value("${bank.service.po.dev.username}")
    String busIntegrationDevUsername;

    @Value("${bank.service.po.dev.password}")
    String busIntegrationDevPassword;

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

    /**
     * Notification
     * */
    @Value("${bank.service.freeze.transaction.id}")
    String freezeTransactionId;

    @Value("${bank.service.freeze.application.id}")
    String freezeApplicationId;

    @Value("${bank.service.freeze.institution.id}")
    String freezeInstitutionId;

    @Value("${bank.service.notification.canal.id}")
    String freezeChannelId;

    @Value("${bank.service.freeze.sucursal.id}")
    String freezeSucursalId;

    @Value("${bank.service.freeze.days}")
    String freezeDays;

    /**
     * references Files and Endpoints WSDL's
     */
    @Value("${spring.resources.static-locations}")
    String absolutePathWSDLResources;

    @Value("${bus-integration.wsdl.balance-name}")
    String balanceWSDLName;

    @Value("${bus-integration.wsdl.transfer-name}")
    String transferWSDLName;

    @Value("${bus-integration.wsdl.notification-name}")
    String notificationWSDLName;

    @Value("${bus-integration.wsdl.freeze-name}")
    String freezeWSDLName;

    //regiter transaction
    @Value("${po.txn-register.username}")
    String txnRegisterUsername;
    @Value("${po.txn-register.password}")
    String txnRegisterPassword;
    @Value("${po.txn-register.endpoint}")
    String txnRegisterSOAPEndpoint;
    @Value("${bus-integration.wsdl.txn-register-name}")
    String registerWSDLName;

    //freeze
    @Value("${po.freeze.password}")
    String freezePassword;
    @Value("${po.freeze.username}")
    String freezeUsername;
    @Value("${po.freeze.endpoint}")
    String freezeSOAPEndpoint;

    //accounting-transfer
    @Value("${po.accounting-transfer.endpoint}")
    String transferSOAPEndpoint;
    @Value("${po.accounting-transfer.username}")
    String transferUsername;
    @Value("${po.accounting-transfer.password}")
    String transferPassword;

    //notification
    @Value("${po.notification.endpoint}")
    String notificationSOAPEndpoint;
    @Value("${po.notification.username}")
    String notificationUsername;
    @Value("${po.notification.password}")
    String notificationPassword;

    @Autowired
    IUtilComponent utilComponent;

    @Autowired
    private IPaymentInstrumentService paymentInstrumentService;


    @Autowired
    private IConfigService configService;

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
                return new PasswordAuthentication(notificationUsername,
                        notificationPassword.toCharArray());
            }
        });
        String urlS = absolutePathWSDLResources + notificationWSDLName;
        URL url;
        try {
            url = new URL(urlS);
            SIOSEjecutarEnvioNotificacionService port = new SIOSEjecutarEnvioNotificacionService(url);
            SIOSEjecutarEnvioNotificacion sms = port.getHTTPPort();

            //endpoint url config
            BindingProvider provider = (BindingProvider) sms;
            provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, notificationSOAPEndpoint);

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
            peticion.setDispositivoId(cellPhone);

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
            LOG.error("SIOSEjecutarEnvioNotificacion ModelCustomErrorException exception =>  message {}, entire object error {}",ent.getMessage(), ent);
        } catch (MalformedURLException ex) {
            LOG.error("SIOSEjecutarEnvioNotificacion MalformedURLException exception =>  message {}, entire object error {}",ex.getMessage(), ex);
            //throw new ModelCustomErrorException(ex.getMessage(), AuthorizerError.MALFORMED_URL);
        } catch (Exception e) {
            LOG.error("SIOSEjecutarEnvioNotificacion generic exception =>  message {}, entire object error {}",e.getMessage(), e);
        }

        return true;
    }

    @Override
    public String transferMoney(String accountDebit, String accountCredit, Double amount, Long ref, String action, String customComment,String useCase) {
        LOG.info("transferMoney: ");
        LOG.info("accountDebit: " + accountDebit + " accountCredit: " + accountCredit + " amount: " + amount + " comment: " + customComment);
        //String toAccNum, String fromAccNum, double payerNetAmount, Long transactionId, Integer useCaseId, String customDesc, Long customerId
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(transferUsername,
                        transferPassword.toCharArray());
            }
        });
        String urlS = absolutePathWSDLResources + transferWSDLName;
        LOG.info("URL: {}", urlS);
        String uniqueTransNum = "";
        URL url;
        try {
            url = new URL(urlS);
            SIOSTransferenciaContableService port = new SIOSTransferenciaContableService(url);
            SIOSTransferenciaContable coreBankingTransferClient = port.getHTTPPort();

            DTTransferenciaContable mtTransferenciaContable = new DTTransferenciaContable();
            mtTransferenciaContable.setActivarMultipleEntrada(BigInteger.ZERO);
            mtTransferenciaContable.setActivarParametroAdicional("");
            Config configIssuer = configService.getConfigByPropertyName(Constants.STR_ACCOUNTING_TRANSFERS_ID + useCase);
            String selectedIssuerId = (configIssuer != null && configIssuer.getPropertyValue() != null && !configIssuer.getPropertyValue().equals("")) ? configIssuer.getPropertyValue() : configService.getConfigByPropertyName(Constants.STR_ACCOUNTING_TRANSFERS_DEFAULT_ID ).getPropertyValue();
            mtTransferenciaContable.setTransaccionId(selectedIssuerId);
            mtTransferenciaContable.setAplicacionId(transferApplicationId);
            mtTransferenciaContable.setPaisId(BigInteger.ZERO);
            mtTransferenciaContable.setEmpresaId(BigInteger.ZERO);
            mtTransferenciaContable.setCanalId(utilComponent.getAccountingTranferChannelId(useCase));

            och.infatlan.hn.ws.acd088.out.transferenciacontable.DTIdentificadorColeccion identificadorColeccion = new och.infatlan.hn.ws.acd088.out.transferenciacontable.DTIdentificadorColeccion();
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

            transferenciaContableItem.setDebitoDescripcion(customComment);
            transferenciaContableItem.setComentario(customComment + "TRANSFER_TESTQA");
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
            transferenciaContableItem.setNumeroReferencia(ref);
            if (action != null && !action.equals("")) {
                transferenciaContableItem.setRespuesta(action);
            }

            och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoColeccion campoCollection = new och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoColeccion();
            och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoItem campoItem = new och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoItem();
            campoItem.setLinea(new BigInteger(transferLine));
            campoItem.setTipoCampo(transferFieldType);

            campoItem.setValor("174");//Todo preguntar a Oscar
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
    public String transferMoneyProcess(String accountDebit, String accountCredit, Double amount, Long ref, String action, String customComment,String useCase) {
        LOG.info("transferMoney in bach process: accountDebit: {} , accountCredit {} , amount: {} , comment: {}", accountDebit, accountCredit, amount, customComment);
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(transferUsername,
                        transferPassword.toCharArray());
            }
        });
        String urlWs = absolutePathWSDLResources + transferWSDLName;
        String uniqueTransNum = Constants.STR_DASH_SEPARATOR;
        URL url;
        try {
            url = new URL(urlWs);
            SIOSTransferenciaContableService port = new SIOSTransferenciaContableService(url);
            SIOSTransferenciaContable coreBankingTransferClient = port.getHTTPPort();

            DTTransferenciaContable mtTransferenciaContable = new DTTransferenciaContable();
            mtTransferenciaContable.setActivarMultipleEntrada(BigInteger.ZERO);
            mtTransferenciaContable.setActivarParametroAdicional("");
            Config configIssuer = configService.getConfigByPropertyName(Constants.STR_ACCOUNTING_TRANSFERS_ID + useCase);
            String selectedIssuerId = (configIssuer != null && configIssuer.getPropertyValue() != null && !configIssuer.getPropertyValue().equals("")) ? configIssuer.getPropertyValue() : configService.getConfigByPropertyName(Constants.STR_ACCOUNTING_TRANSFERS_DEFAULT_ID ).getPropertyValue();
            mtTransferenciaContable.setTransaccionId(selectedIssuerId);
            mtTransferenciaContable.setAplicacionId(transferApplicationId);
            mtTransferenciaContable.setPaisId(BigInteger.ZERO);
            mtTransferenciaContable.setEmpresaId(BigInteger.ZERO);
            mtTransferenciaContable.setCanalId(utilComponent.getAccountingTranferChannelId(useCase));


            och.infatlan.hn.ws.acd088.out.transferenciacontable.DTIdentificadorColeccion identificadorColeccion = new och.infatlan.hn.ws.acd088.out.transferenciacontable.DTIdentificadorColeccion();
            identificadorColeccion.setOmniCanal("");
            mtTransferenciaContable.getIdentificadorColeccion().add(identificadorColeccion);

            DTTransferenciaContableColeccion transferenciaContableColeccion = new DTTransferenciaContableColeccion();

            DTTransferenciaContableItem transferenciaContableItem = new DTTransferenciaContableItem();

            transferenciaContableItem.setLinea(BigInteger.ONE);
            transferenciaContableItem.setAccion(transferAction);
            transferenciaContableItem.setValidar(transferValidate);
            transferenciaContableItem.setCuentaDebito(accountDebit);
            transferenciaContableItem.setMonedaDebito(transferDebitCurrency);

            transferenciaContableItem.setDebitoDescripcion(customComment);
            transferenciaContableItem.setComentario(customComment + "DEFROST_TRANSFER");
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
            transferenciaContableItem.setNumeroReferencia(ref);
            if (action != null && !action.equals("")) {
                transferenciaContableItem.setRespuesta(action);
            }

            och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoColeccion campoCollection = new och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoColeccion();
            och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoItem campoItem = new och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoItem();
            campoItem.setLinea(new BigInteger(transferLine));
            campoItem.setTipoCampo(transferFieldType);
            campoItem.setValor("800");
            campoCollection.getCampoItem().add(campoItem);

            transferenciaContableItem.setCampoColeccion(campoCollection);
            transferenciaContableColeccion.setTransferenciaContableItem(transferenciaContableItem);
            mtTransferenciaContable.setTransferenciaContableColeccion(transferenciaContableColeccion);
            DTTransferenciaContableResponse response = coreBankingTransferClient
                    .siOSTransferenciaContable(mtTransferenciaContable);

            DTTransferenciaContableItem responseTransferenciaContableItem = response.getRespuesta()
                    .getTransferenciaContableColeccion().getTransferenciaContableItem();
            LOG.info("{} service Response -> Comment : {}", Constants.STR_ACCOUNTING_TRANSFER_SERVICE_NAME, responseTransferenciaContableItem.getComentario());

            DTEstado responseState = response.getRespuesta().getEstado();
            LOG.info("{} service Response -> State -> Description: {}", Constants.STR_ACCOUNTING_TRANSFER_SERVICE_NAME, responseState.getDescripcion());
            // Check if the response is successful
            if (Constants.BANK_SUCCESS_STATUS_CODE.equals(responseState.getCodigo())) {
                // Response is successful.
                uniqueTransNum = "" + responseTransferenciaContableItem.getNumeroTransaccionUnico();
                LOG.debug("{} successful, txnUniqueNumber:{} ", Constants.STR_ACCOUNTING_TRANSFER_SERVICE_NAME, uniqueTransNum);
            } else {
                uniqueTransNum = Constants.STR_CUSTOM_ERR;// indicates Error.
                LOG.error("{}: {} , Type: {}, Code: {}, description {} ", Constants.STR_ACCOUNTING_TRANSFER_SERVICE_NAME,
                        AuthorizerError.CUSTOM_ERROR_ACCOUNTING_TRANSFER_ESB, responseState.getTipo(),
                        responseState.getCodigo(), responseState.getDescripcion());
            }

        } catch (Exception e) {
            uniqueTransNum = Constants.STR_EXCEPTION_ERR;
            LOG.error("Exception in {}, {}, message {}, error ", Constants.STR_ACCOUNTING_TRANSFER_SERVICE_NAME, AuthorizerError.ERROR_ACCOUNTING_TRANSFER_FROM_BANK, e.getMessage(), e.getCause());
        }

        return uniqueTransNum;
    }

    @Override
    public String transferMoneyProcess(Transaction txn, boolean hasApplyFreezing) {
        Transaction creatorTxn = txn.getVoucher().getTxnCreatedBy();
        PaymentInstrument payerPI = creatorTxn.getPayerPaymentInstrument();
        Config configAccount = configService.getConfigByPropertyName(Constants.STR_USE_CASE_ACCOUNTING_CONFIG_PREFIX + creatorTxn.getUseCase().getId().toString());
        String accountCredit = (configAccount != null && configAccount.getPropertyValue() != null && !configAccount.getPropertyValue().equals("")) ? configAccount.getPropertyValue() : configService.getConfigByPropertyName(Constants.STR_USE_CASE_ACCOUNT_DEFAULT).getPropertyValue();
        String accountDebit = txn.getVoucher().getTxnCreatedBy().getPayerPaymentInstrument().getStrIdentifier();
        Customer payee = creatorTxn.getPayee();
        String prefix_core_desc = utilComponent.getBankCommentPrefix(creatorTxn.getUseCase().getId().intValue());
        String customComment = prefix_core_desc + Constants.STR_DASH_SEPARATOR + txn.getId() + Constants.STR_DASH_SEPARATOR + txn.getUseCase().getId() + Constants.STR_DASH_SEPARATOR + payerPI.getStrIdentifier() + Constants.STR_DASH_SEPARATOR + payee.getMsisdn();
        String useCase = creatorTxn.getUseCase().getId().toString();
        Double amount = txn.getAmount();

        String action = hasApplyFreezing ? Constants.BANK_ACTION_DEFROST : "";

        LOG.info("transferMoneyProcess: accountDebit: {}, accountCredit {}, amount: {}, comment: {}, hasApplyFreezing{}",
                accountDebit, accountCredit, amount, customComment, hasApplyFreezing);
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(transferUsername,
                        transferPassword.toCharArray());
            }
        });
        String urlWs = absolutePathWSDLResources + transferWSDLName;
        String uniqueTransNum = Constants.STR_DASH_SEPARATOR;
        URL url;
        try {
            url = new URL(urlWs);
            SIOSTransferenciaContableService port = new SIOSTransferenciaContableService(url);
            SIOSTransferenciaContable coreBankingTransferClient = port.getHTTPPort();

            //endpoint url config
            BindingProvider provider = (BindingProvider) coreBankingTransferClient;
            provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, transferSOAPEndpoint);

            DTTransferenciaContable mtTransferenciaContable = new DTTransferenciaContable();
            mtTransferenciaContable.setActivarMultipleEntrada(BigInteger.ZERO);
            mtTransferenciaContable.setActivarParametroAdicional("");
            Config configIssuer = configService.getConfigByPropertyName(Constants.STR_ACCOUNTING_TRANSFERS_ID + useCase);
            String selectedIssuerId = (configIssuer != null && configIssuer.getPropertyValue() != null && !configIssuer.getPropertyValue().equals("")) ? configIssuer.getPropertyValue() : configService.getConfigByPropertyName(Constants.STR_ACCOUNTING_TRANSFERS_DEFAULT_ID ).getPropertyValue();
            mtTransferenciaContable.setTransaccionId(selectedIssuerId);
            mtTransferenciaContable.setAplicacionId(transferApplicationId);
            mtTransferenciaContable.setPaisId(BigInteger.ZERO);
            mtTransferenciaContable.setEmpresaId(BigInteger.ZERO);
            mtTransferenciaContable.setCanalId(utilComponent.getAccountingTranferChannelId(useCase));


            DTParametroAdicionalColeccion parametroAdicionalColeccion = new DTParametroAdicionalColeccion();
            DTParametroAdicionalItem parametroAdicionalItem = new DTParametroAdicionalItem();

            parametroAdicionalItem.setLinea(BigInteger.ONE);
            parametroAdicionalItem.setTipoRegistro(Constants.COD_ATM);
            parametroAdicionalItem.setValor(txn.getStrIdTerminal());


            parametroAdicionalColeccion.getParametroAdicionalItem().add(parametroAdicionalItem);
            mtTransferenciaContable.setParametroAdicionalColeccion(parametroAdicionalColeccion);

            och.infatlan.hn.ws.acd088.out.transferenciacontable.DTIdentificadorColeccion identificadorColeccion = new och.infatlan.hn.ws.acd088.out.transferenciacontable.DTIdentificadorColeccion();
            identificadorColeccion.setOmniCanal("");
            mtTransferenciaContable.getIdentificadorColeccion().add(identificadorColeccion);

            DTTransferenciaContableColeccion transferenciaContableColeccion = new DTTransferenciaContableColeccion();

            DTTransferenciaContableItem transferenciaContableItem = new DTTransferenciaContableItem();

            transferenciaContableItem.setLinea(BigInteger.ONE);
            transferenciaContableItem.setAccion(transferAction);
            transferenciaContableItem.setValidar(transferValidate);
            transferenciaContableItem.setCuentaDebito(accountDebit);
            transferenciaContableItem.setMonedaDebito(transferDebitCurrency);

            transferenciaContableItem.setDebitoDescripcion(customComment);
            transferenciaContableItem.setComentario(customComment + "DEFROST_TRANSFER");
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
            transferenciaContableItem.setNumeroReferencia(creatorTxn.getId());
            transferenciaContableItem.setRespuesta(action);


            och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoColeccion campoCollection = new och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoColeccion();
            och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoItem campoItem = new och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoItem();
            campoItem.setLinea(new BigInteger(transferLine));
            campoItem.setTipoCampo(transferFieldType);
            campoItem.setValor(txn.getUseCase().getId().toString());
            campoCollection.getCampoItem().add(campoItem);

            campoItem = new och.infatlan.hn.ws.acd088.out.transferenciacontable.DTCampoItem();
            campoItem.setLinea(new BigInteger(transferLine));
            campoItem.setTipoCampo(Constants.NRO_AUTORIZADOR);
            campoItem.setValor(txn.getStrAuthorizationCode());
            campoCollection.getCampoItem().add(campoItem);

            transferenciaContableItem.setCampoColeccion(campoCollection);
            transferenciaContableColeccion.setTransferenciaContableItem(transferenciaContableItem);
            mtTransferenciaContable.setTransferenciaContableColeccion(transferenciaContableColeccion);
            DTTransferenciaContableResponse response = coreBankingTransferClient
                    .siOSTransferenciaContable(mtTransferenciaContable);

            DTTransferenciaContableItem responseTransferenciaContableItem = response.getRespuesta()
                    .getTransferenciaContableColeccion().getTransferenciaContableItem();
            LOG.info("{} service Response -> Comment : {}", Constants.STR_ACCOUNTING_TRANSFER_SERVICE_NAME, responseTransferenciaContableItem.getComentario());

            DTEstado responseState = response.getRespuesta().getEstado();
            LOG.info("{} service Response -> State -> Description: {}", Constants.STR_ACCOUNTING_TRANSFER_SERVICE_NAME, responseState.getDescripcion());
            // Check if the response is successful
            if (Constants.BANK_SUCCESS_STATUS_CODE.equals(responseState.getCodigo())) {
                uniqueTransNum = "" + responseTransferenciaContableItem.getRespuesta();
                LOG.debug("{} successful, txnUniqueNumber:{} ", Constants.STR_ACCOUNTING_TRANSFER_SERVICE_NAME, responseTransferenciaContableItem.getRespuesta());
            } else {
                String expectedMsgError = String.format("NotaPrev %s En status ACTIVO no existe",
                        creatorTxn.getId().toString());
                if (responseState.getDetalleTecnico().equals(expectedMsgError)) {
                    TxnStatus transitiveStatus = new TxnStatus();
                    transitiveStatus.setId(new Long(Constants.TO_RE_POST_TXN_STATUS));
                    txn.setTxnStatus(transitiveStatus);
                } else {
                    //TODO Analizar que hacer con los otros errors, para enviarlos con estado 23 => para revision manual
                }
                uniqueTransNum = Constants.STR_CUSTOM_ERR;// indicates Error.
                LOG.error("{}: {} , Type: {}, Code: {}, description {} ", Constants.STR_ACCOUNTING_TRANSFER_SERVICE_NAME,
                        AuthorizerError.CUSTOM_ERROR_ACCOUNTING_TRANSFER_ESB, responseState.getTipo(),
                        responseState.getCodigo(), responseState.getDescripcion());
            }

        } catch (Exception e) {
            uniqueTransNum = Constants.STR_EXCEPTION_ERR;
            LOG.error("Exception in {}, {}, message {}, error ", Constants.STR_ACCOUNTING_TRANSFER_SERVICE_NAME, AuthorizerError.ERROR_ACCOUNTING_TRANSFER_FROM_BANK, e.getMessage(), e.getCause());
        }

        return uniqueTransNum;
    }

    @Override
    public String freezeFoundsProcess(String accountDebit, Double amount, Long ref, String action, String userName, String customComment,String useCase) {
        LOG.info("FreezeFounds PROCESS function: comment {}, amount {}, accountDebit {}, action {}", customComment, amount, accountDebit, action);
        String urlS = absolutePathWSDLResources + freezeWSDLName;
        String coreReference = Constants.STR_DASH_SEPARATOR;
        LOG.info("url: {}", urlS);
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime expirationDate = currentDate.plusDays(new Long(freezeDays));
        String bankFormatCurrentDate = currentDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        String bankFormatExpirationDate = expirationDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(freezeUsername,
                        freezePassword.toCharArray());
            }
        });

        URL url;
        try {
            url = new URL(urlS);
            SIOSCongelamientoCuentasService port = new SIOSCongelamientoCuentasService(url);
            SIOSCongelamientoCuentas siosCongelamientoCuentas = port.getHTTPPort();

            BindingProvider provider = (BindingProvider) siosCongelamientoCuentas;
            provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, freezeSOAPEndpoint);

            DTPeticion dtPeticion = new DTPeticion();
            Config configIssuer = configService.getConfigByPropertyName(Constants.STR_FREEZE_FOUND_ACCOUNTING_TRANSFERS_ID + useCase);
            String selectedIssuerId = (configIssuer != null && configIssuer.getPropertyValue() != null && !configIssuer.getPropertyValue().equals("")) ? configIssuer.getPropertyValue() : configService.getConfigByPropertyName(Constants.STR_FREEZE_FOUND_ACCOUNTING_TRANSFERS_DEFAULT_ID ).getPropertyValue();
            dtPeticion.setTransaccionId(selectedIssuerId);
            dtPeticion.setAplicacionId(freezeApplicationId);
            dtPeticion.setPaisId(Constants.HN_COUNTRY2CODE);
            dtPeticion.setInstitucionId(freezeInstitutionId);
            dtPeticion.setRegionId(Constants.STR_QUESTION_MARK);
            dtPeticion.setCanalId(freezeChannelId);
            dtPeticion.setVersion(Constants.STR_QUESTION_MARK);
            dtPeticion.setLlaveSesion(Constants.STR_QUESTION_MARK);
            dtPeticion.setUsuarioId(userName);
            dtPeticion.setToken(Constants.STR_QUESTION_MARK);
            dtPeticion.setDispositivoId(Constants.STR_QUESTION_MARK);
            dtPeticion.setIdentificacion(Constants.STR_QUESTION_MARK);

            infatlan.hn.acd169.out.congelamientocuentas.DTIdentificadorColeccion dtIdentificadorColeccion = new infatlan.hn.acd169.out.congelamientocuentas.DTIdentificadorColeccion();
            dtIdentificadorColeccion.setWas(Constants.STR_QUESTION_MARK);
            dtIdentificadorColeccion.setPi(Constants.STR_QUESTION_MARK);
            dtIdentificadorColeccion.setOmniCanal(Constants.STR_QUESTION_MARK);
            dtIdentificadorColeccion.setRecibo(Constants.STR_QUESTION_MARK);
            dtIdentificadorColeccion.setNumeroTransaccion(Constants.STR_QUESTION_MARK);

            infatlan.hn.acd169.out.congelamientocuentas.DTParametroAdicionalColeccion dtParametroAdicionalColeccion = new infatlan.hn.acd169.out.congelamientocuentas.DTParametroAdicionalColeccion();
            infatlan.hn.acd169.out.congelamientocuentas.DTParametroAdicionalItem dtParametroAdicionalItem = new infatlan.hn.acd169.out.congelamientocuentas.DTParametroAdicionalItem();

            dtParametroAdicionalItem.setLinea(new BigInteger(Constants.STR_VALUE_1));
            dtParametroAdicionalItem.setTipoRegistro(Constants.STR_QUESTION_MARK);
            dtParametroAdicionalItem.setValor(Constants.STR_QUESTION_MARK);

            dtParametroAdicionalColeccion.getParametroAdicionalItem().add(dtParametroAdicionalItem);

            DTPeticionCongelamientoColeccion dtPeticionCongelamientoColeccion = new DTPeticionCongelamientoColeccion();

            DTPeticionCongelamientoItem dtPeticionCongelamientoItem = new DTPeticionCongelamientoItem();

            dtPeticionCongelamientoItem.setLinea(new BigInteger(Constants.STR_VALUE_1));
            dtPeticionCongelamientoItem.setAccion(action);
            dtPeticionCongelamientoItem.setValidar(Constants.BANK_STR_MARK_TRUE);
            dtPeticionCongelamientoItem.setTipoDebito("");
            dtPeticionCongelamientoItem.setCuentaDebito(accountDebit);
            dtPeticionCongelamientoItem.setMonedaDebito(Constants.BANK_HN_CURRENCY);
            dtPeticionCongelamientoItem.setDebitoDescripcion(customComment);
            dtPeticionCongelamientoItem.setComentario(customComment);
            dtPeticionCongelamientoItem.setSucursalDebito(new BigInteger(freezeSucursalId));
            dtPeticionCongelamientoItem.setMontoOriginal(amount.toString());
            dtPeticionCongelamientoItem.setNumeroReferencia(ref.toString());
            dtPeticionCongelamientoItem.setDiasCongelamiento(new BigInteger(freezeDays));
            dtPeticionCongelamientoItem.setFechaProceso(bankFormatCurrentDate);
            dtPeticionCongelamientoItem.setFechaVencimiento(bankFormatExpirationDate);

            dtPeticionCongelamientoColeccion.getPagoServicioItem().add(dtPeticionCongelamientoItem);

            dtPeticion.setIdentificadorColeccion(dtIdentificadorColeccion);
            dtPeticion.setParametroAdicionalColeccion(dtParametroAdicionalColeccion);
            dtPeticion.setPeticionCongelamientoColeccion(dtPeticionCongelamientoColeccion);
            DTRespuesta dtRespuesta = siosCongelamientoCuentas.siOSCongelamientoCuentas(dtPeticion);

            if (dtRespuesta != null && dtRespuesta.getRespuesta() != null && dtRespuesta.getRespuesta().getEstado() != null && dtRespuesta.getRespuesta().getEstado().getCodigo() != null && dtRespuesta.getRespuesta().getEstado().getCodigo().equals(Constants.BANK_SUCCESS_STATUS_CODE)) {
                DTPeticionCongelamientoColeccionResponse dtPeticionCongelamientoItemResponse = dtRespuesta.getRespuesta().getPeticionCongelamientoColeccion();
                if (dtPeticionCongelamientoItemResponse.getPeticionCongelamientoItemResponse() != null &&
                        !dtPeticionCongelamientoItemResponse.getPeticionCongelamientoItemResponse().isEmpty()) {
                    for (DTPeticionCongelamientoItemResponse item :
                            dtPeticionCongelamientoItemResponse.getPeticionCongelamientoItemResponse()) {
                        if (item.getEstado() != null && item.getEstado().getCodigo() != null) {
                            LOG.info("Freeze Bank Service got success: Status => code {}, description {}, Type {}", item.getEstado().getCodigo(), item.getEstado().getDescripcion(), item.getEstado().getTipo());
                            DTPeticionCongelamientoItem dtPeticionCongelamientoItemRes = item.getPagoServicioItem();
                            if (item.getPagoServicioItem() != null && item.getPagoServicioItem().getNumeroTransaccionUnico() != null) {
                                coreReference = item.getPagoServicioItem().getNumeroTransaccionUnico();
                            }
                        } else {
                            coreReference = Constants.STR_CUSTOM_ERR;
                            LOG.error("Error in Freeze Bank Service: Status => code {}, description {}, Type {}, Technical detail {}, custom err {}", item.getEstado().getCodigo(), item.getEstado().getDescripcion(), item.getEstado().getTipo(), item.getEstado().getDetalleTecnico(), AuthorizerError.CUSTOM_ERROR_SERVICE_BANK_FREEZE);
                        }
                    }
                } else {
                    coreReference = Constants.STR_CUSTOM_ERR;
                    LOG.error("Error in Freeze Bank Service: customError {}, codigo {}, description {}, detail {}", AuthorizerError.CUSTOM_ERROR_SERVICE_BANK_FREEZE, dtRespuesta.getRespuesta().getEstado().getCodigo(), dtRespuesta.getRespuesta().getEstado().getDescripcion(), dtRespuesta.getRespuesta().getEstado().getDetalleTecnico());
                }
            } else {
                LOG.error("Error in freeze Bank Service: customError {}", AuthorizerError.CUSTOM_ERROR_SERVICE_BANK_FREEZE);
                if (dtRespuesta != null && dtRespuesta.getRespuesta() != null && dtRespuesta.getRespuesta().getEstado() != null) {
                    LOG.error("Status: code {}", dtRespuesta.getRespuesta().getEstado().getCodigo());
                    LOG.error("Status: description {}", dtRespuesta.getRespuesta().getEstado().getDescripcion());
                    LOG.error("Status: detail {}", dtRespuesta.getRespuesta().getEstado().getDetalleTecnico());
                    LOG.error("Status: type {}", dtRespuesta.getRespuesta().getEstado().getTipo());
                }
            }
        } catch (Exception ex) {
            coreReference = Constants.STR_EXCEPTION_ERR;
            LOG.error("Custom error {}, error message {}, error {}", AuthorizerError.ERROR_SERVICE_BANK_FREEZE, ex.getMessage(), ex);
        }
        return coreReference;
    }

    @Override
    public String freezeFounds(String accountDebit, Double amount, Long ref, String action, String userName, String customComment,String useCase) {
        LOG.info("FreezeFounds function: comment {}, amount {}, accountDebit {}, action {}", customComment, amount, accountDebit, action);
        String urlS = absolutePathWSDLResources + freezeWSDLName;
        String coreReference = "";
        LOG.info("URL: {}", urlS);
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime expirationDate = currentDate.plusDays(new Long(freezeDays));
        String bankFormatCurrentDate = currentDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        String bankFormatExpirationDate = expirationDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(freezeUsername,
                        freezePassword.toCharArray());
            }
        });

        URL url;
        try {
            url = new URL(urlS);
            SIOSCongelamientoCuentasService port = new SIOSCongelamientoCuentasService(url);
            SIOSCongelamientoCuentas siosCongelamientoCuentas = port.getHTTPPort();

            BindingProvider provider = (BindingProvider) siosCongelamientoCuentas;
            provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, freezeSOAPEndpoint);

            DTPeticion dtPeticion = new DTPeticion();
            Config configIssuer = configService.getConfigByPropertyName(Constants.STR_FREEZE_FOUND_ACCOUNTING_TRANSFERS_ID + useCase);
            String selectedIssuerId = (configIssuer != null && configIssuer.getPropertyValue() != null && !configIssuer.getPropertyValue().equals("")) ? configIssuer.getPropertyValue() : configService.getConfigByPropertyName(Constants.STR_FREEZE_FOUND_ACCOUNTING_TRANSFERS_DEFAULT_ID ).getPropertyValue();
            dtPeticion.setTransaccionId(selectedIssuerId);
            dtPeticion.setAplicacionId(freezeApplicationId);
            dtPeticion.setPaisId(Constants.HN_COUNTRY2CODE);
            dtPeticion.setInstitucionId(freezeInstitutionId);
            dtPeticion.setRegionId(Constants.STR_QUESTION_MARK);
            dtPeticion.setCanalId(freezeChannelId);
            dtPeticion.setVersion(Constants.STR_QUESTION_MARK);
            dtPeticion.setLlaveSesion(Constants.STR_QUESTION_MARK);
            dtPeticion.setUsuarioId(userName);
            dtPeticion.setToken(Constants.STR_QUESTION_MARK);
            dtPeticion.setDispositivoId(Constants.STR_QUESTION_MARK);
            //dtPeticion.setClienteCoreId("111378");
            dtPeticion.setIdentificacion(Constants.STR_QUESTION_MARK);

            infatlan.hn.acd169.out.congelamientocuentas.DTIdentificadorColeccion dtIdentificadorColeccion = new infatlan.hn.acd169.out.congelamientocuentas.DTIdentificadorColeccion();
            dtIdentificadorColeccion.setWas(Constants.STR_QUESTION_MARK);
            dtIdentificadorColeccion.setPi(Constants.STR_QUESTION_MARK);
            dtIdentificadorColeccion.setOmniCanal(Constants.STR_QUESTION_MARK);
            dtIdentificadorColeccion.setRecibo(Constants.STR_QUESTION_MARK);
            dtIdentificadorColeccion.setNumeroTransaccion(Constants.STR_QUESTION_MARK);

            infatlan.hn.acd169.out.congelamientocuentas.DTParametroAdicionalColeccion dtParametroAdicionalColeccion = new infatlan.hn.acd169.out.congelamientocuentas.DTParametroAdicionalColeccion();
            infatlan.hn.acd169.out.congelamientocuentas.DTParametroAdicionalItem dtParametroAdicionalItem = new infatlan.hn.acd169.out.congelamientocuentas.DTParametroAdicionalItem();

            dtParametroAdicionalItem.setLinea(new BigInteger(Constants.STR_VALUE_1));
            dtParametroAdicionalItem.setTipoRegistro(Constants.STR_QUESTION_MARK);
            dtParametroAdicionalItem.setValor(Constants.STR_QUESTION_MARK);

            dtParametroAdicionalColeccion.getParametroAdicionalItem().add(dtParametroAdicionalItem);

            DTPeticionCongelamientoColeccion dtPeticionCongelamientoColeccion = new DTPeticionCongelamientoColeccion();

            DTPeticionCongelamientoItem dtPeticionCongelamientoItem = new DTPeticionCongelamientoItem();

            dtPeticionCongelamientoItem.setLinea(new BigInteger(Constants.STR_VALUE_1));
            dtPeticionCongelamientoItem.setAccion(action);
            dtPeticionCongelamientoItem.setValidar(Constants.BANK_STR_MARK_TRUE);
            dtPeticionCongelamientoItem.setTipoDebito("");
            dtPeticionCongelamientoItem.setCuentaDebito(accountDebit);
            dtPeticionCongelamientoItem.setMonedaDebito(Constants.BANK_HN_CURRENCY);
            //dtPeticionCongelamientoItem.setTitularDebito("");
            dtPeticionCongelamientoItem.setDebitoDescripcion(customComment);
            dtPeticionCongelamientoItem.setComentario(customComment);
            //dtPeticionCongelamientoItem.setMovimientoDebito("");
            dtPeticionCongelamientoItem.setSucursalDebito(new BigInteger(freezeSucursalId));
            //dtPeticionCongelamientoItem.setTipoCredito("");
            dtPeticionCongelamientoItem.setMontoOriginal(amount.toString());
            //dtPeticionCongelamientoItem.setMontoDebito("");
            //dtPeticionCongelamientoItem.setOrigenDivisa("");
            //dtPeticionCongelamientoItem.setFuente("");
            //dtPeticionCongelamientoItem.setNumeroTransaccionUnico("");
            dtPeticionCongelamientoItem.setNumeroReferencia(ref.toString());
            dtPeticionCongelamientoItem.setDiasCongelamiento(new BigInteger(freezeDays));
            //dtPeticionCongelamientoItem.setUsuarioOperador("");
            //dtPeticionCongelamientoItem.setUsuarioAutorizador("");
            dtPeticionCongelamientoItem.setFechaProceso(bankFormatCurrentDate);
            dtPeticionCongelamientoItem.setFechaVencimiento(bankFormatExpirationDate);
            //dtPeticionCongelamientoItem.setRespuesta("");


            dtPeticionCongelamientoColeccion.getPagoServicioItem().add(dtPeticionCongelamientoItem);


            dtPeticion.setIdentificadorColeccion(dtIdentificadorColeccion);
            dtPeticion.setParametroAdicionalColeccion(dtParametroAdicionalColeccion);
            dtPeticion.setPeticionCongelamientoColeccion(dtPeticionCongelamientoColeccion);
            DTRespuesta dtRespuesta = siosCongelamientoCuentas.siOSCongelamientoCuentas(dtPeticion);

            if (dtRespuesta != null && dtRespuesta.getRespuesta() != null && dtRespuesta.getRespuesta().getEstado() != null && dtRespuesta.getRespuesta().getEstado().getCodigo() != null && dtRespuesta.getRespuesta().getEstado().getCodigo().equals(Constants.BANK_SUCCESS_STATUS_CODE)) {
                DTPeticionCongelamientoColeccionResponse dtPeticionCongelamientoItemResponse = dtRespuesta.getRespuesta().getPeticionCongelamientoColeccion();
                if (dtPeticionCongelamientoItemResponse.getPeticionCongelamientoItemResponse() != null &&
                        !dtPeticionCongelamientoItemResponse.getPeticionCongelamientoItemResponse().isEmpty()) {
                    for (DTPeticionCongelamientoItemResponse item :
                            dtPeticionCongelamientoItemResponse.getPeticionCongelamientoItemResponse()) {
                        if (item.getEstado() != null && item.getEstado().getCodigo() != null) {
                            LOG.info("Freeze Bank Service got success: Status => code {}, description {}, Type {}", item.getEstado().getCodigo(), item.getEstado().getDescripcion(), item.getEstado().getTipo());
                            DTPeticionCongelamientoItem dtPeticionCongelamientoItemRes = item.getPagoServicioItem();
                            if (item.getPagoServicioItem() != null && item.getPagoServicioItem().getNumeroTransaccionUnico() != null) {
                                coreReference = item.getPagoServicioItem().getNumeroTransaccionUnico();
                            }
                        } else {
                            LOG.error("Error in Freeze Bank Service: Status => code {}, description {}, Type {}, Technical detail {}", item.getEstado().getCodigo(), item.getEstado().getDescripcion(), item.getEstado().getTipo(), item.getEstado().getDetalleTecnico());
                            throw new ModelCustomErrorException(item.getEstado().getDetalleTecnico(), AuthorizerError.CUSTOM_ERROR_SERVICE_BANK_FREEZE);
                        }
                    }
                } else {
                    LOG.error("Error in Freeze Bank Service: Response {}, customError {}", dtRespuesta, AuthorizerError.CUSTOM_ERROR_SERVICE_BANK_FREEZE);
                    throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.CUSTOM_ERROR_SERVICE_BANK_FREEZE);
                }
            } else {
                LOG.error("Error in Freeze Bank Service: Response {}, customError {}", dtRespuesta, AuthorizerError.CUSTOM_ERROR_SERVICE_BANK_FREEZE);
                throw new ModelCustomErrorException(Constants.CUSTOM_MESSAGE_ERROR, AuthorizerError.CUSTOM_ERROR_SERVICE_BANK_FREEZE);
            }

        } catch (Exception ex) {
            LOG.error(AuthorizerError.ERROR_SERVICE_BANK_FREEZE.toString(), ex);
            throw new ModelCustomErrorException(
                    ex.getMessage(), AuthorizerError.ERROR_SERVICE_BANK_FREEZE);
        }
        return coreReference;
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
                    if ((currentAccount.getTipo().equals(Constants.BANK_ACCOUNT_TYPE_1) || currentAccount.getTipo().equals(Constants.BANK_ACCOUNT_TYPE_2)) && currentAccount.getMoneda().equals(Constants.BANK_HN_CURRENCY)) {
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

    @Override
    public Boolean sendWithdrawalToAccountingClosing(Transaction startTxn,Transaction withdrawalTxn) {
        LOG.info("SendWithdrawalToAccountingClosing Summary:");
        LOG.info("idAdquirente {}", Constants.ACQUIRING_ID);
        LOG.info("cantidad, monto {}", withdrawalTxn.getAmount());
        LOG.info("numeroTarjeta {}", withdrawalTxn.getStrIdentifierPayee());
        LOG.info("monedaTransaccion {}", startTxn.getCurrency().getCode());
        LOG.info("Fecha Procesamiento {}", utilComponent.getProcessingDate(withdrawalTxn.getCreationDate()));
        LOG.info("use_case for config {}", startTxn.getUseCase().getId().toString());
        LOG.info("referencia {}", withdrawalTxn.getChannelReference());
        LOG.info("codigoRespuesta {}", Constants.ATM_SUCCESS_STATUS_CODE);
        LOG.info("identificacionTerminal {}", withdrawalTxn.getStrIdTerminal());
        LOG.info("tiempoProcesamiento {}", utilComponent.getProcessingTime(withdrawalTxn.getStrIdentifierPayer()));
        LOG.info("tipoTransaccion {}", utilComponent.getProcessingCode(withdrawalTxn.getChannelId()));
        LOG.info("idAutorizacion {}", withdrawalTxn.getStrAuthorizationCode().trim());

        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(txnRegisterUsername,
                        txnRegisterPassword.toCharArray());
            }
        });

        try {
            String urlService = absolutePathWSDLResources + registerWSDLName;
            LOG.info("path txnRegister wsdl: {}", urlService);
            URL url;
            url = new URL(urlService);


            SIOSRegistroTransaccionATMService port = new SIOSRegistroTransaccionATMService(url);
            SIOSRegistroTransaccionATM registroTransaccionATM = port.getHTTPPort();

            BindingProvider provider = (BindingProvider) registroTransaccionATM;
            provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, txnRegisterSOAPEndpoint);

            DTRegistroTransaccionATMRequest dtRegistroTransaccionATMRequest = new DTRegistroTransaccionATMRequest();
            hn.bancatlan.rat002._1_0.out.registrotransaccionatm.DTPeticionGeneral dtPeticionGeneral = new hn.bancatlan.rat002._1_0.out.registrotransaccionatm.DTPeticionGeneral();
            dtPeticionGeneral.setTransaccionId(Constants.STR_QUESTION_MARK);
            dtPeticionGeneral.setAplicacionId(Constants.STR_QUESTION_MARK);
            dtPeticionGeneral.setPaisId(Constants.STR_QUESTION_MARK);
            dtPeticionGeneral.setInstitucionId(Constants.STR_QUESTION_MARK);
            dtPeticionGeneral.setRegionId(Constants.STR_QUESTION_MARK);
            dtPeticionGeneral.setCanalId(Constants.STR_QUESTION_MARK);
            dtPeticionGeneral.setIdioma(Constants.STR_QUESTION_MARK);
            dtPeticionGeneral.setVersion(Constants.STR_QUESTION_MARK);
            dtPeticionGeneral.setLlaveSesion(Constants.STR_QUESTION_MARK);
            dtPeticionGeneral.setUsuarioId(Constants.STR_QUESTION_MARK);
            dtPeticionGeneral.setToken(Constants.STR_QUESTION_MARK);
            dtPeticionGeneral.setClienteCoreId(Constants.STR_QUESTION_MARK);
            dtPeticionGeneral.setIp(Constants.STR_QUESTION_MARK);
            dtPeticionGeneral.setDispositivoId(Constants.STR_QUESTION_MARK);
            dtPeticionGeneral.setPaso(Constants.STR_QUESTION_MARK);
            dtPeticionGeneral.setAmbiente(Constants.STR_QUESTION_MARK);


            DTRegistroTransaccionATM dtRegistroTransaccionATM = new DTRegistroTransaccionATM();
            dtRegistroTransaccionATM.setIdAdquirente(Constants.ACQUIRING_ID);
            dtRegistroTransaccionATM.setCantidad(withdrawalTxn.getAmount().toString());
            dtRegistroTransaccionATM.setMonto(withdrawalTxn.getAmount().toString());
            dtRegistroTransaccionATM.setNumeroTarjeta(withdrawalTxn.getStrIdentifierPayee());//F2
            dtRegistroTransaccionATM.setMonedaTransaccion(utilComponent.getCurrencyTransactionCode(startTxn.getCurrency().getCode()));
            dtRegistroTransaccionATM.setFechaProcesamiento(utilComponent.getProcessingDate(withdrawalTxn.getCreationDate()));//F7

            //switch config
            Config configIssuer = configService.getConfigByPropertyName(Constants.STR_USE_CASE_ISSUER_CONFIG_PREFIX + startTxn.getUseCase().getId().toString());
            String selectedIssuerId = (configIssuer != null && configIssuer.getPropertyValue() != null && !configIssuer.getPropertyValue().equals("")) ? configIssuer.getPropertyValue() : configService.getConfigByPropertyName(Constants.STR_USE_CASE_ISSUER_DEFAULT).getPropertyValue();
            dtRegistroTransaccionATM.setIdEmisor(selectedIssuerId);
            LOG.info("idEmisor map use_case config {}", selectedIssuerId);
            dtRegistroTransaccionATM.setReferencia(withdrawalTxn.getChannelReference());//F37 o AtmReference
            dtRegistroTransaccionATM.setCodigoRespuesta(Constants.ATM_SUCCESS_STATUS_CODE);
            dtRegistroTransaccionATM.setIdentificacionTerminal(withdrawalTxn.getStrIdTerminal());
            dtRegistroTransaccionATM.setTiempoProcesamiento(utilComponent.getProcessingTime(withdrawalTxn.getStrIdentifierPayer()));//F3
            dtRegistroTransaccionATM.setTipoTransaccion(utilComponent.getProcessingCode(withdrawalTxn.getChannelId()));//F3 substring(1,2)
            dtRegistroTransaccionATM.setIdAutorizacion(withdrawalTxn.getStrAuthorizationCode().trim());

            dtRegistroTransaccionATMRequest.setPeticionGeneral(dtPeticionGeneral);
            dtRegistroTransaccionATMRequest.setRegistroTransaccionATM(dtRegistroTransaccionATM);
            DTRegistroTransaccionATMResponse response = registroTransaccionATM.siOSRegistroTransaccionATM(dtRegistroTransaccionATMRequest);

            if (response != null && response.getEstado() != null && response.getEstado().getCodigo() != null
                    && response.getEstado().getCodigo().equals(BigInteger.ZERO)) {
                LOG.info("Success RegistroTransaccionATM txn => {}", response.getEstado().getDescripcion());
            } else {
                LOG.info("Custom response error RegistroTransaccionATM => {}", response.getEstado().getDescripcion());
                //retVal = false;
            }
        } catch (Exception ex) {
            LOG.info("General exception on RegistroTransaccionATM {}", ex.getMessage());
        }
        return true;
    }
}
