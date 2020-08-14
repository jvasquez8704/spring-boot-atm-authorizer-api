package com.bancatlan.atmauthorizer.api;

import com.bancatlan.atmauthorizer.api.http.CustomStatus;
import com.bancatlan.atmauthorizer.api.http.CustomResponse;
import com.bancatlan.atmauthorizer.component.Constants;
import com.bancatlan.atmauthorizer.dto.VoucherTransactionDTO;
import com.bancatlan.atmauthorizer.exception.AuthorizerError;
import com.bancatlan.atmauthorizer.exception.ModelCustomErrorException;
import com.bancatlan.atmauthorizer.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.TimeUnit;
import java.util.Random;

@RestController
@RequestMapping("/vouchers")
public class VoucherController {

    @Autowired
    IVoucherService service;
    CustomStatus successStatus = new CustomStatus(Constants.INT_BANK_SUCCESS_STATUS_CODE, Constants.BANK_SUCCESS_STATUS_TYPE, Constants.BANK_SUCCESS_STATUS_MESSAGE, Constants.BANK_STRING_ZERO);
    
    @Value("${util.similator.required}")
    String isSimulatorRequired;

    @Value("${util.similator.ramdon.delay}")
    String isSimulatorRamdon;

    @PostMapping("/process")
    private ResponseEntity<CustomResponse> processVoucher(@RequestBody VoucherTransactionDTO dto) {
        successStatus.setCode(Constants.BANK_SUCCESS_STATUS_CODE);
        successStatus.setType(Constants.BANK_SUCCESS_TYPE);
        successStatus.setMessage(Constants.BANK_SUCCESS_STATUS_MESSAGE);
        return new ResponseEntity<>(new CustomResponse(service.voucherProcess(dto), successStatus), HttpStatus.CREATED);
    }

    @PostMapping("/query")
    private ResponseEntity<CustomResponse> getVouchersByParameter(@RequestBody VoucherTransactionDTO dto) {
        if (dto.getTransaction() == null || dto.getTransaction().getPayer() == null || dto.getTransaction().getPayer().getUsername() == null || dto.getTransaction().getPayer().getUsername().equals("")) {
            throw new ModelCustomErrorException("Some parameter required on request is missing", AuthorizerError.MALFORMED_URL);
        }
        this.getSimulator();
        return new ResponseEntity<>(new CustomResponse(service.getAllByOcbUser(dto.getTransaction().getPayer().getUsername()), successStatus), HttpStatus.CREATED);
    }

    @PostMapping("/withdraw")
    private ResponseEntity<CustomResponse> withdrawVoucher(@RequestBody VoucherTransactionDTO dto){
        return new ResponseEntity<>(new CustomResponse(service.withdraw(dto), successStatus),HttpStatus.OK);
    }

    @PostMapping("/cancel")
    private ResponseEntity<CustomResponse> updateVoucher(@RequestBody VoucherTransactionDTO dto){
         return new ResponseEntity<CustomResponse>(new CustomResponse(service.cancelWithdraw(dto), successStatus),HttpStatus.OK);
    }

    @PostMapping("/atm-process")
    private ResponseEntity<CustomResponse> atmProcessVoucher(@RequestBody VoucherTransactionDTO dto){
        successStatus.setCode(Constants.ATM_SUCCESS_STATUS_CODE);
        this.getSimulator();
        return new ResponseEntity<CustomResponse>(new CustomResponse(service.voucherProcess(dto), successStatus),HttpStatus.OK);
    }

    private void getSimulator(){
        if(isSimulatorRequired != null && isSimulatorRequired.equalsIgnoreCase("Y")){
            try {
                Random r = new Random( System.currentTimeMillis() );
                int num =  ((1 + r.nextInt(2)) * 10000 + r.nextInt(10000));
                num = isSimulatorRamdon != null && isSimulatorRamdon.equalsIgnoreCase("Y") ? num : 2;
                if(num % 2 == 0){
                    TimeUnit.SECONDS.sleep(17);
                }
                System.out.println("Delay for 17 SECONDS " + num);
            } catch(Exception e) {
                System.out.println("error in Delay" + e.getMessage());
            }
        }
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
