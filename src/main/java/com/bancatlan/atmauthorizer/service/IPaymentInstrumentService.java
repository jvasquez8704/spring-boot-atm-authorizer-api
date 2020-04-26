package com.bancatlan.atmauthorizer.service;
import com.bancatlan.atmauthorizer.model.Customer;
import com.bancatlan.atmauthorizer.model.PaymentInstrument;

import java.util.List;

public interface IPaymentInstrumentService extends ICRUD<PaymentInstrument> {
    List<PaymentInstrument> getPaymentInstrumentsByCustomer(Customer customer);
    PaymentInstrument getPaymentInstrumentByCustomerAndStrIdentifier(Customer customer, String strIdentifier);
    PaymentInstrument getPaymentInstrumentByStrIdentifier(String strIdentifier);
}
