package com.marketplace.order.service;

import com.marketplace.order.dto.RawOrderEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.time.Instant;

@ApplicationScoped
public class OrderProducerService {

    private static final Logger LOG = Logger.getLogger(OrderProducerService.class);

    @Inject
    @Channel("raw-orders-out")
    Emitter<RawOrderEvent> rawOrderEmitter;

    public void publishRawOrder(RawOrderEvent event) {
        if (event.getEventTimestamp() == null) {
            event.setEventTimestamp(Instant.now());
        }

        LOG.infof("Publishing order event: orderId=%s", event.getOrderId());
        rawOrderEmitter.send(event);
    }
}
