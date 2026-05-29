package com.fleet.payment.scheduler;

import com.fleet.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentService paymentService;

    @Scheduled(cron = "0 0 6 1 * ?")
    public void generateMonthlyInvoices() {
        log.info("Running scheduled task: generateMonthlyInvoices");
        paymentService.generateMonthlyInvoices();
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void checkOverdueInvoices() {
        log.info("Running scheduled task: checkOverdueInvoices");
        paymentService.checkOverdueInvoices();
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void checkExpiringSubscriptions() {
        log.info("Running scheduled task: checkExpiringSubscriptions");
        paymentService.checkExpiringSubscriptions();
    }
}
