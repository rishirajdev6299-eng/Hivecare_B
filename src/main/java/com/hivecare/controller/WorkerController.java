package com.hivecare.controller;

import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hivecare.model.Booking;
import com.hivecare.model.User;
import com.hivecare.repository.BookingRepository;
import com.hivecare.repository.UserRepository;
import com.hivecare.services.BrevoEmailService;

@RestController
@RequestMapping("/api/workers")
@CrossOrigin(origins = "http://localhost:3000")
public class WorkerController {

	private final UserRepository userRepository;

	private final BookingRepository bookingRepository;

	private final BrevoEmailService brevoEmailService;

	private final PasswordEncoder passwordEncoder;

	public WorkerController(UserRepository userRepository, BookingRepository bookingRepository,
			BrevoEmailService brevoEmailService, PasswordEncoder passwordEncoder) {

		this.userRepository = userRepository;

		this.bookingRepository = bookingRepository;

		this.brevoEmailService = brevoEmailService;

		this.passwordEncoder = passwordEncoder;
	}

	// =====================================================
	// WORKER PROFILE
	// =====================================================

	@GetMapping("/{workerId}")
	public ResponseEntity<?> getWorker(@PathVariable Long workerId) {

		User worker = userRepository.findById(workerId).orElse(null);

		if (worker == null) {

			return ResponseEntity.notFound().build();
		}

		if (!"WORKER".equalsIgnoreCase(worker.getRole())) {

			return ResponseEntity.badRequest().body("User is not a worker.");
		}

		return ResponseEntity.ok(worker);
	}

	// =====================================================
	// SET WORKER ONLINE
	// =====================================================

	@PutMapping("/{workerId}/online")
	public ResponseEntity<?> setWorkerOnline(@PathVariable Long workerId) {

		User worker = userRepository.findById(workerId).orElse(null);

		if (worker == null) {

			return ResponseEntity.notFound().build();
		}

		if (!"WORKER".equalsIgnoreCase(worker.getRole())) {

			return ResponseEntity.badRequest().body("User is not a worker.");
		}

		if (worker.isBlocked()) {

			return ResponseEntity.badRequest().body("Blocked workers cannot go online.");
		}

		worker.setAvailable(true);

		userRepository.save(worker);

		return ResponseEntity.ok(Map.of("success", true, "available", true, "message", "Worker is now online."));
	}

	// =====================================================
	// SET WORKER OFFLINE
	// =====================================================

	@PutMapping("/{workerId}/offline")
	public ResponseEntity<?> setWorkerOffline(@PathVariable Long workerId) {

		User worker = userRepository.findById(workerId).orElse(null);

		if (worker == null) {

			return ResponseEntity.notFound().build();
		}

		if (!"WORKER".equalsIgnoreCase(worker.getRole())) {

			return ResponseEntity.badRequest().body("User is not a worker.");
		}

		worker.setAvailable(false);

		userRepository.save(worker);

		return ResponseEntity.ok(Map.of("success", true, "available", false, "message", "Worker is now offline."));
	}

	// =====================================================
	// UPDATE WORKER AVAILABILITY
	// =====================================================

	@PutMapping("/{id}/availability")
	public ResponseEntity<?> updateAvailability(@PathVariable Long id, @RequestParam boolean available) {

		try {

			User worker = userRepository.findById(id).orElse(null);

			if (worker == null) {

				return ResponseEntity.status(404).body("Worker not found");
			}

			if (!"WORKER".equalsIgnoreCase(worker.getRole())) {

				return ResponseEntity.status(400).body("User is not a worker");
			}

			if (worker.isBlocked() && available) {

				return ResponseEntity.status(403).body("Blocked worker cannot go online");
			}

			worker.setAvailable(available);

			worker.setAvailabilitySet(true);

			User savedWorker = userRepository.save(worker);

			return ResponseEntity.ok(savedWorker);

		} catch (Exception e) {

			e.printStackTrace();

			return ResponseEntity.status(500).body("Unable to update worker availability: " + e.getMessage());
		}
	}

	// =====================================================
	// ADMIN - GET ALL WORKERS
	// =====================================================

	@GetMapping("/admin/all")
	public ResponseEntity<List<User>> getAllWorkers() {

		return ResponseEntity.ok(userRepository.findByRole("WORKER"));
	}

	// =====================================================
	// ADMIN - CREATE WORKER
	// =====================================================

	@PostMapping("/admin/create")
	public ResponseEntity<?> createWorker(@RequestBody User worker) {

		if (worker.getEmail() == null || worker.getEmail().isBlank()) {

			return ResponseEntity.badRequest().body("Email is required");
		}

		if (userRepository.existsByEmail(worker.getEmail())) {

			return ResponseEntity.badRequest().body("Email already exists");
		}

		if (worker.getWorkerService() == null || worker.getWorkerService().isBlank()) {

			return ResponseEntity.badRequest().body("Worker service is required");
		}

		if (worker.getPassword() == null || worker.getPassword().isBlank()) {

			return ResponseEntity.badRequest().body("Password is required");
		}

		worker.setRole("WORKER");

		worker.setBlocked(false);

		worker.setRejectedBookings(0);

		// =====================================================
		// IMPORTANT:
		// HASH WORKER PASSWORD BEFORE SAVING TO MYSQL
		// =====================================================

		worker.setPassword(passwordEncoder.encode(worker.getPassword()));

		User savedWorker = userRepository.save(worker);

		// Never return the password to frontend
		savedWorker.setPassword(null);

		return ResponseEntity.ok(savedWorker);
	}
	// =====================================================
	// BOOKING REQUESTS
	// =====================================================

	@GetMapping("/{workerId}/requests")
	public ResponseEntity<List<Booking>> getRequests(@PathVariable Long workerId) {

		User worker = userRepository.findById(workerId).orElse(null);

		if (worker == null || !"WORKER".equalsIgnoreCase(worker.getRole())) {

			return ResponseEntity.notFound().build();
		}

		if (!worker.isAvailable()) {

			return ResponseEntity.ok(List.of());
		}

		List<Booking> requests = bookingRepository.findByServiceAndStatus(worker.getWorkerService(), "PENDING");

		return ResponseEntity.ok(requests);
	}

	// =====================================================
	// ACCEPT BOOKING
	// =====================================================

	@PutMapping("/{workerId}/bookings/{bookingId}/accept")
	public ResponseEntity<?> acceptBooking(@PathVariable Long workerId, @PathVariable Long bookingId) {

		User worker = userRepository.findById(workerId).orElse(null);

		if (worker == null || !"WORKER".equalsIgnoreCase(worker.getRole())) {

			return ResponseEntity.badRequest().body("Invalid worker");
		}

		Booking booking = bookingRepository.findById(bookingId).orElse(null);

		if (booking == null) {

			return ResponseEntity.notFound().build();
		}

		// =================================================
		// CHECK BOOKING STATUS
		// =================================================

		if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {

			return ResponseEntity.badRequest().body("Booking already assigned");
		}

		// =================================================
		// CHECK SERVICE
		// =================================================

		if (worker.getWorkerService() == null || booking.getService() == null
				|| !worker.getWorkerService().equalsIgnoreCase(booking.getService())) {

			return ResponseEntity.badRequest().body("This booking is not for your service");
		}

		// =================================================
		// ASSIGN WORKER
		// =================================================

		booking.setWorkerId(workerId);

		booking.setStatus("ACCEPTED");

		Booking savedBooking = bookingRepository.save(booking);

		// =================================================
		// SEND EMAIL TO CUSTOMER
		// =================================================

		try {

			// Make sure latest customer email exists

			if (savedBooking.getUserId() != null) {

				userRepository.findById(savedBooking.getUserId()).ifPresent(customer -> {

					savedBooking.setCustomerEmail(customer.getEmail());

					savedBooking.setCustomerPhone(customer.getPhone());
				});
			}

			brevoEmailService.sendWorkerAcceptedEmail(savedBooking, worker);

		} catch (Exception emailError) {

			System.out.println("Worker accepted, but email failed: " + emailError.getMessage());
		}

		return ResponseEntity.ok(savedBooking);
	}

	// =====================================================
	// REJECT BOOKING
	// =====================================================

	@PutMapping("/{workerId}/bookings/{bookingId}/reject")
	public ResponseEntity<?> rejectBooking(@PathVariable Long workerId, @PathVariable Long bookingId) {

		User worker = userRepository.findById(workerId).orElse(null);

		if (worker == null || !"WORKER".equalsIgnoreCase(worker.getRole())) {

			return ResponseEntity.badRequest().body("Invalid worker");
		}

		Booking booking = bookingRepository.findById(bookingId).orElse(null);

		if (booking == null) {

			return ResponseEntity.notFound().build();
		}

		if (worker.getWorkerService() == null || booking.getService() == null
				|| !worker.getWorkerService().equalsIgnoreCase(booking.getService())) {

			return ResponseEntity.badRequest().body("This booking is not for your service");
		}

		Integer currentRejected = worker.getRejectedBookings();

		if (currentRejected == null) {

			currentRejected = 0;
		}

		worker.setRejectedBookings(currentRejected + 1);

		userRepository.save(worker);

		return ResponseEntity.ok("Booking rejected");
	}

	// =====================================================
	// ADMIN SEARCH WORKERS
	// =====================================================

	@GetMapping("/admin/search")
	public ResponseEntity<List<User>> searchWorkers(@RequestParam(required = false) String search) {

		List<User> workers;

		if (search == null || search.isBlank()) {

			workers = userRepository.findByRole("WORKER");

		} else {

			String value = search.trim();

			workers = userRepository.findByRole("WORKER");

			workers = workers.stream().filter(worker -> {

				String id = worker.getId() != null ? worker.getId().toString() : "";

				String name = worker.getName() != null ? worker.getName() : "";

				String service = worker.getWorkerService() != null ? worker.getWorkerService() : "";

				return id.equalsIgnoreCase(value)

						|| name.toLowerCase().contains(value.toLowerCase())

						|| service.toLowerCase().contains(value.toLowerCase());
			}).toList();
		}

		return ResponseEntity.ok(workers);
	}

	// =====================================================
	// MY ASSIGNED BOOKINGS
	// =====================================================

	@GetMapping("/{workerId}/bookings")
	public List<Booking> getMyBookings(@PathVariable Long workerId) {

		return bookingRepository.findByWorkerId(workerId);
	}

	// =====================================================
	// COMPLETE SERVICE
	// =====================================================

	@PutMapping("/{workerId}/bookings/{bookingId}/complete")
	public ResponseEntity<?> completeService(@PathVariable Long workerId, @PathVariable Long bookingId) {

		User worker = userRepository.findById(workerId).orElse(null);

		if (worker == null || !"WORKER".equalsIgnoreCase(worker.getRole())) {

			return ResponseEntity.badRequest().body("Invalid worker");
		}

		Booking booking = bookingRepository.findById(bookingId).orElse(null);

		if (booking == null) {

			return ResponseEntity.notFound().build();
		}

		// =================================================
		// VERIFY WORKER ASSIGNMENT
		// =================================================

		if (booking.getWorkerId() == null || !booking.getWorkerId().equals(workerId)) {

			return ResponseEntity.badRequest().body("This booking is not assigned to this worker");
		}

		// =================================================
		// ONLY ACCEPTED CAN COMPLETE
		// =================================================

		if (!"ACCEPTED".equalsIgnoreCase(booking.getStatus())) {

			return ResponseEntity.badRequest().body("Only accepted bookings can be completed");
		}

		// =================================================
		// COMPLETE SERVICE
		// =================================================

		booking.setStatus("COMPLETED");

		booking.setCompletedAt(java.time.LocalDateTime.now().toString());

		Booking savedBooking = bookingRepository.save(booking);

		// =================================================
		// SEND COMPLETION EMAIL
		// =================================================

		try {

			if (savedBooking.getUserId() != null) {

				userRepository.findById(savedBooking.getUserId()).ifPresent(customer -> {

					savedBooking.setCustomerEmail(customer.getEmail());

					savedBooking.setCustomerPhone(customer.getPhone());
				});
			}

			brevoEmailService.sendServiceCompletedEmail(savedBooking, worker);

		} catch (Exception emailError) {

			System.out.println("Service completed, but email failed: " + emailError.getMessage());
		}

		return ResponseEntity.ok(savedBooking);
	}
}