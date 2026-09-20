package com.hivecare.controller;

import com.hivecare.dto.ReviewResponse;
import com.hivecare.model.Booking;
import com.hivecare.model.Review;
import com.hivecare.model.User;
import com.hivecare.repository.BookingRepository;
import com.hivecare.repository.ReviewRepository;
import com.hivecare.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

	private final ReviewRepository reviewRepository;
	private final BookingRepository bookingRepository;
	private final UserRepository userRepository;

	public ReviewController(ReviewRepository reviewRepository, BookingRepository bookingRepository,
			UserRepository userRepository) {
		this.reviewRepository = reviewRepository;
		this.bookingRepository = bookingRepository;
		this.userRepository = userRepository;
	}

	// =========================================================
	// CREATE REVIEW
	// =========================================================

	@PostMapping
	public ResponseEntity<?> createReview(@RequestBody Review review) {

		try {

			// -------------------------------------------------
			// BASIC VALIDATION
			// -------------------------------------------------

			if (review.getBookingId() == null) {
				return ResponseEntity.badRequest().body("Booking ID is required.");
			}

			if (review.getUserId() == null) {
				return ResponseEntity.badRequest().body("User ID is required.");
			}

			if (review.getWorkerId() == null) {
				return ResponseEntity.badRequest().body("Worker ID is required.");
			}

			if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {

				return ResponseEntity.badRequest().body("Rating must be between 1 and 5.");
			}

			// -------------------------------------------------
			// FIND BOOKING
			// -------------------------------------------------

			Booking booking = bookingRepository.findById(review.getBookingId()).orElse(null);

			if (booking == null) {

				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found.");
			}

			// -------------------------------------------------
			// CHECK COMPLETED
			// -------------------------------------------------

			if (booking.getStatus() == null || !"COMPLETED".equalsIgnoreCase(booking.getStatus())) {

				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("You can review a service only after it is completed.");
			}

			// -------------------------------------------------
			// CHECK CUSTOMER
			// -------------------------------------------------

			if (booking.getUserId() == null || !booking.getUserId().equals(review.getUserId())) {

				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("You are not authorized to review this booking.");
			}

			// -------------------------------------------------
			// CHECK WORKER
			// -------------------------------------------------

			if (booking.getWorkerId() == null || !booking.getWorkerId().equals(review.getWorkerId())) {

				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("The selected worker does not belong to this booking.");
			}

			// -------------------------------------------------
			// CHECK CUSTOMER EXISTS
			// -------------------------------------------------

			User customer = userRepository.findById(review.getUserId()).orElse(null);

			if (customer == null) {

				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found.");
			}

			// -------------------------------------------------
			// CHECK WORKER EXISTS
			// -------------------------------------------------

			User worker = userRepository.findById(review.getWorkerId()).orElse(null);

			if (worker == null) {

				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Worker not found.");
			}

			// -------------------------------------------------
			// CHECK DUPLICATE
			// -------------------------------------------------

			if (reviewRepository.existsByBookingId(review.getBookingId())) {

				return ResponseEntity.status(HttpStatus.CONFLICT).body("This booking has already been reviewed.");
			}

			// -------------------------------------------------
			// SERVICE MUST COME FROM BOOKING
			// -------------------------------------------------

			review.setService(booking.getService());

			// -------------------------------------------------
			// SAVE
			// -------------------------------------------------

			Review saved = reviewRepository.save(review);

			return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));

		} catch (Exception e) {

			e.printStackTrace();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to save review.");
		}
	}

	// =========================================================
	// WORKER REVIEWS
	// =========================================================

	@GetMapping("/worker/{workerId}")
	public ResponseEntity<List<ReviewResponse>> getWorkerReviews(@PathVariable Long workerId) {

		List<ReviewResponse> reviews = reviewRepository.findByWorkerIdOrderByCreatedAtDesc(workerId).stream()
				.map(this::toResponse).collect(Collectors.toList());

		return ResponseEntity.ok(reviews);
	}

	// =========================================================
	// ALL REVIEWS - HOME PAGE
	// =========================================================

	@GetMapping
	public ResponseEntity<List<ReviewResponse>> getAllReviewsForHome() {

		List<ReviewResponse> reviews = reviewRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse)
				.collect(Collectors.toList());

		return ResponseEntity.ok(reviews);
	}
	// =========================================================
	// USER REVIEWS
	// =========================================================

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<ReviewResponse>> getUserReviews(@PathVariable Long userId) {

		List<ReviewResponse> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
				.map(this::toResponse).collect(Collectors.toList());

		return ResponseEntity.ok(reviews);
	}

	// =========================================================
	// ALL REVIEWS - ADMIN / HOME
	// =========================================================

	@GetMapping("/admin")
	public ResponseEntity<List<ReviewResponse>> getAllReviews() {

		List<ReviewResponse> reviews = reviewRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse)
				.collect(Collectors.toList());

		return ResponseEntity.ok(reviews);
	}

	// =========================================================
	// BOOKING REVIEW
	// =========================================================

	@GetMapping("/booking/{bookingId}")
	public ResponseEntity<?> getBookingReview(@PathVariable Long bookingId) {

		return reviewRepository.findByBookingId(bookingId).map(review -> ResponseEntity.ok(toResponse(review)))
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	// =========================================================
	// DELETE REVIEW
	// =========================================================

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteReview(@PathVariable Long id) {

		if (!reviewRepository.existsById(id)) {

			return ResponseEntity.notFound().build();
		}

		reviewRepository.deleteById(id);

		return ResponseEntity.ok("Review deleted successfully.");
	}

	// =========================================================
	// CONVERT REVIEW -> RESPONSE
	// =========================================================

	private ReviewResponse toResponse(Review review) {

		String customerName = "Customer";
		String workerName = "Worker";

		// -----------------------------------------------------
		// CUSTOMER NAME
		// -----------------------------------------------------

		if (review.getUserId() != null) {

			customerName = userRepository.findById(review.getUserId()).map(User::getName)
					.filter(name -> name != null && !name.trim().isEmpty()).orElse("Customer");
		}

		// -----------------------------------------------------
		// WORKER NAME
		// -----------------------------------------------------

		if (review.getWorkerId() != null) {

			workerName = userRepository.findById(review.getWorkerId()).map(User::getName)
					.filter(name -> name != null && !name.trim().isEmpty()).orElse("Worker");
		}

		return new ReviewResponse(review.getId(), review.getBookingId(), review.getUserId(), review.getWorkerId(),
				customerName, workerName, review.getService(), review.getRating(), review.getComment(),
				review.getCreatedAt());
	}
}