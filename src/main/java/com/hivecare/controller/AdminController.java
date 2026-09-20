package com.hivecare.controller;
import java.util.stream.Collectors;

import com.hivecare.dto.AdminBookingResponse;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.hivecare.model.Booking;
import com.hivecare.model.Service;
import com.hivecare.model.User;
import com.hivecare.repository.BookingRepository;
import com.hivecare.repository.ServiceRepository;
import com.hivecare.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;


    public AdminController(
            UserRepository userRepository,
            BookingRepository bookingRepository,
            ServiceRepository serviceRepository) {

        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.serviceRepository = serviceRepository;
    }


    // ALL USERS
    @GetMapping("/users")
    public List<User> getUsers() {

        return userRepository.findByRole("USER");
    }


    // ALL WORKERS
    @GetMapping("/workers")
    public List<User> getWorkers() {

        return userRepository.findByRole("WORKER");
    }


    // ALL BOOKINGS
    @GetMapping("/bookings")
    public List<AdminBookingResponse> getBookings() {

        List<Booking> bookings =
                bookingRepository.findAll();

        return bookings.stream()
                .map(booking -> {

                    AdminBookingResponse response =
                            new AdminBookingResponse();

                    response.setId(
                            booking.getId()
                    );

                    response.setService(
                            booking.getService()
                    );

                    response.setName(
                            booking.getName()
                    );

                    response.setAddress(
                            booking.getAddress()
                    );

                    response.setDate(
                            booking.getDate()
                    );

                    response.setCourse(
                            booking.getCourse()
                    );

                    response.setAmount(
                            booking.getAmount()
                    );

                    response.setUserId(
                            booking.getUserId()
                    );

                    response.setWorkerId(
                            booking.getWorkerId()
                    );


                    /*
                     * Get worker name.
                     */
                    if (booking.getWorkerId() != null) {

                        User worker =
                                userRepository
                                        .findById(
                                            booking.getWorkerId()
                                        )
                                        .orElse(null);

                        if (worker != null) {

                            response.setWorkerName(
                                    worker.getName()
                            );

                        } else {

                            response.setWorkerName(
                                    "Unknown Worker"
                            );
                        }

                    } else {

                        response.setWorkerName(
                                "Not Assigned"
                        );
                    }


                    response.setStatus(
                            booking.getStatus()
                    );

                    response.setPaymentTiming(
                            booking.getPaymentTiming()
                    );

                    response.setPaymentStatus(
                            booking.getPaymentStatus()
                    );

                    response.setRazorpayPaymentId(
                            booking.getRazorpayPaymentId()
                    );

                    response.setPaidAt(
                            booking.getPaidAt()
                    );

                    response.setCompletedAt(
                            booking.getCompletedAt()
                    );


                    return response;

                })
                .collect(
                    Collectors.toList()
                );
    }

 // =====================================================
 // DELETE BOOKING - ADMIN
 // Permanently deletes booking from database
 // =====================================================

 @DeleteMapping("/bookings/{id}")
 public String deleteBooking(@PathVariable Long id) {

     if (!bookingRepository.existsById(id)) {
         throw new RuntimeException(
             "Booking not found with ID: " + id
         );
     }

     bookingRepository.deleteById(id);

     return "Booking deleted successfully";
 }
    // ALL SERVICES
    @GetMapping("/services")
    public List<Service> getServices() {

        return serviceRepository.findAll();
    }

    

    // COUNTS FOR DASHBOARD
    @GetMapping("/stats")
    public AdminStats getStats() {

        long bookings =
                bookingRepository.count();

        long services =
                serviceRepository.count();

        long workers =
                userRepository.findByRole("WORKER")
                        .size();

        long users =
                userRepository.findByRole("USER")
                        .size();

        return new AdminStats(
                bookings,
                services,
                workers,
                users
        );
    }


    public static class AdminStats {

        private long bookings;
        private long services;
        private long workers;
        private long users;


        public AdminStats(
                long bookings,
                long services,
                long workers,
                long users) {

            this.bookings = bookings;
            this.services = services;
            this.workers = workers;
            this.users = users;
        }


        public long getBookings() {
            return bookings;
        }

        public long getServices() {
            return services;
        }

        public long getWorkers() {
            return workers;
        }

        public long getUsers() {
            return users;
        }
    }
    
}