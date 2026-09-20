package com.hivecare.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hivecare.model.Booking;
import com.hivecare.repository.BookingRepository;
import com.hivecare.services.RazorpayService;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {

    private final BookingRepository bookingRepository;

    private final RazorpayService razorpayService;


    public PaymentController(
            BookingRepository bookingRepository,
            RazorpayService razorpayService) {

        this.bookingRepository =
                bookingRepository;

        this.razorpayService =
                razorpayService;
    }


    // ==========================================
    // CREATE ORDER
    // ==========================================

    @PostMapping("/create-order/{bookingId}")
    public ResponseEntity<?> createOrder(
            @PathVariable Long bookingId) {

        try {

            Booking booking =
                    bookingRepository
                            .findById(bookingId)
                            .orElse(null);

            if (booking == null) {

                return ResponseEntity
                        .badRequest()
                        .body("Booking not found");
            }


            if (booking.getAmount() == null ||
                booking.getAmount() <= 0) {

                return ResponseEntity
                        .badRequest()
                        .body("Invalid booking amount");
            }


            /*
             * If already paid, don't create
             * another order.
             */
            if ("PAID".equalsIgnoreCase(
                    booking.getPaymentStatus())) {

                return ResponseEntity
                        .badRequest()
                        .body("Booking is already paid");
            }


            /*
             * Create Razorpay order.
             */
            com.razorpay.Order order =
                    razorpayService.createOrder(
                            booking.getId(),
                            booking.getAmount()
                    );


            String orderId =
                    order.get("id");


            /*
             * Save Razorpay order ID.
             */
            booking.setRazorpayOrderId(
                    orderId
            );

            bookingRepository.save(booking);


            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "orderId",
                    orderId
            );

            response.put(
                    "amount",
                    order.get("amount")
            );

            response.put(
                    "currency",
                    order.get("currency")
            );

            response.put(
                    "keyId",
                    razorpayService.getKeyId()
            );

            response.put(
                    "bookingId",
                    booking.getId()
            );


            return ResponseEntity.ok(
                    response
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                        "Unable to create Razorpay order"
                    );
        }
    }


    // ==========================================
    // VERIFY PAYMENT
    // ==========================================

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody Map<String, String> data) {

        try {

            Long bookingId =
                    Long.parseLong(
                        data.get("bookingId")
                    );

            String paymentId =
                    data.get(
                        "razorpay_payment_id"
                    );

            String orderId =
                    data.get(
                        "razorpay_order_id"
                    );

            String signature =
                    data.get(
                        "razorpay_signature"
                    );


            Booking booking =
                    bookingRepository
                            .findById(bookingId)
                            .orElse(null);


            if (booking == null) {

                return ResponseEntity
                        .badRequest()
                        .body("Booking not found");
            }


            /*
             * IMPORTANT:
             *
             * Use the order ID saved in OUR
             * database for verification.
             */
            String databaseOrderId =
                    booking.getRazorpayOrderId();


            if (databaseOrderId == null ||
                !databaseOrderId.equals(orderId)) {

                return ResponseEntity
                        .badRequest()
                        .body(
                            "Invalid Razorpay order"
                        );
            }


            boolean verified =
                    razorpayService.verifyPayment(
                            databaseOrderId,
                            paymentId,
                            signature
                    );


            if (!verified) {

                return ResponseEntity
                        .badRequest()
                        .body(
                            "Payment verification failed"
                        );
            }


            /*
             * Payment successful.
             */
            booking.setPaymentStatus(
                    "PAID"
            );

            booking.setRazorpayPaymentId(
                    paymentId
            );

            booking.setRazorpaySignature(
                    signature
            );

            booking.setPaidAt(
                    LocalDateTime.now()
                            .toString()
            );


            bookingRepository.save(
                    booking
            );


            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "message",
                    "Payment successful"
            );

            response.put(
                    "bookingId",
                    booking.getId()
            );

            response.put(
                    "paymentStatus",
                    booking.getPaymentStatus()
            );

            response.put(
                    "paymentId",
                    paymentId
            );


            return ResponseEntity.ok(
                    response
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                        "Payment verification error"
                    );
        }
        
    }
    @GetMapping("/receipt/{bookingId}")
    public ResponseEntity<?> getReceipt(
            @PathVariable Long bookingId) {

        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElse(null);

        if (booking == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        if (!"PAID".equalsIgnoreCase(
                booking.getPaymentStatus())) {

            return ResponseEntity
                    .badRequest()
                    .body(
                        "Payment has not been completed"
                    );
        }


        Map<String, Object> receipt =
                new HashMap<>();


        receipt.put(
                "bookingId",
                booking.getId()
        );

        receipt.put(
                "customerName",
                booking.getName()
        );

        receipt.put(
                "service",
                booking.getService()
        );

        receipt.put(
                "course",
                booking.getCourse()
        );

        receipt.put(
                "address",
                booking.getAddress()
        );

        receipt.put(
                "amount",
                booking.getAmount()
        );

        receipt.put(
                "paymentTiming",
                booking.getPaymentTiming()
        );

        receipt.put(
                "paymentStatus",
                booking.getPaymentStatus()
        );

        receipt.put(
                "razorpayPaymentId",
                booking.getRazorpayPaymentId()
        );

        receipt.put(
                "razorpayOrderId",
                booking.getRazorpayOrderId()
        );

        receipt.put(
                "paidAt",
                booking.getPaidAt()
        );

        receipt.put(
                "completedAt",
                booking.getCompletedAt()
        );


        /*
         * Worker information
         */
        if (booking.getWorkerId() != null) {

            receipt.put(
                    "workerId",
                    booking.getWorkerId()
            );

        } else {

            receipt.put(
                    "workerId",
                    null
            );
        }


        return ResponseEntity.ok(
                receipt
        );
    }
}