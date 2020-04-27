package com.bancatlan.atmauthorizer.api;

import com.bancatlan.atmauthorizer.model.PaymentInstrument;
import com.bancatlan.atmauthorizer.service.IPaymentInstrumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pi")
public class PaymentInstrumentController {
    @Autowired
    IPaymentInstrumentService service;

    @PostMapping
    public ResponseEntity<PaymentInstrument> createCustomer(@RequestBody PaymentInstrument pi){
        return new ResponseEntity<PaymentInstrument>(service.create(pi), HttpStatus.OK);
    }
}
