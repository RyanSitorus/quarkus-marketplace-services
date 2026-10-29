package com.marketplace.order;

import com.marketplace.order.dto.ProcessedOrderEvent;
import com.marketplace.order.dto.RawOrderEvent;
import com.marketplace.order.entity.ProcessedOrderEntity;
import com.marketplace.order.repository.ProcessedOrderRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class OrderProcessingTest {

    @Inject
    @Any
    InMemoryConnector connector;

    @Inject
    ProcessedOrderRepository orderRepository;

    @Test
    void testConsumeTransformAndPersist() {
        InMemorySource<RawOrderEvent> rawOrdersIn = connector.source("raw-orders-in");
        InMemorySink<ProcessedOrderEvent> processedOrdersOut = connector.sink("processed-orders-out");

        int initialOutCount = processedOrdersOut.received().size();

        RawOrderEvent rawEvent = new RawOrderEvent(
                "ord-test-001",
                "cust-001",
                "Rina Kartika",
                "Life Protection Plan",
                "insurance",
                2,
                new BigDecimal("500000"),
                "promo10"
        );

        rawOrdersIn.send(rawEvent);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> processedOrdersOut.received().size() > initialOutCount);

        ProcessedOrderEvent outputEvent = processedOrdersOut.received()
                .get(processedOrdersOut.received().size() - 1)
                .getPayload();

        assertNotNull(outputEvent.getId());
        assertEquals("ORD-TEST-001", outputEvent.getOrderId());
        assertEquals("CUST-001", outputEvent.getCustomerId());
        assertEquals("INSURANCE", outputEvent.getCategory());
        assertEquals("PROMO10", outputEvent.getPromoCode());
        assertEquals(new BigDecimal("1000000.00"), outputEvent.getGrossAmount());
        assertEquals(new BigDecimal("100000.00"), outputEvent.getDiscountAmount());
        assertEquals(new BigDecimal("99000.00"), outputEvent.getTaxAmount());
        assertEquals(new BigDecimal("999000.00"), outputEvent.getNetAmount());
        assertEquals("GOLD", outputEvent.getTransactionTier());
        assertEquals("PROCESSED", outputEvent.getStatus());

        Optional<ProcessedOrderEntity> dbRecord = orderRepository.findByOrderId("ORD-TEST-001");
        assertTrue(dbRecord.isPresent());
        assertEquals(new BigDecimal("999000.00"), dbRecord.get().getNetAmount());

        given()
                .when()
                .get("/api/orders/ORD-TEST-001")
                .then()
                .statusCode(200)
                .body("orderId", equalTo("ORD-TEST-001"))
                .body("netAmount", equalTo(999000.0f))
                .body("transactionTier", equalTo("GOLD"));
    }

    @Test
    void testPublishOrderEndpoint() {
        InMemorySink<RawOrderEvent> rawOrdersOut = connector.sink("raw-orders-out");
        int initialCount = rawOrdersOut.received().size();

        RawOrderEvent request = new RawOrderEvent(
                "ORD-API-002",
                "CUST-202",
                "Dedi Kurniawan",
                "Commercial Property Coverage",
                "PROPERTY",
                1,
                new BigDecimal("2000000"),
                "VIP20"
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(202)
                .body("status", equalTo("PUBLISHED"))
                .body("orderId", equalTo("ORD-API-002"));

        assertEquals(initialCount + 1, rawOrdersOut.received().size());
        assertEquals("ORD-API-002", rawOrdersOut.received().get(initialCount).getPayload().getOrderId());
    }
}
