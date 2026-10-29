package com.marketplace.checkout.service;

import com.marketplace.checkout.model.CheckoutOrder;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class FulfillmentService {

    private static final Logger LOG = Logger.getLogger(FulfillmentService.class);

    public CheckoutOrder confirmOrderAndShip(CheckoutOrder order) {
        order.setShippingTrackingNumber("SHP-" + order.getOrderId().toUpperCase());
        order.setStatus("COMPLETED");
        LOG.infof("Order completed and shipment scheduled: orderId=%s, tracking=%s",
                order.getOrderId(), order.getShippingTrackingNumber());
        return order;
    }

    public CheckoutOrder notifyRejection(CheckoutOrder order) {
        LOG.infof("Sending rejection notification for orderId=%s, status=%s, reason=%s",
                order.getOrderId(), order.getStatus(), order.getRejectionReason());
        return order;
    }
}

