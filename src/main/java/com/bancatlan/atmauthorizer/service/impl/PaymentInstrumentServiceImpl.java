package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.model.Customer;
import com.bancatlan.atmauthorizer.model.PaymentInstrument;
import com.bancatlan.atmauthorizer.repo.IPaymentInstrumentRepo;
import com.bancatlan.atmauthorizer.service.IPaymentInstrumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentInstrumentServiceImpl implements IPaymentInstrumentService {
    @Autowired
    IPaymentInstrumentRepo repo;

    @Override
    public PaymentInstrument create(PaymentInstrument pi) {
        pi.setCreationDate(LocalDateTime.now());
        return repo.save(pi);
    }

    @Override
    public PaymentInstrument update(PaymentInstrument obj) {
        return null;
    }

    @Override
    public List<PaymentInstrument> getAll() {
        return null;
    }

    @Override
    public PaymentInstrument getById(Long id) {
        Optional<PaymentInstrument> pi = repo.findById(id);
        return pi.isPresent() ? pi.get() : null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public List<PaymentInstrument> getPaymentInstrumentsByCustomer(Customer customer) {
        return repo.getAllByCustomer(customer);
    }

    @Override
    public PaymentInstrument getPaymentInstrumentByCustomerAndStrIdentifier(Customer customer, String strIdentifier) {
        return repo.getByCustomerAndStrIdentifier(customer, strIdentifier);
    }

    @Override
    public PaymentInstrument getPaymentInstrumentByStrIdentifier(String strIdentifier) {
        return repo.getByStrIdentifier(strIdentifier);
    }
}
