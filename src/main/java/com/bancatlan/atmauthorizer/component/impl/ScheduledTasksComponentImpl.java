package com.bancatlan.atmauthorizer.component.impl;

import com.bancatlan.atmauthorizer.component.IScheduledTasksComponent;
import com.bancatlan.atmauthorizer.service.ITransactionService;
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

    //@Scheduled(cron = "0 * * * * ?")
    @Scheduled(fixedRate = 300000)
    @Override
    public void runWithdrawConfirmedAsTransferToControlledAccount() {
        LOG.info("In Component: Executing Pending Confirmed Withdrawals at => {} " , dateTimeFormatter.format(LocalDateTime.now()));
        transactionService.executeAllConfirmedWithDrawls();
    }
}