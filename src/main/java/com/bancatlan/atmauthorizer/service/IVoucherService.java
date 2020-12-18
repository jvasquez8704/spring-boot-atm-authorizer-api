package com.bancatlan.atmauthorizer.service;

import com.bancatlan.atmauthorizer.dto.OcbVoucherDTO;
import com.bancatlan.atmauthorizer.dto.VoucherTransactionDTO;
import com.bancatlan.atmauthorizer.model.Customer;
import com.bancatlan.atmauthorizer.model.Transaction;
import com.bancatlan.atmauthorizer.model.Voucher;

import java.util.List;

public interface IVoucherService extends ICRUD<Voucher> {
    VoucherTransactionDTO voucherProcess(VoucherTransactionDTO dto);
    VoucherTransactionDTO bankVerifyPayment(VoucherTransactionDTO dto);
    VoucherTransactionDTO bankConfirmPayment(VoucherTransactionDTO dto);
    OcbVoucherDTO verify(OcbVoucherDTO dto);
    OcbVoucherDTO confirm(OcbVoucherDTO dto);
    Voucher withdraw(VoucherTransactionDTO dto);
    Voucher findVoucherToWithdraw(String pickupCode, String secretCode, Customer customer);
    Voucher findVoucherToReverse(String pickupCode, String secretCode, Customer customer);
    Voucher cancelWithdraw(VoucherTransactionDTO dto);
    Voucher reverseInProcess(Voucher voucher);
    Voucher cancel(Voucher voucher);
    Voucher cancelByTxn(Transaction txn);
    List<Voucher> getAllByOcbUser(String ocbUser);
    Voucher getVoucherByCreatorTransaction(Transaction transaction);
    void reverseExpiredVouchers();
}
