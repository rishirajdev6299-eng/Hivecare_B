package com.hivecare.repository;

import com.hivecare.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository
        extends JpaRepository<Subject, Long> {

    List<Subject> findByService_NameIgnoreCase(
            String serviceName
    );
}