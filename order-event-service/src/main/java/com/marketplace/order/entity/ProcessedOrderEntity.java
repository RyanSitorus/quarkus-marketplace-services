package com.marketplace.order.entity;

import com.marketplace.order.dto.ProcessedOrderEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "processed_orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "category")
    private String category;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "promo_code")
    private String promoCode;

    @Column(name = "gross_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal grossAmount;

    @Column(name = "discount_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal discountAmount;

    @Column(name = "tax_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal taxAmount;

    @Column(name = "net_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal netAmount;

    @Column(name = "transaction_tier", nullable = false)
    private String transactionTier;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public ProcessedOrderEvent toEvent() {
        return ProcessedOrderEvent.builder()
                .id(this.id)
                .orderId(this.orderId)
                .customerId(this.customerId)
                .customerName(this.customerName)
                .productName(this.productName)
                .category(this.category)
                .quantity(this.quantity)
                .unitPrice(this.unitPrice)
                .promoCode(this.promoCode)
                .grossAmount(this.grossAmount)
                .discountAmount(this.discountAmount)
                .taxAmount(this.taxAmount)
                .netAmount(this.netAmount)
                .transactionTier(this.transactionTier)
                .status(this.status)
                .processedAt(this.processedAt)
                .build();
    }
}
