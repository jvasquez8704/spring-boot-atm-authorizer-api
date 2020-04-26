package com.bancatlan.atmauthorizer.api;

import com.bancatlan.atmauthorizer.model.Transaction;
import com.bancatlan.atmauthorizer.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private ITransactionService service;

    @GetMapping
    public ResponseEntity<List<Transaction>> getAll(){
        List<Transaction> txnList = service.getAll();
        return  new ResponseEntity<List<Transaction>>(txnList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTxnById(@PathVariable("id") Long id){
        return  new ResponseEntity<Transaction>(service.getById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Transaction> createTxn(@RequestBody Transaction txn){
        return  new ResponseEntity<Transaction>(service.create(txn),HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Transaction> updateTxn(@RequestBody Transaction txn){
        return new ResponseEntity<Transaction>(service.update(txn), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteById(@PathVariable("id") Long id){
        return new ResponseEntity<Object>(service.delete(id) ? HttpStatus.OK: HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
