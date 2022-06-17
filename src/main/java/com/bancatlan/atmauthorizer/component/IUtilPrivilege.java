package com.bancatlan.atmauthorizer.component;

import com.bancatlan.atmauthorizer.api.model.ResponsePrivilege;

public interface IUtilPrivilege {
    ResponsePrivilege AccountAndUserHavePrivilege(String username, String AccountNumber);
}
