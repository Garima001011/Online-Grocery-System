package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.Order;
import com.grocery.localgrocery.entity.User;
import com.grocery.localgrocery.repository.OrderRepository;
import com.grocery.localgrocery.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.grocery.localgrocery.entity.DeliveryIssue;
import com.grocery.localgrocery.entity.DeliverySession;
import com.grocery.localgrocery.repository.DeliveryIssueRepository;
import com.grocery.localgrocery.repository.DeliverySessionRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DeliveryIssueRepository issueRepository;
    private final DeliverySessionRepository sessionRepository;

    public DeliveryController(OrderRepository orderRepository,
                              UserRepository userRepository,
                              DeliveryIssueRepository issueRepository,
                              DeliverySessionRepository sessionRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
        this.sessionRepository = sessionRepository;
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

    // Update order status (modified to handle PAYMENT_RECEIVED and COD)
    @PostMapping("/update-status")
    public Order updateStatus(@RequestBody UpdateStatusRequest req) {
        Order order = orderRepository.findById(req.orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getDeliveryPerson() == null ||
                !order.getDeliveryPerson().getId().equals(req.deliveryPersonId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not assigned to this order");
        }

        // Validate status transition
        String newStatus = req.status;
        String currentStatus = order.getStatus();

        if ("PAYMENT_RECEIVED".equals(newStatus) && !"PICKED_UP".equals(currentStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must be PICKED_UP before confirming payment");
        }
        if ("DELIVERED".equals(newStatus) && !"PAYMENT_RECEIVED".equals(currentStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment must be received before delivery");
        }

        order.setStatus(newStatus);

        // Set timestamps and COD flag
        if ("PICKED_UP".equals(newStatus)) {
            order.setPickedUpAt(LocalDateTime.now());
        } else if ("PAYMENT_RECEIVED".equals(newStatus)) {
            order.setPaymentReceivedAt(LocalDateTime.now());
            if (req.codCollected != null) {
                order.setCodCollected(req.codCollected);
            }
        } else if ("DELIVERED".equals(newStatus)) {
            order.setDeliveredAt(LocalDateTime.now());
            // Update delivery person's stats
            User deliveryPerson = order.getDeliveryPerson();
            deliveryPerson.setTotalDeliveries(deliveryPerson.getTotalDeliveries() + 1);
            // Add earnings (e.g., fixed delivery fee)
            BigDecimal deliveryFee = new BigDecimal("50.00"); // configurable
            deliveryPerson.setTotalEarnings(deliveryPerson.getTotalEarnings().add(deliveryFee));
            // Recalculate performance badge
            updatePerformanceBadge(deliveryPerson);
            userRepository.save(deliveryPerson);
        }

        return orderRepository.save(order);
    }

    // New endpoint: upload delivery proof photo
    @PostMapping("/upload-proof")
    public ResponseEntity<?> uploadProof(@RequestParam("orderId") Long orderId,
                                         @RequestParam("deliveryPersonId") Long deliveryPersonId,
                                         @RequestParam("file") MultipartFile file) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getDeliveryPerson().getId().equals(deliveryPersonId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not assigned to this order");
        }

        try {
            // Save file (similar to product image upload)
            String uploadDir = "uploads/delivery-proof/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            String fileUrl = "/" + uploadDir + fileName;

            order.setDeliveryProofImageUrl(fileUrl);
            orderRepository.save(order);
            return ResponseEntity.ok(Map.of("imageUrl", fileUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload proof: " + e.getMessage());
        }
    }

    // Report an issue
    @PostMapping("/report-issue")
    public ResponseEntity<?> reportIssue(@RequestBody IssueRequest req) {
        Order order = orderRepository.findById(req.orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        User deliveryPerson = userRepository.findById(req.deliveryPersonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery person not found"));

        DeliveryIssue issue = new DeliveryIssue();
        issue.setOrder(order);
        issue.setReportedBy(deliveryPerson);
        issue.setIssueType(req.issueType);
        issue.setDescription(req.description);
        issue.setReportedAt(LocalDateTime.now());
        issue.setStatus("OPEN");

        issueRepository.save(issue);
        return ResponseEntity.ok(issue);
    }

    // Update live location
    @PostMapping("/location")
    public ResponseEntity<?> updateLocation(@RequestBody LocationRequest req) {
        User deliveryPerson = userRepository.findById(req.deliveryPersonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery person not found"));
        deliveryPerson.setCurrentLocation(req.latitude + "," + req.longitude);
        userRepository.save(deliveryPerson);
        return ResponseEntity.ok().build();
    }

    // Online status tracking
    @PostMapping("/online-status")
    public ResponseEntity<?> setOnlineStatus(@RequestBody OnlineStatusRequest req) {
        User deliveryPerson = userRepository.findById(req.deliveryPersonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery person not found"));

        boolean wasOnline = deliveryPerson.getOnlineStatus() != null && deliveryPerson.getOnlineStatus();

        if (req.online && !wasOnline) {
            // Going online
            deliveryPerson.setOnlineStatus(true);
            deliveryPerson.setLastOnlineTime(LocalDateTime.now());
            // Start a session
            DeliverySession session = new DeliverySession();
            session.setDeliveryPerson(deliveryPerson);
            session.setStartTime(LocalDateTime.now());
            sessionRepository.save(session);
        } else if (!req.online && wasOnline) {
            // Going offline
            deliveryPerson.setOnlineStatus(false);
            // End current open session
            List<DeliverySession> openSessions = sessionRepository.findByDeliveryPersonAndEndTimeIsNull(deliveryPerson);
            for (DeliverySession s : openSessions) {
                s.setEndTime(LocalDateTime.now());
                sessionRepository.save(s);
            }
        }

        userRepository.save(deliveryPerson);
        return ResponseEntity.ok().build();
    }

    // Earnings summary
    @GetMapping("/earnings/summary")
    public Map<String, Object> getEarningsSummary(@RequestParam Long deliveryPersonId,
                                                  @RequestParam String period) { // "daily" or "weekly"
        User deliveryPerson = userRepository.findById(deliveryPersonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery person not found"));

        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();

        if ("daily".equalsIgnoreCase(period)) {
            start = end.toLocalDate().atStartOfDay();
        } else if ("weekly".equalsIgnoreCase(period)) {
            start = end.toLocalDate().minusDays(6).atStartOfDay();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Period must be 'daily' or 'weekly'");
        }

        // Get orders delivered in that period
        List<Order> deliveredOrders = orderRepository.findByDeliveryPersonIdAndStatusAndDeliveredAtBetween(
                deliveryPersonId, "DELIVERED", start, end);

        int totalDeliveries = deliveredOrders.size();
        BigDecimal totalEarnings = deliveredOrders.stream()
                .map(o -> new BigDecimal("50.00")) // delivery fee per order
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Add incentives and bonus (dummy for now, could be from user fields)
        BigDecimal incentives = deliveryPerson.getIncentives() != null ? deliveryPerson.getIncentives() : BigDecimal.ZERO;
        BigDecimal bonus = deliveryPerson.getBonus() != null ? deliveryPerson.getBonus() : BigDecimal.ZERO;

        Map<String, Object> summary = new HashMap<>();
        summary.put("period", period);
        summary.put("totalDeliveries", totalDeliveries);
        summary.put("totalEarnings", totalEarnings);
        summary.put("incentives", incentives);
        summary.put("bonus", bonus);
        summary.put("performanceBadge", deliveryPerson.getPerformanceBadge());

        return summary;
    }

    // Get all delivery persons (for admin dropdown)
    @GetMapping("/persons")
    public List<User> getAllDeliveryPersons() {
        return userRepository.findByRole("DELIVERY");
    }

    // Helper to update performance badge based on deliveries and rating
    private void updatePerformanceBadge(User deliveryPerson) {
        int deliveries = deliveryPerson.getTotalDeliveries();
        double rating = deliveryPerson.getRating();

        if (deliveries >= 100 && rating >= 4.5) {
            deliveryPerson.setPerformanceBadge("GOLD");
        } else if (deliveries >= 50 && rating >= 4.0) {
            deliveryPerson.setPerformanceBadge("SILVER");
        } else if (deliveries >= 10) {
            deliveryPerson.setPerformanceBadge("BRONZE");
        } else {
            deliveryPerson.setPerformanceBadge(null);
        }
    }

    // Request DTOs
    public static class AssignRequest {
        public Long orderId;
        public Long deliveryPersonId;
    }

    public static class UpdateStatusRequest {
        public Long orderId;
        public Long deliveryPersonId;
        public String status; // PICKED_UP, PAYMENT_RECEIVED, DELIVERED, CANCELLED
        public Boolean codCollected; // for PAYMENT_RECEIVED
    }

    public static class IssueRequest {
        public Long orderId;
        public Long deliveryPersonId;
        public String issueType;
        public String description;
    }

    public static class LocationRequest {
        public Long deliveryPersonId;
        public double latitude;
        public double longitude;
    }

    public static class OnlineStatusRequest {
        public Long deliveryPersonId;
        public boolean online;
    }
}