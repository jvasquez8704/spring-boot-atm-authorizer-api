package com.bancatlan.atmauthorizer.component.impl;

import com.bancatlan.atmauthorizer.component.IScheduledTasksComponent;
import com.bancatlan.atmauthorizer.service.ITransactionService;
import com.bancatlan.atmauthorizer.service.IVoucherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ScheduledTasksComponentImpl implements IScheduledTasksComponent {
    Logger LOG = LoggerFactory.getLogger(ScheduledTasksComponentImpl.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Autowired
    ITransactionService transactionService;

    @Autowired
    IVoucherService voucherService;

    //@Scheduled(cron = "0 * * * * ?")
    @Scheduled(fixedRate = 900000)//15mins
    @Override
    public void runWithdrawConfirmedAsTransferToControlledAccount() {
        LOG.info("WithdrawConfirmed: Name thread => {}" , Thread.currentThread().getName());
        transactionService.executeAllConfirmedWithDrawls();
    }

    //@Scheduled(fixedRate = 3600000)//1hour
    @Scheduled(fixedRate = 600000)//10mins
    @Override
    public void runExpiredVouchers() {
        LOG.info("RunExpiredVouchers: Name thread => {}" , Thread.currentThread().getName());
        voucherService.reverseExpiredVouchers();
    }

}
