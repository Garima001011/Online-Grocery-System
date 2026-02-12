package com.grocery.localgrocery.entity;

import jakarta.persistence.*;
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
}