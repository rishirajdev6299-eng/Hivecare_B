package com.hivecare.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hivecare.model.Booking;
import com.hivecare.repository.BookingRepository;
import com.hivecare.repository.UserRepository;
import com.hivecare.services.BrevoEmailService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;

    private final UserRepository userRepository;

    private final BrevoEmailService brevoEmailService;

    public BookingController(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            BrevoEmailService brevoEmailService) {

        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.brevoEmailService = brevoEmailService;
    }

    // =====================================================
    // CREATE BOOKING
    // =====================================================

    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestBody Booking booking) {

        try {

            // =================================================
            // GET CUSTOMER DETAILS FROM USER TABLE
            // =================================================

            if (booking.getUserId() != null) {

                userRepository
                        .findById(booking.getUserId())
                        .ifPresent(customer -> {

                            booking.setCustomerEmail(
                                    customer.getEmail()
                            );

                            booking.setCustomerPhone(
                                    customer.getPhone()
                            );

                            System.out.println(
                                    "Customer Email: "
                                            + customer.getEmail()
                            );

                            System.out.println(
                                    "Customer Phone: "
                                            + customer.getPhone()
                            );
                        });
            }

            // =================================================
            // INITIAL BOOKING STATUS
            // =================================================

            booking.setStatus("PENDING");

            booking.setWorkerId(null);

            booking.setPaymentStatus("PENDING");

            booking.setRazorpayOrderId(null);

            booking.setRazorpayPaymentId(null);

            booking.setRazorpaySignature(null);

            booking.setPaidAt(null);

            booking.setCompletedAt(null);

            // =================================================
            // SAVE BOOKING
            // =================================================

            Booking savedBooking =
                    bookingRepository.save(booking);

            // =================================================
            // SEND BOOKING EMAIL
            // =================================================

            try {

                brevoEmailService
                        .sendBookingCreatedEmail(
                                savedBooking
                        );

            } catch (Exception emailError) {

                System.out.println(
                        "Booking created, but email failed: "
                                + emailError.getMessage()
                );
            }

            // =================================================
            // FIND ONLINE WORKERS
            // =================================================

            List<com.hivecare.model.User> availableWorkers =
                    userRepository
                            .findByRoleAndWorkerServiceAndAvailable(
                                    "WORKER",
                                    savedBooking.getService(),
                                    true
                            );

            // =================================================
            // DEBUG
            // =================================================

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "NEW BOOKING CREATED"
            );

            System.out.println(
                    "Booking ID: "
                            + savedBooking.getId()
            );

            System.out.println(
                    "Service: "
                            + savedBooking.getService()
            );

            System.out.println(
                    "Customer: "
                            + savedBooking.getName()
            );

            System.out.println(
                    "Customer Email: "
                            + savedBooking.getCustomerEmail()
            );

            System.out.println(
                    "Customer Phone: "
                            + savedBooking.getCustomerPhone()
            );

            System.out.println(
                    "Online matching workers: "
                            + availableWorkers.size()
            );

            for (com.hivecare.model.User worker :
                    availableWorkers) {

                System.out.println(
                        "Worker ID: "
                                + worker.getId()
                                + " | Name: "
                                + worker.getName()
                                + " | Service: "
                                + worker.getWorkerService()
                                + " | Phone: "
                                + worker.getPhone()
                );
            }

            System.out.println(
                    "========================================"
            );

            return ResponseEntity.ok(savedBooking);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            "Unable to create booking: "
                                    + e.getMessage()
                    );
        }
    }

    // =====================================================
    // GET ALL BOOKINGS
    // =====================================================

    @GetMapping
    public List<Booking> getAllBookings() {

        return bookingRepository.findAll();
    }

    // =====================================================
    // GET USER BOOKINGS
    // =====================================================

    @GetMapping("/user/{userId}")
    public List<Booking> getUserBookings(
            @PathVariable Long userId) {

        List<Booking> bookings =
                bookingRepository.findByUserId(userId);

        for (Booking booking : bookings) {

            // =================================================
            // GET WORKER NAME
            // =================================================

            if (booking.getWorkerId() != null) {

                userRepository
                        .findById(booking.getWorkerId())
                        .ifPresent(worker -> {

                            if ("WORKER".equalsIgnoreCase(
                                    worker.getRole())) {

                                booking.setWorkerName(
                                        worker.getName()
                                );
                            }
                        });
            }

            // =================================================
            // GET CUSTOMER EMAIL + PHONE
            // =================================================

            if (booking.getUserId() != null) {

                userRepository
                        .findById(booking.getUserId())
                        .ifPresent(customer -> {

                            booking.setCustomerEmail(
                                    customer.getEmail()
                            );

                            booking.setCustomerPhone(
                                    customer.getPhone()
                            );
                        });
            }
        }

        return bookings;
    }

 // =====================================================
 // UPDATE BOOKING
 // =====================================================

 @PutMapping("/{id}")
 public ResponseEntity<Booking> updateBooking(
         @PathVariable Long id,
         @RequestBody Booking bookingDetails) {

     return bookingRepository.findById(id)

             .map(existingBooking -> {

                 existingBooking.setService(
                         bookingDetails.getService()
                 );

                 existingBooking.setName(
                         bookingDetails.getName()
                 );

                 existingBooking.setAddress(
                         bookingDetails.getAddress()
                 );

                 existingBooking.setDate(
                         bookingDetails.getDate()
                 );

                 // =================================================
                 // UPDATE TIME SLOT
                 // =================================================

                 existingBooking.setTimeSlot(
                         bookingDetails.getTimeSlot()
                 );

                 existingBooking.setCourse(
                         bookingDetails.getCourse()
                 );

                 existingBooking.setPaymentMethod(
                         bookingDetails.getPaymentMethod()
                 );

                 existingBooking.setAmount(
                         bookingDetails.getAmount()
                 );

                 if (bookingDetails.getPaymentTiming() != null) {

                     existingBooking.setPaymentTiming(
                             bookingDetails.getPaymentTiming()
                     );
                 }

                 return ResponseEntity.ok(
                         bookingRepository.save(
                                 existingBooking
                         )
                 );
             })

             .orElse(
                     ResponseEntity.notFound().build()
             );
 }

    // =====================================================
    // CANCEL BOOKING
    // =====================================================

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long id) {

        try {

            Booking booking =
                    bookingRepository.findById(id)
                            .orElse(null);

            if (booking == null) {

                return ResponseEntity
                        .status(404)
                        .body(
                                "Booking not found with ID: "
                                        + id
                        );
            }

            booking.setStatus("CANCELLED");

            Booking savedBooking =
                    bookingRepository.save(booking);

            return ResponseEntity.ok(savedBooking);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            "Unable to cancel booking: "
                                    + e.getMessage()
                    );
        }
    }

    // =====================================================
    // ADMIN DELETE BOOKING
    // =====================================================

    @DeleteMapping("/admin/bookings/{id}")
    public ResponseEntity<?> deleteAdminBooking(
            @PathVariable Long id) {

        try {

            if (!bookingRepository.existsById(id)) {

                return ResponseEntity
                        .notFound()
                        .build();
            }

            bookingRepository.deleteById(id);

            return ResponseEntity.ok(
                    "Booking deleted successfully"
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            "Unable to delete booking: "
                                    + e.getMessage()
                    );
        }
    }
}