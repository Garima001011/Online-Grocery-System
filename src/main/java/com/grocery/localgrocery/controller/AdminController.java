package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.*;
import com.grocery.localgrocery.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;

    public AdminController(UserRepository userRepository,
                           OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           ProductRepository productRepository,
                           CategoryRepository categoryRepository,
                           StoreRepository storeRepository,
                           NotificationRepository notificationRepository,
                           NotificationLogRepository notificationLogRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
        this.notificationRepository = notificationRepository;
        this.notificationLogRepository = notificationLogRepository;
    }

    // Dashboard stats
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Get total revenue (sum of all order totals)
        BigDecimal totalRevenue = orderRepository.findAll().stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);

        // Get total orders
        long totalOrders = orderRepository.count();
        stats.put("totalOrders", totalOrders);

        // Get total customers
        long totalCustomers = userRepository.countByRole("CUSTOMER");
        stats.put("totalCustomers", totalCustomers);

        // Get pending returns
        List<OrderItem> allItems = orderItemRepository.findAll();
        long pendingReturns = allItems.stream()
                .filter(item -> item.getReturnStatus() != null && item.getReturnStatus().equals("REQUESTED"))
                .count();
        stats.put("pendingReturns", pendingReturns);

        // Get today's stats
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);

        List<Order> todayOrders = orderRepository.findAll().stream()
                .filter(order -> order.getCreatedAt() != null &&
                        !order.getCreatedAt().isBefore(startOfDay) &&
                        !order.getCreatedAt().isAfter(endOfDay))
                .toList();

        BigDecimal todayRevenue = todayOrders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> todayStats = new HashMap<>();
        todayStats.put("sales", todayRevenue);
        todayStats.put("orders", todayOrders.size());
        todayStats.put("customers", todayOrders.stream()
                .map(Order::getUser)
                .distinct()
                .count());
        stats.put("todayStats", todayStats);

        // Calculate growth percentages (simplified - you can add actual calculations)
        stats.put("revenueGrowth", 12.5);
        stats.put("orderGrowth", 8.3);
        stats.put("customerGrowth", 5.7);
        stats.put("returnRate", 2.1);

        return stats;
    }

    // Get recent orders
    // In AdminController, change getRecentOrders method:
    @GetMapping("/orders/recent")
    public List<Order> getRecentOrders(@RequestParam(defaultValue = "10") int limit) {
        return orderRepository.findAll().stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Get returns for admin
    @GetMapping("/returns")
    public List<Map<String, Object>> getReturns() {
        List<OrderItem> allItems = orderItemRepository.findAll();
        List<Map<String, Object>> returns = new ArrayList<>();

        for (OrderItem item : allItems) {
            if (item.getReturnStatus() != null && !item.getReturnStatus().equals("NONE")) {
                Map<String, Object> returnItem = new HashMap<>();
                returnItem.put("id", item.getId());
                returnItem.put("orderId", item.getOrder().getId());
                returnItem.put("customerName", item.getOrder().getUser().getName());
                returnItem.put("customerEmail", item.getOrder().getUser().getEmail());
                returnItem.put("productName", item.getProduct().getName());
                returnItem.put("quantity", item.getQuantity());
                returnItem.put("priceAtPurchase", item.getPriceAtPurchase());
                returnItem.put("returnStatus", item.getReturnStatus());
                returnItem.put("returnReason", item.getReturnReason());
                returnItem.put("returnRequestedAt", item.getReturnRequestedAt());
                returnItem.put("refundAmount", item.getRefundAmount());
                returns.add(returnItem);
            }
        }

        return returns;
    }

    // Process return
    @PostMapping("/returns/{id}/process")
    public ResponseEntity<?> processReturn(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String action = request.get("action");

        OrderItem item = orderItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return not found"));

        if (!Arrays.asList("APPROVED", "REJECTED", "REFUNDED").contains(action)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid action");
        }

        item.setReturnStatus(action);
        item.setReturnProcessedAt(LocalDateTime.now());

        if ("REFUNDED".equals(action)) {
            // Update order payment status
            Order order = item.getOrder();
            order.setPaymentStatus("REFUNDED");
            orderRepository.save(order);
        }

        orderItemRepository.save(item);

        return ResponseEntity.ok().build();
    }

    // Get charts data - Sales over time
    @GetMapping("/charts/sales")
    public Map<String, Object> getSalesChartData(@RequestParam(defaultValue = "7") int days) {
        Map<String, Object> chartData = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // Get all orders
        List<Order> allOrders = orderRepository.findAll();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            labels.add(date.toString());

            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            double dayTotal = allOrders.stream()
                    .filter(order -> order.getCreatedAt() != null &&
                            !order.getCreatedAt().isBefore(startOfDay) &&
                            !order.getCreatedAt().isAfter(endOfDay))
                    .mapToDouble(order -> order.getTotal().doubleValue())
                    .sum();

            values.add(dayTotal);
        }

        chartData.put("labels", labels);
        chartData.put("values", values);

        return chartData;
    }

    // Get orders by status for chart
    @GetMapping("/charts/orders-by-status")
    public Map<String, Object> getOrdersByStatus() {
        Map<String, Object> chartData = new HashMap<>();

        List<String> statuses = Arrays.asList("PLACED", "ASSIGNED", "PICKED_UP", "DELIVERED", "CANCELLED");
        List<Long> counts = new ArrayList<>();

        List<Order> allOrders = orderRepository.findAll();
        for (String status : statuses) {
            long count = allOrders.stream()
                    .filter(order -> status.equals(order.getStatus()))
                    .count();
            counts.add(count);
        }

        chartData.put("labels", statuses);
        chartData.put("values", counts);

        return chartData;
    }

    // Get notifications
    @GetMapping("/notifications")
    public List<Notification> getNotifications() {
        return notificationRepository.findAll().stream()
                .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()))
                .toList();
    }

    // Notification Request DTO
    public static class NotificationRequest {
        private String title;
        private String message;
        private String type;
        private String targetAudience;
        private LocalDateTime scheduledFor;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTargetAudience() { return targetAudience; }
        public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

        public LocalDateTime getScheduledFor() { return scheduledFor; }
        public void setScheduledFor(LocalDateTime scheduledFor) { this.scheduledFor = scheduledFor; }
    }

    // Send notification
    @PostMapping("/notifications")
    public Notification sendNotification(@RequestBody NotificationRequest request) {

        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setTargetAudience(request.getTargetAudience());
        notification.setScheduledFor(request.getScheduledFor());
        notification.setCreatedAt(LocalDateTime.now());

        // Determine status
        if (request.getScheduledFor() != null &&
                request.getScheduledFor().isAfter(LocalDateTime.now())) {
            notification.setStatus("SCHEDULED");
        } else {
            notification.setStatus("SENT");
        }

        // Save notification
        notification = notificationRepository.save(notification);

        // Only create logs immediately if actually sent now
        if ("SENT".equals(notification.getStatus())) {
            List<User> targetUsers = getTargetUsers(request.getTargetAudience());

            for (User user : targetUsers) {
                NotificationLog log = new NotificationLog();
                log.setNotification(notification);
                log.setUser(user);
                log.setSentAt(LocalDateTime.now());
                notificationLogRepository.save(log);
            }
        }
        return notification;
    }

    private List<User> getTargetUsers(String audience) {
        switch (audience) {
            case "CUSTOMERS":
                return userRepository.findByRole("CUSTOMER");
            case "DELIVERY":
                return userRepository.findByRole("DELIVERY");
            case "ALL":
                return userRepository.findAll();
            default:
                return userRepository.findAll();
        }
    }
    // Add product with image
    @PostMapping("/products")
    public ResponseEntity<?> addProductWithImage(
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("stock") Integer stock,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("storeId") Long storeId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        // Save image and get URL (store in local folder)
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                String uploadDir = "uploads/products/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String safeOriginal = Objects.requireNonNullElse(image.getOriginalFilename(), "image")
                        .replaceAll("[^a-zA-Z0-9.-]", "_");
                String fileName = UUID.randomUUID() + "_" + safeOriginal;

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(image.getInputStream(), filePath); // optionally add REPLACE_EXISTING

                imageUrl = "/" + uploadDir + fileName;
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to upload image: " + e.getMessage());
            }
        }

        try {
            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setStock(stock);

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
            product.setCategory(category);

            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
            product.setStore(store);

            // FIX: only set when present (so you don’t overwrite with null)
            if (description != null && !description.isBlank()) {
                product.setDescription(description);
            }
            if (imageUrl != null) {
                product.setImageUrl(imageUrl);
            }

            productRepository.save(product);
            return ResponseEntity.ok(product);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save product: " + e.getMessage());
        }
    }

    // Get all delivery persons
    @GetMapping("/delivery-persons")
    public List<User> getAllDeliveryPersons() {
        return userRepository.findByRole("DELIVERY");
    }

    // Get unassigned orders
    @GetMapping("/orders/unassigned")
    public List<Order> getUnassignedOrders() {
        return orderRepository.findAll().stream()
                .filter(order -> order.getDeliveryPerson() == null &&
                        !"DELIVERED".equals(order.getStatus()) &&
                        !"CANCELLED".equals(order.getStatus()))
                .toList();
    }

    // Assign order to delivery person
    @PostMapping("/orders/{orderId}/assign")
    public ResponseEntity<?> assignOrder(@PathVariable Long orderId,
                                         @RequestBody Map<String, Long> request) {
        Long deliveryPersonId = request.get("deliveryPersonId");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        User deliveryPerson = userRepository.findById(deliveryPersonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery person not found"));

        if (!"DELIVERY".equals(deliveryPerson.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a delivery person");
        }

        order.setDeliveryPerson(deliveryPerson);
        order.setStatus("ASSIGNED");
        order.setAssignedAt(LocalDateTime.now());

        orderRepository.save(order);

        return ResponseEntity.ok(order);
    }

    // Get assigned orders
    @GetMapping("/orders/assigned")
    public List<Order> getAssignedOrders() {
        return orderRepository.findAll().stream()
                .filter(order -> order.getDeliveryPerson() != null &&
                        !"DELIVERED".equals(order.getStatus()))
                .toList();
    }

    // Get all users with statistics
    @GetMapping("/users/stats")
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();

        // Get user count by role
        List<User> allUsers = userRepository.findAll();
        Map<String, Long> roleCount = new HashMap<>();

        for (User user : allUsers) {
            roleCount.put(user.getRole(), roleCount.getOrDefault(user.getRole(), 0L) + 1);
        }

        stats.put("byRole", roleCount);
        stats.put("total", allUsers.size());

        return stats;
    }

    // Add a new delivery partner
    @PostMapping("/delivery-partners")
    public ResponseEntity<?> addDeliveryPartner(@RequestBody User newUser) {
        // Check if email already exists
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        // Set role to DELIVERY
        newUser.setRole("DELIVERY");

        User savedUser = userRepository.save(newUser);
        return ResponseEntity.ok(savedUser);
    }

    // Get top products
    @GetMapping("/products/top")
    public List<Map<String, Object>> getTopProducts(@RequestParam(defaultValue = "10") int limit) {
        // Get all order items and count product occurrences
        Map<Product, Long> productCount = new HashMap<>();
        List<OrderItem> allItems = orderItemRepository.findAll();

        for (OrderItem item : allItems) {
            Product product = item.getProduct();
            productCount.put(product, productCount.getOrDefault(product, 0L) + item.getQuantity());
        }

        // Sort by count and return top products
        return productCount.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> productInfo = new HashMap<>();
                    productInfo.put("product", entry.getKey());
                    productInfo.put("salesCount", entry.getValue());
                    return productInfo;
                })
                .toList();
    }

// AdminController.java - Add these methods

    // Get user with details for admin
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("id", user.getId());
        userDetails.put("name", user.getName());
        userDetails.put("email", user.getEmail());
        userDetails.put("role", user.getRole());
        userDetails.put("phone", user.getPhone());
        userDetails.put("profileImageUrl", user.getProfileImageUrl());
        userDetails.put("createdAt", user.getCreatedAt());

        if ("DELIVERY".equals(user.getRole())) {
            userDetails.put("vehicleType", user.getVehicleType());
            userDetails.put("vehicleNumber", user.getVehicleNumber());
            userDetails.put("isAvailable", user.getIsAvailable());
            userDetails.put("currentLocation", user.getCurrentLocation());
            userDetails.put("rating", user.getRating());
            userDetails.put("totalDeliveries", user.getTotalDeliveries());

            // Get delivery stats
            List<Order> deliveryOrders = orderRepository.findByDeliveryPersonId(id);
            userDetails.put("completedDeliveries", deliveryOrders.stream()
                    .filter(o -> "DELIVERED".equals(o.getStatus()))
                    .count());
            userDetails.put("activeDeliveries", deliveryOrders.stream()
                    .filter(o -> !"DELIVERED".equals(o.getStatus()) && !"CANCELLED".equals(o.getStatus()))
                    .count());
        }

        return ResponseEntity.ok(userDetails);
    }

    // Update user details
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (updatedUser.getName() != null) user.setName(updatedUser.getName());
        if (updatedUser.getPhone() != null) user.setPhone(updatedUser.getPhone());

        if ("DELIVERY".equals(user.getRole())) {
            if (updatedUser.getVehicleType() != null) user.setVehicleType(updatedUser.getVehicleType());
            if (updatedUser.getVehicleNumber() != null) user.setVehicleNumber(updatedUser.getVehicleNumber());
            if (updatedUser.getIsAvailable() != null) user.setIsAvailable(updatedUser.getIsAvailable());
            if (updatedUser.getCurrentLocation() != null) user.setCurrentLocation(updatedUser.getCurrentLocation());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    // Toggle delivery person availability
    @PostMapping("/delivery/{id}/availability")
    public ResponseEntity<?> toggleAvailability(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        User deliveryPerson = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery person not found"));

        if (!"DELIVERY".equals(deliveryPerson.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a delivery person");
        }

        deliveryPerson.setIsAvailable(request.get("isAvailable"));
        userRepository.save(deliveryPerson);

        return ResponseEntity.ok(Map.of(
                "message", "Availability updated",
                "isAvailable", deliveryPerson.getIsAvailable()
        ));
    }

    // Get delivery statistics
    @GetMapping("/delivery/stats")
    public Map<String, Object> getDeliveryStats() {
        Map<String, Object> stats = new HashMap<>();

        List<User> deliveryPersons = userRepository.findByRole("DELIVERY");
        stats.put("totalDeliveryPersons", deliveryPersons.size());
        stats.put("availableDeliveryPersons", deliveryPersons.stream()
                .filter(User::getIsAvailable)
                .count());

        // Average rating
        double avgRating = deliveryPersons.stream()
                .mapToDouble(User::getRating)
                .average()
                .orElse(0.0);
        stats.put("averageRating", avgRating);

        // Total deliveries
        int totalDeliveries = deliveryPersons.stream()
                .mapToInt(User::getTotalDeliveries)
                .sum();
        stats.put("totalDeliveries", totalDeliveries);

        return stats;
    }
}