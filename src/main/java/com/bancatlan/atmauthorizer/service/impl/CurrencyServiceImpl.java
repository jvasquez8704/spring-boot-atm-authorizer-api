package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.model.Currency;
import com.bancatlan.atmauthorizer.repo.ICurrencyRepo;
import com.bancatlan.atmauthorizer.service.ICurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CurrencyServiceImpl implements ICurrencyService {

    @Autowired
    ICurrencyRepo repo;

    @Override
    public Currency create(Currency obj) {
        return null;
    }

    @Override
    public Currency update(Currency obj) {
        return null;
    }

    @Override
    public List<Currency> getAll() {
        return null;
    }

    @Override
    public Currency getById(Long id) {
        Optional<Currency> currency = repo.findById(id);
        return currency.isPresent() ? currency.get() : null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public Currency getCurrencyByCode(String code) {
        return repo.getCurrencyByCode(code);
    }
}
