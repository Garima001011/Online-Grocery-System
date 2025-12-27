package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.*;
import com.grocery.localgrocery.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
}
