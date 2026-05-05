package com.grocery.localgrocery.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    void defaultConstructorShouldInitializeDefaults() {
        Notification notification = new Notification();
        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    void shouldSetAndGetFields() {
        Notification notification = new Notification();
        notification.setTitle("Flash Sale");
        notification.setMessage("50% off on all fruits today!");
        notification.setType("OFFER");
        notification.setTargetAudience("CUSTOMERS");
        notification.setStatus("SENT");

        LocalDateTime scheduled = LocalDateTime.now().plusDays(1);
        notification.setScheduledFor(scheduled);

        assertThat(notification.getTitle()).isEqualTo("Flash Sale");
        assertThat(notification.getMessage()).isEqualTo("50% off on all fruits today!");
        assertThat(notification.getType()).isEqualTo("OFFER");
        assertThat(notification.getTargetAudience()).isEqualTo("CUSTOMERS");
        assertThat(notification.getStatus()).isEqualTo("SENT");
        assertThat(notification.getScheduledFor()).isEqualTo(scheduled);
    }
}
