package com.bancatlan.atmauthorizer.service;

import com.bancatlan.atmauthorizer.dto.VoucherTransactionDTO;
import com.bancatlan.atmauthorizer.model.Transaction;
import com.bancatlan.atmauthorizer.model.Voucher;

import java.util.List;

public interface IVoucherService extends ICRUD<Voucher> {
    Voucher process(VoucherTransactionDTO dto);
    VoucherTransactionDTO verify(VoucherTransactionDTO dto);
    Voucher confirm(VoucherTransactionDTO dto);
    Voucher withdraw(VoucherTransactionDTO dto);
    Voucher cancelWithdraw(VoucherTransactionDTO dto);
    List<Voucher> getAllByOcbUser(String ocbUser);
}
