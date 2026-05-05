package com.grocery.localgrocery.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void shouldSetAndGetFields() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("secret");
        user.setRole("CUSTOMER");
        user.setName("Test User");
        user.setPhone("1234567890");
        user.setProfileImageUrl("/img/avatar.png");
        user.setVehicleType("Bike");
        user.setVehicleNumber("AB-1234");
        user.setIsAvailable(true);
        user.setCurrentLocation("Kathmandu");
        user.setRating(4.5);
        user.setTotalDeliveries(100);
        user.setResetToken("token123");
        user.setTotalEarnings(new BigDecimal("5000.00"));
        user.setIncentives(new BigDecimal("200.00"));
        user.setBonus(new BigDecimal("50.00"));
        user.setPerformanceBadge("GOLD");
        user.setOnlineStatus(true);

        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getRole()).isEqualTo("CUSTOMER");
        assertThat(user.getName()).isEqualTo("Test User");
        assertThat(user.getPhone()).isEqualTo("1234567890");
        assertThat(user.getProfileImageUrl()).isEqualTo("/img/avatar.png");
        assertThat(user.getVehicleType()).isEqualTo("Bike");
        assertThat(user.getVehicleNumber()).isEqualTo("AB-1234");
        assertThat(user.getIsAvailable()).isTrue();
        assertThat(user.getCurrentLocation()).isEqualTo("Kathmandu");
        assertThat(user.getRating()).isEqualTo(4.5);
        assertThat(user.getTotalDeliveries()).isEqualTo(100);
        assertThat(user.getResetToken()).isEqualTo("token123");
        assertThat(user.getTotalEarnings()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(user.getIncentives()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(user.getBonus()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(user.getPerformanceBadge()).isEqualTo("GOLD");
        assertThat(user.getOnlineStatus()).isTrue();
    }

    @Test
    void defaultConstructorShouldInitializeDefaults() {
        User user = new User();
        assertThat(user).isNotNull();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getIsAvailable()).isTrue();
        assertThat(user.getRating()).isEqualTo(5.0);
        assertThat(user.getTotalDeliveries()).isEqualTo(0);
    }
}
