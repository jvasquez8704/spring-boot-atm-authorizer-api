package com.bancatlan.atmauthorizer.service;

import com.bancatlan.atmauthorizer.dto.OcbVoucherDTO;

public interface ICardlessWithdrawal {
    OcbVoucherDTO verify(OcbVoucherDTO dto);
    OcbVoucherDTO confirm(OcbVoucherDTO dto);
}
