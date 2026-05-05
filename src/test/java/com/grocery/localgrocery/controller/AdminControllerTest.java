package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.*;
import com.grocery.localgrocery.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private OrderItemRepository orderItemRepository;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private StoreRepository storeRepository;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private NotificationLogRepository notificationLogRepository;

    @Test
    void getDashboardStatsShouldReturnStats() throws Exception {
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());
        when(orderRepository.count()).thenReturn(0L);
        when(userRepository.countByRole("CUSTOMER")).thenReturn(0L);
        when(orderItemRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/dashboard"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalRevenue").value(0))
               .andExpect(jsonPath("$.totalOrders").value(0))
               .andExpect(jsonPath("$.totalCustomers").value(0))
               .andExpect(jsonPath("$.pendingReturns").value(0));
    }

    @Test
    void getRecentOrdersShouldReturnSortedOrders() throws Exception {
        Order o1 = new Order();
        Order o2 = new Order();

        when(orderRepository.findAll()).thenReturn(List.of(o1, o2));

        mockMvc.perform(get("/api/admin/orders/recent?limit=5"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void processReturnShouldApproveReturn() throws Exception {
        Order order = new Order();

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setReturnStatus("REQUESTED");

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(item);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        String json = """
            {"action":"APPROVED"}
            """;

        mockMvc.perform(post("/api/admin/returns/1/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isOk());
    }

    @Test
    void processReturnShouldRefundAndUpdateOrder() throws Exception {
        Order order = new Order();
        order.setPaymentStatus("COMPLETED");

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setReturnStatus("REQUESTED");

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(item);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        String json = """
            {"action":"REFUNDED"}
            """;

        mockMvc.perform(post("/api/admin/returns/1/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isOk());
    }

    @Test
    void processReturnShouldRejectInvalidAction() throws Exception {
        OrderItem item = new OrderItem();

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(item));

        String json = """
            {"action":"INVALID"}
            """;

        mockMvc.perform(post("/api/admin/returns/1/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isBadRequest());
    }

    @Test
    void getUserStatsShouldReturnRoleCounts() throws Exception {
        User admin = new User();
        admin.setRole("ADMIN");
        User customer = new User();
        customer.setRole("CUSTOMER");

        when(userRepository.findAll()).thenReturn(List.of(admin, customer));

        mockMvc.perform(get("/api/admin/users/stats"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.total").value(2))
               .andExpect(jsonPath("$.byRole.ADMIN").value(1))
               .andExpect(jsonPath("$.byRole.CUSTOMER").value(1));
    }

    @Test
    void assignOrderShouldSucceed() throws Exception {
        Order order = new Order();

        User deliveryPerson = new User();
        deliveryPerson.setRole("DELIVERY");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findById(2L)).thenReturn(Optional.of(deliveryPerson));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        String json = """
            {"deliveryPersonId":2}
            """;

        mockMvc.perform(post("/api/admin/orders/1/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isOk());
    }

    @Test
    void assignOrderShouldRejectNonDeliveryUser() throws Exception {
        Order order = new Order();

        User customer = new User();
        customer.setRole("CUSTOMER");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findById(2L)).thenReturn(Optional.of(customer));

        String json = """
            {"deliveryPersonId":2}
            """;

        mockMvc.perform(post("/api/admin/orders/1/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isBadRequest());
    }

    @Test
    void getDeliveryPersonsShouldReturnDeliveryUsers() throws Exception {
        User dp = new User();
        dp.setRole("DELIVERY");
        dp.setName("Rider One");

        when(userRepository.findByRole("DELIVERY")).thenReturn(List.of(dp));

        mockMvc.perform(get("/api/admin/delivery-persons"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].name").value("Rider One"));
    }
}
