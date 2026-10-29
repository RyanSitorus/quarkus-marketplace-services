package com.marketplace.order.serialization;

import com.marketplace.order.dto.RawOrderEvent;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class RawOrderDeserializer extends ObjectMapperDeserializer<RawOrderEvent> {
    public RawOrderDeserializer() {
        super(RawOrderEvent.class);
    }
}

