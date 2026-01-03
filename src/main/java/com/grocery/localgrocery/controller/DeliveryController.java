package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.Order;
import com.grocery.localgrocery.entity.User;
import com.grocery.localgrocery.repository.OrderRepository;
import com.grocery.localgrocery.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public DeliveryController(OrderRepository orderRepository,
                              UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    // Test endpoint
    @GetMapping("/test")
    public String test() {
        return "Delivery Controller is working!";
    }

    // Get orders assigned to a specific delivery person
    @GetMapping("/my-orders")
    public List<Order> getMyOrders(@RequestParam Long deliveryPersonId) {
        User deliveryPerson = userRepository.findById(deliveryPersonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery person not found"));

        if (!"DELIVERY".equals(deliveryPerson.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a delivery person");
        }

        return orderRepository.findByDeliveryPersonId(deliveryPersonId);
    }

    // Admin assigns order to delivery person
    @PostMapping("/assign")
    public Order assignOrder(@RequestBody AssignRequest req) {
        Order order = orderRepository.findById(req.orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        User deliveryPerson = userRepository.findById(req.deliveryPersonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery person not found"));

        if (!"DELIVERY".equals(deliveryPerson.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a delivery person");
        }

        order.setDeliveryPerson(deliveryPerson);
        order.setStatus("ASSIGNED");
        order.setAssignedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    // Delivery person updates order status
    @PostMapping("/update-status")
    public Order updateStatus(@RequestBody UpdateStatusRequest req) {
        Order order = orderRepository.findById(req.orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        // Verify this delivery person is assigned to this order
        if (order.getDeliveryPerson() == null ||
                !order.getDeliveryPerson().getId().equals(req.deliveryPersonId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not assigned to this order");
        }

        order.setStatus(req.status);

        // Set timeline timestamps
        if ("PICKED_UP".equals(req.status)) {
            order.setPickedUpAt(LocalDateTime.now());
        } else if ("DELIVERED".equals(req.status)) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }

    // Get all delivery persons (for admin dropdown)
    @GetMapping("/persons")
    public List<User> getAllDeliveryPersons() {
        return userRepository.findByRole("DELIVERY");
    }

    // Request classes
    public static class AssignRequest {
        public Long orderId;
        public Long deliveryPersonId;
    }

    public static class UpdateStatusRequest {
        public Long orderId;
        public Long deliveryPersonId;
        public String status; // PICKED_UP, DELIVERED, CANCELLED
    }
}