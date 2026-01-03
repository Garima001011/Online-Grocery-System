package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.*;
import com.grocery.localgrocery.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderController(OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // Get all orders
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Get orders by user ID
    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable Long userId) {
        return orderRepository.findByUserId(userId);
    }

    // Get specific order by ID
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    public static class PlaceOrderRequest {
        public Long userId;
        public String deliveryAddress;
        public List<Item> items;

        public static class Item {
            public Long productId;
            public int quantity;
        }
    }

    @PostMapping
    public Order placeOrder(@RequestBody PlaceOrderRequest req) {
        if (req.userId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId required");
        if (req.deliveryAddress == null || req.deliveryAddress.trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "deliveryAddress required");
        if (req.items == null || req.items.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "items required");

        User user = userRepository.findById(req.userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setDeliveryAddress(req.deliveryAddress.trim());

        // Calculate totals
        double subtotal = 0;
        for (PlaceOrderRequest.Item i : req.items) {
            Product p = productRepository.findById(i.productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
            subtotal += p.getPrice() * i.quantity;
        }

        double tax = subtotal * 0.13; // 13% VAT
        double total = subtotal + tax;

        order.setSubtotal(java.math.BigDecimal.valueOf(subtotal));
        order.setTax(java.math.BigDecimal.valueOf(tax));
        order.setTotal(java.math.BigDecimal.valueOf(total));

        order = orderRepository.save(order);

        for (PlaceOrderRequest.Item i : req.items) {
            if (i.quantity <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be > 0");
            }

            Product p = productRepository.findById(i.productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

            if (p.getStock() < i.quantity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough stock for: " + p.getName());
            }

            // reduce stock
            p.setStock(p.getStock() - i.quantity);
            productRepository.save(p);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(i.quantity);
            oi.setPriceAtPurchase(p.getPrice());
            orderItemRepository.save(oi);
        }

        return order;
    }

    // Add delivery notes endpoint
    @PostMapping("/{id}/notes")
    public Order updateDeliveryNotes(@PathVariable Long id, @RequestBody NotesRequest req) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        order.setDeliveryNotes(req.notes);
        return orderRepository.save(order);
    }

    // Request return for an order item
    @PostMapping("/{orderId}/items/{itemId}/return")
    public ResponseEntity<?> requestReturn(@PathVariable Long orderId,
                                           @PathVariable Long itemId,
                                           @RequestBody ReturnRequest req) {

        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found"));

        // Verify item belongs to order
        if (!item.getOrder().getId().equals(orderId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item does not belong to this order");
        }

        // Check if already has a return request
        if (!"NONE".equals(item.getReturnStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Return already requested for this item");
        }

        // Check if order was delivered (can only return delivered items)
        if (!"DELIVERED".equals(item.getOrder().getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only return delivered items");
        }

        // Check if within return period (7 days)
        LocalDateTime deliveredAt = item.getOrder().getDeliveredAt();
        if (deliveredAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item not yet delivered");
        }

        if (LocalDateTime.now().isAfter(deliveredAt.plusDays(7))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Return period (7 days) has expired");
        }

        // Update return status
        item.setReturnStatus("REQUESTED");
        item.setReturnReason(req.reason);
        item.setReturnDescription(req.description + " | Policy: " + req.policy);
        item.setReturnRequestedAt(LocalDateTime.now());

        // Calculate refund amount (full amount for now)
        item.setRefundAmount(item.getPriceAtPurchase() * item.getQuantity());

        orderItemRepository.save(item);

        // You can add notification logic here for admin
        // notifyAdminAboutReturn(orderId, itemId, req.reason);

        return ResponseEntity.ok(Map.of(
                "message", "Return request submitted successfully",
                "policy", "NEPAL CAN MOVE FAST will contact you for pickup"
        ));
    }

    // Get all return requests (for admin)
    @GetMapping("/returns")
    public List<OrderItem> getReturnRequests() {
        return orderItemRepository.findByReturnStatusNot("NONE");
    }

    // Update return status (for admin)
    @PostMapping("/{orderId}/items/{itemId}/return-status")
    public OrderItem updateReturnStatus(@PathVariable Long orderId,
                                        @PathVariable Long itemId,
                                        @RequestBody ReturnStatusRequest req) {

        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found"));

        // Verify item belongs to order
        if (!item.getOrder().getId().equals(orderId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item does not belong to this order");
        }

        item.setReturnStatus(req.status);
        item.setReturnProcessedAt(LocalDateTime.now());

        // If refunded, update payment status
        if ("REFUNDED".equals(req.status)) {
            item.getOrder().setPaymentStatus("REFUND_PROCESSED");
            orderRepository.save(item.getOrder());
        }

        return orderItemRepository.save(item);
    }

    // Request classes
    public static class NotesRequest {
        public String notes;
    }

    public static class ReturnRequest {
        public String reason;
        public String description;
        public String policy; // "NEPAL CAN MOVE FAST"
    }

    public static class ReturnStatusRequest {
        public String status; // APPROVED, REJECTED, REFUNDED
    }
}