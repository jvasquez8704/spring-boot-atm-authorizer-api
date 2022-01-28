package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.model.Config;
import com.bancatlan.atmauthorizer.repo.IConfigRepo;
import com.bancatlan.atmauthorizer.service.IConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConfigServiceImpl implements IConfigService {

    @Autowired
    IConfigRepo repo;

    @Override
    public Config create(Config obj) {
        return null;
    }

    @Override
    public Config update(Config obj) {
        return null;
    }

    @Override
    public List<Config> getAll() {
        return null;
    }

    @Override
    public Config getById(Long id) {
        Optional<Config> config = repo.findById(id);
        return config.isPresent() ? config.get() : null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public Config getConfigByPropertyName(String propertyName) {
        return repo.getConfigByPropertyName(propertyName);
    }
}
