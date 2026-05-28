package com.fleet.document.scheduler;

import com.fleet.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentScheduler {

    private final DocumentService documentService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void checkExpiredDocuments() {
        log.info("Running scheduled task: checkExpiredDocuments");
        documentService.checkExpiredDocuments();
    }
}
