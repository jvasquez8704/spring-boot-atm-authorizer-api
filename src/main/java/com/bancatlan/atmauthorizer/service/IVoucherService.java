package com.bancatlan.atmauthorizer.service;

import com.bancatlan.atmauthorizer.dto.VoucherTransactionDTO;
import com.bancatlan.atmauthorizer.model.Transaction;
import com.bancatlan.atmauthorizer.model.Voucher;

import java.util.List;

public interface IVoucherService extends ICRUD<Voucher> {
    VoucherTransactionDTO voucherProcess(VoucherTransactionDTO dto);
    VoucherTransactionDTO bankVerifyPayment(VoucherTransactionDTO dto);
    VoucherTransactionDTO bankConfirmPayment(VoucherTransactionDTO dto);
    Voucher withdraw(VoucherTransactionDTO dto);
    Voucher cancelWithdraw(VoucherTransactionDTO dto);
    List<Voucher> getAllByOcbUser(String ocbUser);
}
