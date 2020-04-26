package com.bancatlan.atmauthorizer.repo;

import com.bancatlan.atmauthorizer.model.Customer;
import com.bancatlan.atmauthorizer.model.PaymentInstrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPaymentInstrumentRepo extends JpaRepository<PaymentInstrument, Long> {
    List<PaymentInstrument> getAllByCustomer(Customer customer);
    PaymentInstrument getByCustomerAndStrIdentifier(Customer customer , String strIdentifier);
    PaymentInstrument getByStrIdentifier(String strIdentifier);
}
