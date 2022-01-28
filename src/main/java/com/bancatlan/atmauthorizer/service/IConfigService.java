package com.bancatlan.atmauthorizer.service;

import com.bancatlan.atmauthorizer.model.Config;

public interface IConfigService extends ICRUD<Config> {
    Config getConfigByPropertyName(String propertyName);
}
