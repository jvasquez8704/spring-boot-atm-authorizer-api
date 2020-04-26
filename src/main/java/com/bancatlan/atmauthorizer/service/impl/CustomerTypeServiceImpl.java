package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.model.CustomerType;
import com.bancatlan.atmauthorizer.repo.ICustomerTypeRepo;
import com.bancatlan.atmauthorizer.service.ICustomerTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerTypeServiceImpl implements ICustomerTypeService {
    @Autowired
    ICustomerTypeRepo repo;

    @Override
    public CustomerType create(CustomerType obj) {
        return null;
    }

    @Override
    public CustomerType update(CustomerType obj) {
        return null;
    }

    @Override
    public List<CustomerType> getAll() {
        return null;
    }

    @Override
    public CustomerType getById(Long id) {
        Optional<CustomerType> cstType = repo.findById(id);
        return cstType.isPresent() ? cstType.get() : null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }
}
