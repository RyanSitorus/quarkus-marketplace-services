package com.marketplace.order.service;

import com.marketplace.order.dto.RawOrderEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class OrderProducerService {

    private static final Logger LOG = Logger.getLogger(OrderProducerService.class);

    @Inject
    @Channel("raw-orders-out")
    Emitter<RawOrderEvent> rawOrderEmitter;

    public void publishRawOrder(RawOrderEvent event) {
        if (event.getOrderId() == null || event.getOrderId().isBlank()) {
            event.setOrderId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (event.getEventTimestamp() == null) {
            event.setEventTimestamp(Instant.now());
        }

        LOG.infof("Publishing order event: orderId=%s", event.getOrderId());
        rawOrderEmitter.send(event);
    }
}
