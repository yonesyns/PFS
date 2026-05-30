package com.fleet.commons.event.document;

import com.fleet.commons.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class DocumentExpiredEvent extends BaseEvent<DocumentExpiredEvent.DocumentExpiredPayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class DocumentExpiredPayload {
        private UUID documentId;
        private String entityType;
        private String entityId;
        private String documentType;
        private LocalDate expiryDate;
    }
}
