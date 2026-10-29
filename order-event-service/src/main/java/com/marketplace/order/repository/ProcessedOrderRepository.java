package com.marketplace.order.repository;

import com.marketplace.order.entity.ProcessedOrderEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class ProcessedOrderRepository implements PanacheRepository<ProcessedOrderEntity> {

    public Optional<ProcessedOrderEntity> findByOrderId(String orderId) {
        return find("orderId", orderId).firstResultOptional();
    }
}

