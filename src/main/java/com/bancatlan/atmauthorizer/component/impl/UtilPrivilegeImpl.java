package com.bancatlan.atmauthorizer.component.impl;

import com.bancatlan.atmauthorizer.api.model.ResponsePrivilege;
import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.component.IUtilPrivilege;
import com.bancatlan.atmauthorizer.model.Config;
import com.bancatlan.atmauthorizer.service.IConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UtilPrivilegeImpl implements IUtilPrivilege {
    @Autowired
    private IConfigService configService;

    @Override
    public ResponsePrivilege AccountAndUserHavePrivilege(String username, String accountNumber) {
        Config configIssuer = configService.getConfigByPropertyName(Constants.STR_URL_SERVICE_PRIVILEGE);
        String urlPrivilege = (configIssuer != null && configIssuer.getPropertyValue() != null && !configIssuer.getPropertyValue().equals("")) ? configIssuer.getPropertyValue() : configService.getConfigByPropertyName(Constants.STR_URL_SERVICE_PRIVILEGE ).getPropertyValue();

        configIssuer = configService.getConfigByPropertyName(Constants.STR_IS_SCHEDULED_SAVINGS_ACCOUNTS);
        String isScheduledSavingsAccounts = (configIssuer != null && configIssuer.getPropertyValue() != null && !configIssuer.getPropertyValue().equals("")) ? configIssuer.getPropertyValue() : configService.getConfigByPropertyName(Constants.STR_IS_SCHEDULED_SAVINGS_ACCOUNTS ).getPropertyValue();


        RestTemplate restTemplate =new RestTemplate();
        ResponsePrivilege responsePrivilege = restTemplate.getForObject(urlPrivilege+"/privilege/verify/account-user?accountNumber="+accountNumber+"&username=" +username+"&isSavingsAccountValidation="+ isScheduledSavingsAccounts, ResponsePrivilege.class);
        return responsePrivilege;
    }

}
