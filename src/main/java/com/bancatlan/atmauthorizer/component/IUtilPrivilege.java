package com.bancatlan.atmauthorizer.component;

import com.bancatlan.atmauthorizer.api.model.ResponsePrivilege;
import com.bancatlan.atmauthorizer.exception.PrivilegeError;

public interface IUtilPrivilege {
    ResponsePrivilege AccountAndUserHavePrivilege(String username, String AccountNumber);
    Boolean isPrivilegeValidationActive();
    PrivilegeError errorMessage (String status);

}
