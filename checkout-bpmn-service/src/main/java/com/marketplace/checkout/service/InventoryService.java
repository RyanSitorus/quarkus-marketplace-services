package com.marketplace.checkout.service;

import com.marketplace.checkout.model.CheckoutOrder;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InventoryService {

    private static final Logger LOG = Logger.getLogger(InventoryService.class);
    private static final int MAX_AVAILABLE_STOCK = 50;

    public CheckoutOrder reserveStock(CheckoutOrder order) {
        LOG.infof("Checking inventory for orderId=%s, productId=%s, qty=%d",
                order.getOrderId(), order.getProductId(), order.getQuantity());

        boolean invalidProduct = order.getProductId() == null
                || "PROD-OOS".equalsIgnoreCase(order.getProductId().trim());
        boolean invalidQuantity = order.getQuantity() <= 0
                || order.getQuantity() > MAX_AVAILABLE_STOCK;

        if (invalidProduct || invalidQuantity) {
            order.setStockAvailable(false);
            order.setStatus("FAILED_OUT_OF_STOCK");
            order.setRejectionReason("Insufficient stock for product " + order.getProductId());
            LOG.warnf("Stock reservation failed for orderId=%s", order.getOrderId());
            return order;
        }

        order.setStockAvailable(true);
        order.setStatus("STOCK_RESERVED");
        LOG.infof("Stock reserved for orderId=%s", order.getOrderId());
        return order;
    }

    public CheckoutOrder releaseStock(CheckoutOrder order) {
        LOG.infof("Releasing reserved stock for orderId=%s, productId=%s",
                order.getOrderId(), order.getProductId());
        order.setStockAvailable(false);
        order.setStatus("FAILED_PAYMENT");
        return order;
    }
}

