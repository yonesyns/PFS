package com.fleet.commons.event.document;

import com.fleet.commons.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class DocumentUploadedEvent extends BaseEvent<DocumentUploadedEvent.DocumentPayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class DocumentPayload {
        private UUID documentId;
        private String entityType;
        private String entityId;
        private String documentType;
    }
}
