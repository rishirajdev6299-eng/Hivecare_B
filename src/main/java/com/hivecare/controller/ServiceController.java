
package com.hivecare.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hivecare.model.Service;
import com.hivecare.repository.ServiceRepository;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceRepository serviceRepository;

    public ServiceController(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    // =========================================================
    // GET ALL SERVICES
    // =========================================================

    @GetMapping
    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    // =========================================================
    // CREATE SERVICE
    // =========================================================

    @PostMapping
    public ResponseEntity<Service> createService(
            @RequestBody Service service) {

        if (service.getName() == null ||
                service.getName().trim().isEmpty()) {

            return ResponseEntity.badRequest().build();
        }

        service.setName(service.getName().trim());

        return ResponseEntity.ok(
                serviceRepository.save(service)
        );
    }

    // =========================================================
    // UPDATE SERVICE
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<Service> updateService(
            @PathVariable Long id,
            @RequestBody Service details) {

        return serviceRepository.findById(id)
                .map(service -> {

                    if (details.getName() != null &&
                            !details.getName().trim().isEmpty()) {

                        service.setName(
                                details.getName().trim()
                        );
                    }

                    service.setDescription(
                            details.getDescription()
                    );

                    service.setPrice(
                            details.getPrice()
                    );

                    return ResponseEntity.ok(
                            serviceRepository.save(service)
                    );
                })
                .orElse(
                        ResponseEntity.notFound().build()
                );
    }

    // =========================================================
    // DELETE SERVICE
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(
            @PathVariable Long id) {

        if (!serviceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        serviceRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}

