package com.hivecare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hivecare.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByWorkerId(Long workerId);

    List<Booking> findByServiceAndStatus(String service, String status);

    List<Booking> findByStatus(String status);
}