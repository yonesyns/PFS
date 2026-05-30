package com.fleet.commons.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent<T> {
    private UUID eventId = UUID.randomUUID();
    private String eventType;
    private Instant timestamp = Instant.now();
    private T payload;
    private String source;
}
