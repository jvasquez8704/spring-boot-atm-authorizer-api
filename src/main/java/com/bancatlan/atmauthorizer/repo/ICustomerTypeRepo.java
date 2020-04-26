package com.bancatlan.atmauthorizer.repo;

import com.bancatlan.atmauthorizer.model.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICustomerTypeRepo extends JpaRepository<CustomerType, Long> {
}
