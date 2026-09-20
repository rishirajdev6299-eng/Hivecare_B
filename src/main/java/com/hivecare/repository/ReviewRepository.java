package com.hivecare.repository;

import com.hivecare.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByWorkerIdOrderByCreatedAtDesc(Long workerId);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Review> findAllByOrderByCreatedAtDesc();

    Optional<Review> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);
}