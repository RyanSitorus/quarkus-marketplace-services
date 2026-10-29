package com.marketplace.order.resource;

import com.marketplace.order.dto.RawOrderEvent;
import com.marketplace.order.entity.ProcessedOrderEntity;
import com.marketplace.order.repository.ProcessedOrderRepository;
import com.marketplace.order.service.OrderProducerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    OrderProducerService producerService;

    @Inject
    ProcessedOrderRepository orderRepository;

    @POST
    public Response publishOrder(RawOrderEvent request) {
        producerService.publishRawOrder(request);
        return Response.status(Response.Status.ACCEPTED)
                .entity(Map.of("status", "PUBLISHED", "orderId", request.getOrderId()))
                .build();
    }

    @GET
    public List<ProcessedOrderEntity> getAllOrders() {
        return orderRepository.listAll();
    }

    @GET
    @Path("/{orderId}")
    public ProcessedOrderEntity getByOrderId(@PathParam("orderId") String orderId) {
        return orderRepository.findByOrderId(orderId.trim().toUpperCase())
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }
}
