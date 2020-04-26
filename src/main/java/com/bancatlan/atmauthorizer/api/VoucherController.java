package com.bancatlan.atmauthorizer.api;

import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.dto.VoucherTransactionDTO;
import com.bancatlan.atmauthorizer.exception.AuthorizerError;
import com.bancatlan.atmauthorizer.exception.ModelNotFoundException;
import com.bancatlan.atmauthorizer.model.Voucher;
import com.bancatlan.atmauthorizer.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vouchers")
public class VoucherController {

    @Autowired
    IVoucherService service;

    @GetMapping
    private ResponseEntity<List<Voucher>> getAll(){
        List<Voucher> list = service.getAll();
        return new ResponseEntity<List<Voucher>>(list, HttpStatus.OK);
    }

    @GetMapping("/ocb/{user}")
    private ResponseEntity<List<Voucher>> getVoucherByOcbUser(@PathVariable("user") String user){
        List<Voucher> list = service.getAllByOcbUser(user);
        if(list.isEmpty()){
            throw new ModelNotFoundException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.VOUCHER_NOT_FOUND);
        }
        return new ResponseEntity<List<Voucher>>(list, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    private ResponseEntity<Voucher> getVoucherById(@PathVariable("id") Long id){
        Voucher voucher = service.getById(id);
        if(voucher == null){
            throw new ModelNotFoundException(Constants.MODEL_NOT_FOUND_MESSAGE_ERROR, AuthorizerError.VOUCHER_NOT_FOUND);
        }
        return new ResponseEntity<Voucher>(voucher, HttpStatus.OK);
    }

    @PostMapping
    private ResponseEntity<Voucher> processVoucher(@RequestBody VoucherTransactionDTO dto){
        return new ResponseEntity<Voucher>(service.process(dto),HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    private ResponseEntity<VoucherTransactionDTO> verifyVoucher(@RequestBody VoucherTransactionDTO dto){
        return new ResponseEntity<VoucherTransactionDTO>(service.verify(dto),HttpStatus.CREATED);
    }

    @PostMapping("/confirm")
    private ResponseEntity<Voucher> confirmVoucher(@RequestBody VoucherTransactionDTO dto){
        return new ResponseEntity<Voucher>(service.confirm(dto),HttpStatus.CREATED);
    }

    @PutMapping("/withdraw")
    private ResponseEntity<Voucher> withdrawVoucher(@RequestBody VoucherTransactionDTO dto){
        return new ResponseEntity<Voucher>(service.withdraw(dto),HttpStatus.OK);
    }

    @PutMapping("/cancel")
    private ResponseEntity<Voucher> updateVoucher(@RequestBody VoucherTransactionDTO dto){
         return new ResponseEntity<Voucher>(service.cancelWithdraw(dto),HttpStatus.OK);
    }

}
