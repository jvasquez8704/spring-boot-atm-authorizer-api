package com.bancatlan.atmauthorizer.api;

import com.bancatlan.atmauthorizer.api.http.CustomStatus;
import com.bancatlan.atmauthorizer.api.http.CustomResponse;
import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.dto.OcbVoucherDTO;
import com.bancatlan.atmauthorizer.dto.VoucherTransactionDTO;
import com.bancatlan.atmauthorizer.exception.AuthorizerError;
import com.bancatlan.atmauthorizer.exception.ModelCustomErrorException;
import com.bancatlan.atmauthorizer.service.ICardlessWithdrawal;
import com.bancatlan.atmauthorizer.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vouchers")
public class VoucherController {
    CustomStatus bankingSuccessStatus = new CustomStatus(Constants.BANK_SUCCESS_STATUS_CODE, Constants.BANK_SUCCESS_STATUS_TYPE, Constants.BANK_SUCCESS_STATUS_MESSAGE, Constants.BANK_STRING_ZERO);
    CustomStatus atmSuccessStatus = new CustomStatus(Constants.ATM_SUCCESS_STATUS_CODE, Constants.BANK_SUCCESS_STATUS_TYPE, Constants.BANK_SUCCESS_STATUS_MESSAGE, Constants.BANK_STRING_ZERO);

    @Autowired
    IVoucherService service;

    @Autowired
    ICardlessWithdrawal cardlessWithdrawalService;

    @PostMapping("/process")
    private ResponseEntity<CustomResponse> processVoucher(@RequestBody VoucherTransactionDTO dto) {
        return new ResponseEntity<>(new CustomResponse(service.voucherProcess(dto), bankingSuccessStatus), HttpStatus.CREATED);
    }

    @PostMapping("/query")
    private ResponseEntity<CustomResponse> getVouchersByParameter(@RequestBody VoucherTransactionDTO dto) {
        if (dto.getTransaction() == null || dto.getTransaction().getPayer() == null || dto.getTransaction().getPayer().getUsername() == null || dto.getTransaction().getPayer().getUsername().equals("")) {
            throw new ModelCustomErrorException("Some parameter required on request is missing", AuthorizerError.MALFORMED_URL);
        }
        return new ResponseEntity<>(new CustomResponse(service.getAllByOcbUser(dto.getTransaction().getPayer().getUsername()), bankingSuccessStatus), HttpStatus.CREATED);
    }

    @PostMapping("/withdraw")
    private ResponseEntity<CustomResponse> withdrawVoucher(@RequestBody VoucherTransactionDTO dto){
        return new ResponseEntity<>(new CustomResponse(service.withdraw(dto), bankingSuccessStatus),HttpStatus.OK);
    }

    @PostMapping("/cancel")
    private ResponseEntity<CustomResponse> updateVoucher(@RequestBody VoucherTransactionDTO dto){
         return new ResponseEntity<CustomResponse>(new CustomResponse(service.cancelWithdraw(dto), bankingSuccessStatus),HttpStatus.OK);
    }

    @PostMapping("/atm-process")
    private ResponseEntity<CustomResponse> atmProcessVoucher(@RequestBody VoucherTransactionDTO dto){
        return new ResponseEntity<CustomResponse>(new CustomResponse(service.voucherProcess(dto), atmSuccessStatus),HttpStatus.OK);
    }

    @PostMapping("/verify")
    private ResponseEntity<CustomResponse> verifyVoucher(@RequestBody OcbVoucherDTO dto){
        return new ResponseEntity<>(new CustomResponse(cardlessWithdrawalService.verify(dto), bankingSuccessStatus),HttpStatus.OK);
    }

    @PostMapping("/confirm")
    private ResponseEntity<CustomResponse> confirmVoucher(@RequestBody OcbVoucherDTO dto){
        return new ResponseEntity<>(new CustomResponse(cardlessWithdrawalService.confirm(dto), bankingSuccessStatus),HttpStatus.OK);
    }

      /*
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
    }*/

}
