package com.bancatlan.atmauthorizer.repo;

import com.bancatlan.atmauthorizer.api.http.CustomResponse;
import com.bancatlan.atmauthorizer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICustomerRepo extends JpaRepository<Customer, Long> {
    Customer getByMsisdn(String msisdn);
    Customer findByMsisdnContains(String msisdn);
    Customer getByUsername(String username);
}

