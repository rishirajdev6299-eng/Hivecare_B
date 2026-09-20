package com.hivecare.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hivecare.dto.SubjectResponse;
import com.hivecare.model.Service;
import com.hivecare.model.Subject;
import com.hivecare.repository.ServiceRepository;
import com.hivecare.repository.SubjectRepository;

@RestController
@RequestMapping("/api/subjects")
@CrossOrigin(origins = "*")
public class SubjectController {

    private final SubjectRepository subjectRepository;
    private final ServiceRepository serviceRepository;

    public SubjectController(
            SubjectRepository subjectRepository,
            ServiceRepository serviceRepository) {

        this.subjectRepository = subjectRepository;
        this.serviceRepository = serviceRepository;
    }

    // =====================================================
    // GET ALL SUBJECTS
    // =====================================================

    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getAllSubjects() {

        List<SubjectResponse> subjects =
                subjectRepository.findAll()
                        .stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(subjects);
    }

    // =====================================================
    // GET SUBJECTS / OPTIONS BY SERVICE
    // =====================================================

    @GetMapping("/service/{serviceName}")
    public ResponseEntity<List<SubjectResponse>>
    getSubjectsByService(
            @PathVariable String serviceName) {

        List<SubjectResponse> subjects =
                subjectRepository
                        .findByService_NameIgnoreCase(serviceName)
                        .stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(subjects);
    }

    // =====================================================
    // TUTOR SUBJECTS
    // KEEPING THIS SO MYBOOKINGS.JS DOES NOT BREAK
    // =====================================================

    @GetMapping("/tutor")
    public ResponseEntity<List<SubjectResponse>>
    getTutorSubjects() {

        List<SubjectResponse> subjects =
                subjectRepository
                        .findByService_NameIgnoreCase("Tutor")
                        .stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(subjects);
    }

    // =====================================================
    // GET SUBJECT BY ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponse> getSubjectById(
            @PathVariable Long id) {

        return subjectRepository.findById(id)
                .map(subject ->
                        ResponseEntity.ok(
                                convertToResponse(subject)
                        )
                )
                .orElse(
                        ResponseEntity.notFound().build()
                );
    }

    // =====================================================
    // CREATE SUBJECT / SERVICE OPTION
    // =====================================================

    @PostMapping
    public ResponseEntity<?> createSubject(
            @RequestBody SubjectRequest request) {

        if (request.getName() == null ||
                request.getName().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Subject name is required.");
        }

        if (request.getPrice() == null ||
                request.getPrice() < 0) {

            return ResponseEntity.badRequest()
                    .body("Valid subject price is required.");
        }

        if (request.getServiceId() == null) {

            return ResponseEntity.badRequest()
                    .body("Service ID is required.");
        }

        Service service =
                serviceRepository.findById(
                        request.getServiceId()
                ).orElse(null);

        if (service == null) {

            return ResponseEntity.badRequest()
                    .body("Service not found.");
        }

        Subject subject = new Subject();

        subject.setName(
                request.getName().trim()
        );

        subject.setPrice(
                request.getPrice()
        );

        subject.setService(service);

        Subject saved =
                subjectRepository.save(subject);

        return ResponseEntity.ok(
                convertToResponse(saved)
        );
    }

    // =====================================================
    // UPDATE SUBJECT / SERVICE OPTION
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubject(
            @PathVariable Long id,
            @RequestBody SubjectRequest request) {

        return subjectRepository.findById(id)
                .map(subject -> {

                    if (request.getName() != null &&
                            !request.getName()
                                    .trim()
                                    .isEmpty()) {

                        subject.setName(
                                request.getName().trim()
                        );
                    }

                    if (request.getPrice() != null &&
                            request.getPrice() >= 0) {

                        subject.setPrice(
                                request.getPrice()
                        );
                    }

                    if (request.getServiceId() != null) {

                        Service service =
                                serviceRepository.findById(
                                        request.getServiceId()
                                ).orElse(null);

                        if (service == null) {

                            return ResponseEntity
                                    .badRequest()
                                    .body(
                                            "Service not found."
                                    );
                        }

                        subject.setService(service);
                    }

                    Subject saved =
                            subjectRepository.save(subject);

                    return ResponseEntity.ok(
                            convertToResponse(saved)
                    );
                })
                .orElse(
                        ResponseEntity.notFound().build()
                );
    }

    // =====================================================
    // DELETE SUBJECT / SERVICE OPTION
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubject(
            @PathVariable Long id) {

        if (!subjectRepository.existsById(id)) {

            return ResponseEntity.notFound().build();
        }

        subjectRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // RESPONSE CONVERTER
    // =====================================================

    private SubjectResponse convertToResponse(
            Subject subject) {

        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getPrice()
        );
    }

    // =====================================================
    // REQUEST DTO
    // =====================================================

    public static class SubjectRequest {

        private String name;
        private Double price;
        private Long serviceId;

        public SubjectRequest() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Long getServiceId() {
            return serviceId;
        }

        public void setServiceId(Long serviceId) {
            this.serviceId = serviceId;
        }
    }
}