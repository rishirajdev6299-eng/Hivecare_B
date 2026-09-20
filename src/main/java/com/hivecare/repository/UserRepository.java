package com.hivecare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hivecare.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    Optional<User> findOptionalByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(String role);

    List<User> findByRoleAndWorkerService(
            String role,
            String workerService
    );

    List<User> findByRoleAndNameContainingIgnoreCase(
            String role,
            String name
    );

    List<User> findByRoleAndWorkerServiceContainingIgnoreCase(
            String role,
            String workerService
    );

    Optional<User> findByProviderAndProviderId(
            String provider,
            String providerId
    );

    List<User> findByRoleAndAvailable(
            String role,
            boolean available
    );

    List<User> findByRoleAndWorkerServiceAndAvailable(
            String role,
            String workerService,
            boolean available
    );
    
    Optional<User> findByResetToken(String resetToken);

    // =====================================================
    // FORCE UPDATE WORKER AVAILABILITY
    // =====================================================

    @Modifying
    @Query("""
        UPDATE User u
        SET u.available = :available
        WHERE u.id = :id
        AND UPPER(u.role) = 'WORKER'
    """)
    int updateWorkerAvailability(
            @Param("id") Long id,
            @Param("available") boolean available
    );
}