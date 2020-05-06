package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.model.Customer;
import com.bancatlan.atmauthorizer.model.CustomerType;
import com.bancatlan.atmauthorizer.repo.ICustomerRepo;
import com.bancatlan.atmauthorizer.service.ICustomerService;
import com.bancatlan.atmauthorizer.service.ICustomerTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements ICustomerService {

    @Autowired
    ICustomerRepo repo;

    @Autowired
    ICustomerTypeService customerTypeService;

    @Override
    public Customer getByMsisdn(String msisdn) {
        return repo.getByMsisdn(msisdn);
    }

    @Override
    public Customer checkIfCustomerExist(String msisdn) {
        return repo.findByMsisdnContains(msisdn);
    }

    @Override
    public Customer getByUsername(String username) {
        return repo.getByUsername(username);
    }

    @Override
    public Customer register(Customer customer) {
        CustomerType customerType = customerTypeService.getById(Constants.CUSTOMER_TYPE_CONSUMER_ID);
        customer.setCustomerType(customerType);
        return this.create(customer);
    }

    @Override
    public Customer create(Customer obj) {
        obj.setCreationDate(LocalDateTime.now());
        return repo.save(obj);
    }

    @Override
    public Customer update(Customer customer) {
        customer.setUpdateDate(LocalDateTime.now());
        //TODO [CustomerServiceImpl] add update user admin in update Method
        //customer.setIdUserUpdate("admin");
        return repo.save(customer);
    }

    @Override
    public List<Customer> getAll() {
        return null;
    }

    @Override
    public Customer getById(Long id) {
        Optional<Customer> customer = repo.findById(id);
        return customer.isPresent() ? customer.get() : null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public Boolean checkCustomerLimits(Customer customer) {
        //Todo check global Limits
        //Todo check by customer type
        //Todo check by customer
        return true;
    }

    @Override
    public Boolean checkCustomerPrivileges(Customer customer) {
        return true;
    }

    @Override
    public Boolean checkCustomerStatus(Customer customer) {
        return true;
    }
}
