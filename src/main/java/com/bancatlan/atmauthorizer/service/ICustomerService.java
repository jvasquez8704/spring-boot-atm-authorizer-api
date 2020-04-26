package com.bancatlan.atmauthorizer.service;

import com.bancatlan.atmauthorizer.model.Customer;

public interface ICustomerService extends ICRUD<Customer> {
    Customer getByMsisdn(String msisdn);
    Customer checkIfCustomerExist(String msisdn);
    Customer getByOcbUser(String ocbUser);
    Customer register(Customer customer);
    Boolean checkCustomerLimits(Customer customer);
    Boolean checkCustomerPrivileges(Customer customer);
    Boolean checkCustomerStatus(Customer customer);
}
