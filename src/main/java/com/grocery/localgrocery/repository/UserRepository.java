package com.grocery.localgrocery.repository;

import com.grocery.localgrocery.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email
    Optional<User> findByEmail(String email);

    // Find all users by role
    List<User> findByRole(String role);

    // Count users by role
    long countByRole(String role);

    Optional<User> findByResetToken(String resetToken);

    // Find active delivery persons
    @Query("SELECT u FROM User u WHERE u.role = 'DELIVERY' AND u.isAvailable = true")
    List<User> findAvailableDeliveryPersons();

    // Find all delivery persons with their stats
    @Query("SELECT u, COUNT(o) as deliveryCount FROM User u LEFT JOIN Order o ON u.id = o.deliveryPerson.id WHERE u.role = 'DELIVERY' GROUP BY u.id")
    List<Object[]> findDeliveryPersonsWithStats();

    // Search users by name or email
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<User> searchUsers(String searchTerm);

    // Find users by multiple roles
    @Query("SELECT u FROM User u WHERE u.role IN ?1")
    List<User> findByRoles(List<String> roles);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find delivery persons by availability
    @Query("SELECT u FROM User u WHERE u.role = 'DELIVERY' AND u.isAvailable = ?1")
    List<User> findDeliveryByAvailability(boolean isAvailable);
}