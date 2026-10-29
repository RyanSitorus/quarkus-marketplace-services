package com.marketplace.checkout.service;

import com.marketplace.checkout.model.CheckoutOrder;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;

@ApplicationScoped
public class PricingService {

    private static final Logger LOG = Logger.getLogger(PricingService.class);

    private static final BigDecimal PROMO10_RATE = new BigDecimal("0.10");
    private static final BigDecimal VIP20_RATE = new BigDecimal("0.20");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("500000");
    private static final BigDecimal FLAT_SHIPPING_COST = new BigDecimal("25000.00");

    public CheckoutOrder calculatePricing(CheckoutOrder order) {
        BigDecimal unitPrice = order.getUnitPrice() != null
                ? order.getUnitPrice().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(order.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = resolveDiscount(subtotal, order.getPromoCode());
        BigDecimal shipping = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : FLAT_SHIPPING_COST;

        BigDecimal total = subtotal.subtract(discount).max(BigDecimal.ZERO)
                .add(shipping)
                .setScale(2, RoundingMode.HALF_UP);

        order.setSubtotal(subtotal);
        order.setDiscountAmount(discount);
        order.setShippingCost(shipping);
        order.setTotalAmount(total);
        order.setStatus("PRICED");

        LOG.infof("Pricing calculated for orderId=%s: subtotal=%s, discount=%s, shipping=%s, total=%s",
                order.getOrderId(), subtotal, discount, shipping, total);
        return order;
    }

    private BigDecimal resolveDiscount(BigDecimal subtotal, String promoCode) {
        if (promoCode == null || promoCode.isBlank()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        String normalized = promoCode.trim().toUpperCase();
        if ("VIP20".equals(normalized)) {
            return subtotal.multiply(VIP20_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        if ("PROMO10".equals(normalized)) {
            return subtotal.multiply(PROMO10_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}

