package com.marketplace.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedOrderEvent {

    private Long id;
    private String orderId;
    private String customerId;
    private String customerName;
    private String productName;
    private String category;
    private int quantity;
    private BigDecimal unitPrice;
    private String promoCode;
    private BigDecimal grossAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private String transactionTier;
    private String status;
    private Instant processedAt;
}
