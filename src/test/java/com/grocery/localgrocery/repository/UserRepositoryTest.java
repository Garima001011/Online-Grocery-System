package com.grocery.localgrocery.repository;

import com.grocery.localgrocery.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindByEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("secret");
        user.setRole("CUSTOMER");
        user.setName("Test User");
        user.setPhone("1234567890");

        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("test@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test User");
    }

    @Test
    void shouldFindByRole() {
        User customer = new User();
        customer.setEmail("c@example.com");
        customer.setPassword("pass");
        customer.setRole("CUSTOMER");
        customer.setName("Customer");
        customer.setPhone("1111111111");

        User delivery = new User();
        delivery.setEmail("d@example.com");
        delivery.setPassword("pass");
        delivery.setRole("DELIVERY");
        delivery.setName("Delivery");
        delivery.setPhone("2222222222");

        userRepository.save(customer);
        userRepository.save(delivery);

        List<User> customers = userRepository.findByRole("CUSTOMER");
        assertThat(customers).hasSize(1);
        assertThat(customers.get(0).getEmail()).isEqualTo("c@example.com");
    }

    @Test
    void shouldCountByRole() {
        User u1 = new User();
        u1.setEmail("a@example.com");
        u1.setPassword("pass");
        u1.setRole("CUSTOMER");
        u1.setName("A");
        u1.setPhone("1111111111");

        User u2 = new User();
        u2.setEmail("b@example.com");
        u2.setPassword("pass");
        u2.setRole("CUSTOMER");
        u2.setName("B");
        u2.setPhone("2222222222");

        userRepository.save(u1);
        userRepository.save(u2);

        assertThat(userRepository.countByRole("CUSTOMER")).isEqualTo(2);
    }

    @Test
    void shouldFindByResetToken() {
        User user = new User();
        user.setEmail("reset@example.com");
        user.setPassword("pass");
        user.setRole("CUSTOMER");
        user.setName("Reset");
        user.setPhone("3333333333");
        user.setResetToken("token-abc");

        userRepository.save(user);

        Optional<User> found = userRepository.findByResetToken("token-abc");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("reset@example.com");
    }

    @Test
    void shouldCheckEmailExists() {
        User user = new User();
        user.setEmail("exists@example.com");
        user.setPassword("pass");
        user.setRole("CUSTOMER");
        user.setName("Exists");
        user.setPhone("4444444444");

        userRepository.save(user);

        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("missing@example.com")).isFalse();
    }
}
