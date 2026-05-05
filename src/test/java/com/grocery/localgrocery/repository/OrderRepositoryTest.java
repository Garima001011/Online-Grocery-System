package com.grocery.localgrocery.repository;

import com.grocery.localgrocery.entity.Order;
import com.grocery.localgrocery.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindOrder() {
        User user = new User();
        user.setEmail("order@test.com");
        user.setPassword("pass");
        user.setRole("CUSTOMER");
        user.setName("Order User");
        user.setPhone("5555555555");
        user = userRepository.save(user);

        Order order = new Order();
        order.setUser(user);
        order.setDeliveryAddress("Kathmandu");
        order.setTotal(new BigDecimal("150.00"));

        Order saved = orderRepository.save(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(orderRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void shouldFindByUserId() {
        User user = new User();
        user.setEmail("u1@test.com");
        user.setPassword("pass");
        user.setRole("CUSTOMER");
        user.setName("U1");
        user.setPhone("6666666666");
        user = userRepository.save(user);

        Order order = new Order();
        order.setUser(user);
        order.setDeliveryAddress("Patan");
        order.setTotal(new BigDecimal("200.00"));
        orderRepository.save(order);

        List<Order> found = orderRepository.findByUserId(user.getId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getDeliveryAddress()).isEqualTo("Patan");
    }

    @Test
    void shouldFindByDeliveryPersonId() {
        User customer = new User();
        customer.setEmail("c@test.com");
        customer.setPassword("pass");
        customer.setRole("CUSTOMER");
        customer.setName("Customer");
        customer.setPhone("7777777777");
        customer = userRepository.save(customer);

        User delivery = new User();
        delivery.setEmail("d@test.com");
        delivery.setPassword("pass");
        delivery.setRole("DELIVERY");
        delivery.setName("Delivery");
        delivery.setPhone("8888888888");
        delivery = userRepository.save(delivery);

        Order order = new Order();
        order.setUser(customer);
        order.setDeliveryPerson(delivery);
        order.setDeliveryAddress("Bhaktapur");
        order.setStatus("ASSIGNED");
        order.setTotal(new BigDecimal("100.00"));
        orderRepository.save(order);

        List<Order> found = orderRepository.findByDeliveryPersonId(delivery.getId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStatus()).isEqualTo("ASSIGNED");
    }

    @Test
    void shouldCountByStatus() {
        User user = new User();
        user.setEmail("count@test.com");
        user.setPassword("pass");
        user.setRole("CUSTOMER");
        user.setName("Count");
        user.setPhone("9999999999");
        user = userRepository.save(user);

        Order o1 = new Order();
        o1.setUser(user);
        o1.setDeliveryAddress("A");
        o1.setStatus("PLACED");
        o1.setTotal(BigDecimal.TEN);
        orderRepository.save(o1);

        Order o2 = new Order();
        o2.setUser(user);
        o2.setDeliveryAddress("B");
        o2.setStatus("PLACED");
        o2.setTotal(BigDecimal.TEN);
        orderRepository.save(o2);

        Order o3 = new Order();
        o3.setUser(user);
        o3.setDeliveryAddress("C");
        o3.setStatus("DELIVERED");
        o3.setTotal(BigDecimal.TEN);
        orderRepository.save(o3);

        assertThat(orderRepository.countByStatus("PLACED")).isEqualTo(2);
        assertThat(orderRepository.countByStatus("DELIVERED")).isEqualTo(1);
    }
}
