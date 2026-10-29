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
public class RawOrderEvent {

    private String orderId;
    private String customerId;
    private String customerName;
    private String productName;
    private String category;
    private int quantity;
    private BigDecimal unitPrice;
    private String promoCode;
    private Instant eventTimestamp;

    public RawOrderEvent(String orderId, String customerId, String customerName,
                         String productName, String category, int quantity,
                         BigDecimal unitPrice, String promoCode) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.promoCode = promoCode;
        this.eventTimestamp = Instant.now();
    }
}
