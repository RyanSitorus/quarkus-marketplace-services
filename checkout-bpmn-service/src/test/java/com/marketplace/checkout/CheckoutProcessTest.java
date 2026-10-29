package com.marketplace.checkout;

import com.marketplace.checkout.model.CheckoutOrder;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
public class CheckoutProcessTest {

    @Test
    void testCheckoutSuccessWithPromoAndFreeShipping() {
        CheckoutOrder order = new CheckoutOrder(
                "CHK-001",
                "CUST-101",
                "Budi Santoso",
                "PROD-INS-01",
                "Comprehensive Vehicle Insurance",
                2,
                new BigDecimal("500000"),
                "PROMO10",
                "VIRTUAL_ACCOUNT"
        );

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("order", order))
                .when()
                .post("/checkout")
                .then()
                .statusCode(201)
                .body("order.orderId", equalTo("CHK-001"))
                .body("order.stockAvailable", equalTo(true))
                .body("order.subtotal", equalTo(1000000.0f))
                .body("order.discountAmount", equalTo(100000.0f))
                .body("order.shippingCost", equalTo(0.0f))
                .body("order.totalAmount", equalTo(900000.0f))
                .body("order.paymentSuccess", equalTo(true))
                .body("order.paymentReference", equalTo("PAY-CHK-001"))
                .body("order.shippingTrackingNumber", equalTo("SHP-CHK-001"))
                .body("order.status", equalTo("COMPLETED"))
                .body("order.rejectionReason", nullValue());
    }

    @Test
    void testCheckoutRejectedWhenOutOfStock() {
        CheckoutOrder order = new CheckoutOrder(
                "CHK-002",
                "CUST-102",
                "Siti Rahma",
                "PROD-OOS",
                "Limited Gadget Protection",
                1,
                new BigDecimal("250000"),
                "NONE",
                "CREDIT_CARD"
        );

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("order", order))
                .when()
                .post("/checkout")
                .then()
                .statusCode(201)
                .body("order.orderId", equalTo("CHK-002"))
                .body("order.stockAvailable", equalTo(false))
                .body("order.paymentSuccess", equalTo(false))
                .body("order.status", equalTo("FAILED_OUT_OF_STOCK"))
                .body("order.rejectionReason", notNullValue())
                .body("order.shippingTrackingNumber", nullValue());
    }

    @Test
    void testCheckoutPaymentFailedReleasesReservedStock() {
        CheckoutOrder order = new CheckoutOrder(
                "CHK-003",
                "CUST-103",
                "Andi Wijaya",
                "PROD-HOME-02",
                "Home Fire Protection",
                1,
                new BigDecimal("300000"),
                "NONE",
                "DECLINED"
        );

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("order", order))
                .when()
                .post("/checkout")
                .then()
                .statusCode(201)
                .body("order.orderId", equalTo("CHK-003"))
                .body("order.subtotal", equalTo(300000.0f))
                .body("order.shippingCost", equalTo(25000.0f))
                .body("order.totalAmount", equalTo(325000.0f))
                .body("order.paymentSuccess", equalTo(false))
                .body("order.stockAvailable", equalTo(false))
                .body("order.status", equalTo("FAILED_PAYMENT"))
                .body("order.rejectionReason", notNullValue())
                .body("order.shippingTrackingNumber", nullValue());
    }
}

