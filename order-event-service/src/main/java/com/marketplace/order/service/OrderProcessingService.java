package com.marketplace.order.service;

import com.marketplace.order.dto.ProcessedOrderEvent;
import com.marketplace.order.dto.RawOrderEvent;
import com.marketplace.order.entity.ProcessedOrderEntity;
import com.marketplace.order.repository.ProcessedOrderRepository;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class OrderProcessingService {

    private static final Logger LOG = Logger.getLogger(OrderProcessingService.class);

    private static final BigDecimal TAX_RATE = new BigDecimal("0.11");
    private static final BigDecimal PROMO10_RATE = new BigDecimal("0.10");
    private static final BigDecimal VIP20_RATE = new BigDecimal("0.20");
    private static final BigDecimal BULK_DISCOUNT_RATE = new BigDecimal("0.05");
    private static final BigDecimal BULK_THRESHOLD = new BigDecimal("1000000");
    private static final BigDecimal PLATINUM_THRESHOLD = new BigDecimal("1000000");
    private static final BigDecimal GOLD_THRESHOLD = new BigDecimal("500000");

    @Inject
    ProcessedOrderRepository orderRepository;

    @Incoming("raw-orders-in")
    @Outgoing("processed-orders-out")
    @Blocking
    @Transactional
    public ProcessedOrderEvent processOrder(RawOrderEvent rawEvent) {
        LOG.infof("Processing raw order: %s", rawEvent.getOrderId());

        String orderId = normalizeUpper(rawEvent.getOrderId(),
                "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        String customerId = normalizeUpper(rawEvent.getCustomerId(), "CUST-GUEST");
        String customerName = normalizeText(rawEvent.getCustomerName(), "Guest");
        String productName = normalizeText(rawEvent.getProductName(), "Unknown Product");
        String category = normalizeUpper(rawEvent.getCategory(), "GENERAL");
        String promoCode = normalizeUpper(rawEvent.getPromoCode(), "NONE");

        int quantity = Math.max(1, rawEvent.getQuantity());
        BigDecimal unitPrice = (rawEvent.getUnitPrice() == null || rawEvent.getUnitPrice().compareTo(BigDecimal.ZERO) < 0)
                ? BigDecimal.ZERO
                : rawEvent.getUnitPrice().setScale(2, RoundingMode.HALF_UP);

        BigDecimal grossAmount = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = calculateDiscount(grossAmount, promoCode);
        BigDecimal taxableAmount = grossAmount.subtract(discountAmount).max(BigDecimal.ZERO);
        BigDecimal taxAmount = taxableAmount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAmount = taxableAmount.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
        String tier = determineTier(netAmount);

        ProcessedOrderEntity entity = ProcessedOrderEntity.builder()
                .orderId(orderId)
                .customerId(customerId)
                .customerName(customerName)
                .productName(productName)
                .category(category)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .promoCode(promoCode)
                .grossAmount(grossAmount)
                .discountAmount(discountAmount)
                .taxAmount(taxAmount)
                .netAmount(netAmount)
                .transactionTier(tier)
                .status("PROCESSED")
                .processedAt(Instant.now())
                .build();

        orderRepository.persist(entity);
        LOG.infof("Order saved: orderId=%s, netAmount=%s, tier=%s", entity.getOrderId(), entity.getNetAmount(), entity.getTransactionTier());

        return entity.toEvent();
    }

    private String normalizeText(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }

    private String normalizeUpper(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.trim().toUpperCase();
    }

    private BigDecimal calculateDiscount(BigDecimal grossAmount, String promoCode) {
        if ("VIP20".equals(promoCode)) {
            return grossAmount.multiply(VIP20_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        if ("PROMO10".equals(promoCode)) {
            return grossAmount.multiply(PROMO10_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        if (grossAmount.compareTo(BULK_THRESHOLD) >= 0) {
            return grossAmount.multiply(BULK_DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private String determineTier(BigDecimal netAmount) {
        if (netAmount.compareTo(PLATINUM_THRESHOLD) >= 0) {
            return "PLATINUM";
        }
        if (netAmount.compareTo(GOLD_THRESHOLD) >= 0) {
            return "GOLD";
        }
        return "REGULAR";
    }
}
