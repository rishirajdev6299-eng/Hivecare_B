package com.hivecare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hivecare.model.Service;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}
