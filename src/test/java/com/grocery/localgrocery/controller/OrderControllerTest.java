package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.Order;
import com.grocery.localgrocery.entity.OrderItem;
import com.grocery.localgrocery.entity.Product;
import com.grocery.localgrocery.entity.User;
import com.grocery.localgrocery.repository.OrderItemRepository;
import com.grocery.localgrocery.repository.OrderRepository;
import com.grocery.localgrocery.repository.ProductRepository;
import com.grocery.localgrocery.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private OrderItemRepository orderItemRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ProductRepository productRepository;

    @Test
    void getAllOrdersShouldReturnList() throws Exception {
        Order order = new Order();
        order.setStatus("PLACED");

        when(orderRepository.findAll()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].status").value("PLACED"));
    }

    @Test
    void getOrderByIdShouldReturnOrder() throws Exception {
        User user = new User();
        user.setEmail("user@test.com");

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PLACED");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/orders/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("PLACED"));
    }

    @Test
    void getOrderByIdShouldReturnNotFound() throws Exception {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/99"))
               .andExpect(status().isNotFound());
    }

    @Test
    void placeOrderShouldCreateOrder() throws Exception {
        User user = new User();
        user.setEmail("user@test.com");

        Product product = new Product();
        product.setName("Rice");
        product.setPrice(50.0);
        product.setStock(10);
        product.setWeightKg(5.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));

        String json = """
            {"userId":1,"deliveryAddress":"Kathmandu","items":[{"productId":1,"quantity":2}],"subtotal":100.0,"tax":13.0,"shippingFee":100.0,"total":213.0,"paymentMethod":"COD"}
            """;

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isOk());
    }

    @Test
    void placeOrderShouldRejectMissingUserId() throws Exception {
        String json = """
            {"deliveryAddress":"Kathmandu","items":[{"productId":1,"quantity":1}]}
            """;

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isBadRequest());
    }

    @Test
    void placeOrderShouldRejectEmptyItems() throws Exception {
        String json = """
            {"userId":1,"deliveryAddress":"Kathmandu","items":[]}
            """;

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isBadRequest());
    }

    @Test
    void cancelOrderShouldSucceed() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("owner@test.com");

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PLACED");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        String json = """
            {"reason":"Changed my mind"}
            """;

        mockMvc.perform(post("/api/orders/1/cancel?userId=1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Order cancelled successfully"));
    }

    @Test
    void cancelOrderShouldRejectIfNotOwner() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setEmail("other@test.com");

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PLACED");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        String json = """
            {"reason":"Changed my mind"}
            """;

        mockMvc.perform(post("/api/orders/1/cancel?userId=1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isForbidden());
    }

    @Test
    void cancelOrderShouldRejectIfAlreadyDelivered() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("owner@test.com");

        Order order = new Order();
        order.setUser(user);
        order.setStatus("DELIVERED");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        String json = """
            {"reason":"Changed my mind"}
            """;

        mockMvc.perform(post("/api/orders/1/cancel?userId=1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isBadRequest());
    }
}
