package com.bancatlan.atmauthorizer.component.impl;

import com.bancatlan.atmauthorizer.api.model.ResponsePrivilege;
import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.component.IUtilComponent;
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

    @Autowired
    private IUtilComponent utilComponent;

    @Override
    public ResponsePrivilege AccountAndUserHavePrivilege(String username, String accountNumber) {
        String urlPrivilege = utilComponent.getConfigValueByPropertyName(Constants.STR_URL_SERVICE_PRIVILEGE,Constants.STR_URL_SERVICE_PRIVILEGE);
        String isScheduledSavingsAccount = utilComponent.getConfigValueByPropertyName(Constants.STR_IS_SCHEDULED_SAVINGS_ACCOUNTS,Constants.STR_IS_SCHEDULED_SAVINGS_ACCOUNTS);

        RestTemplate restTemplate =new RestTemplate();
        ResponsePrivilege responsePrivilege = restTemplate.getForObject(urlPrivilege+"/privilege/verify/account-user?accountNumber="+accountNumber+"&username=" +username+"&isSavingsAccountValidation="+ isScheduledSavingsAccount, ResponsePrivilege.class);

        return responsePrivilege;
    }

    @Override
    public Boolean isPrivilegeValidationActive() {
        String isPrivilegeValidationActive = utilComponent.getConfigValueByPropertyName(Constants.STR_IS_PRIVILEGE_VALIDATION_ACTIVE,Constants.STR_IS_PRIVILEGE_VALIDATION_ACTIVE);
        if(isPrivilegeValidationActive.equals("1")){
            return true;
        }
        return  false;
    }


}
