package com.fleet.notification.scheduler;

import com.fleet.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 300000) // Toutes les 5 minutes
    public void retryFailedNotifications() {
        log.info("Running scheduled task: retryFailedNotifications");
        notificationService.retryFailedNotifications();
    }
}
