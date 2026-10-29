package com.marketplace.checkout.service;

import com.marketplace.checkout.model.CheckoutOrder;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.math.BigDecimal;

@ApplicationScoped
public class PaymentService {

    private static final Logger LOG = Logger.getLogger(PaymentService.class);

    public CheckoutOrder processPayment(CheckoutOrder order) {
        LOG.infof("Processing payment for orderId=%s, method=%s, total=%s",
                order.getOrderId(), order.getPaymentMethod(), order.getTotalAmount());

        String method = order.getPaymentMethod() == null ? "" : order.getPaymentMethod().trim().toUpperCase();
        boolean validAmount = order.getTotalAmount() != null && order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0;
        boolean declinedMethod = method.isEmpty() || "DECLINED".equals(method) || "INVALID_CARD".equals(method);

        if (!validAmount || declinedMethod) {
            order.setPaymentSuccess(false);
            order.setRejectionReason("Payment declined for method: " + order.getPaymentMethod());
            LOG.warnf("Payment failed for orderId=%s", order.getOrderId());
            return order;
        }

        order.setPaymentSuccess(true);
        order.setPaymentReference("PAY-" + order.getOrderId().toUpperCase());
        order.setStatus("PAID");
        LOG.infof("Payment succeeded for orderId=%s, ref=%s", order.getOrderId(), order.getPaymentReference());
        return order;
    }
}
