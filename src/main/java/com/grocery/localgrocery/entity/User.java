package com.grocery.localgrocery.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // ADMIN, CUSTOMER, DELIVERY

    @Column(nullable = false)
    private String name;

    // New fields for all users
    @Column(nullable = false)
    private String phone;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    // Fields for delivery partners
    @Column(name = "vehicle_type")
    private String vehicleType;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "current_location")
    private String currentLocation;

    private Double rating = 5.0;

    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @Column(name = "total_earnings")
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "incentives")
    private BigDecimal incentives = BigDecimal.ZERO;

    @Column(name = "bonus")
    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(name = "performance_badge")
    private String performanceBadge; // "GOLD", "SILVER", "BRONZE"

    @Column(name = "online_status")
    private Boolean onlineStatus = false;

    @Column(name = "last_online_time")
    private LocalDateTime lastOnlineTime;

    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isAvailable = true;
        this.rating = 5.0;
        this.totalDeliveries = 0;
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getVehicleType() { return vehicleType; }
    public String getVehicleNumber() { return vehicleNumber; }
    public Boolean getIsAvailable() { return isAvailable; }
    public String getCurrentLocation() { return currentLocation; }
    public Double getRating() { return rating; }
    public Integer getTotalDeliveries() { return totalDeliveries; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getResetToken() { return resetToken; }
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public BigDecimal getIncentives() { return incentives; }
    public BigDecimal getBonus() { return bonus; }
    public String getPerformanceBadge() { return performanceBadge; }
    public Boolean getOnlineStatus() { return onlineStatus; }
    public LocalDateTime getLastOnlineTime() { return lastOnlineTime; }




    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }
    public void setRating(Double rating) { this.rating = rating; }
    public void setTotalDeliveries(Integer totalDeliveries) { this.totalDeliveries = totalDeliveries; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
    public void setLastOnlineTime(LocalDateTime lastOnlineTime) { this.lastOnlineTime = lastOnlineTime; }
    public void setTotalEarnings(BigDecimal totalEarnings) {this.totalEarnings = totalEarnings;}
    public void setIncentives(BigDecimal incentives) {this.incentives = incentives;}
    public void setBonus(BigDecimal bonus) {this.bonus = bonus;}
    public void setPerformanceBadge(String performanceBadge) {this.performanceBadge = performanceBadge;}
    public void setOnlineStatus(Boolean onlineStatus) {this.onlineStatus = onlineStatus;}
}