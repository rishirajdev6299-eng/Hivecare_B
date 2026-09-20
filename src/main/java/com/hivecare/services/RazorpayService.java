package com.hivecare.services;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

@Service
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;


    // ==========================================
    // CREATE RAZORPAY ORDER
    // ==========================================

    public Order createOrder(
            Long bookingId,
            Double amount) throws Exception {

        RazorpayClient client =
                new RazorpayClient(
                        keyId,
                        keySecret
                );

        int amountInPaise =
                (int) Math.round(amount * 100);

        JSONObject options =
                new JSONObject();

        options.put(
                "amount",
                amountInPaise
        );

        options.put(
                "currency",
                "INR"
        );

        options.put(
                "receipt",
                "booking_" + bookingId
        );

        options.put(
                "payment_capture",
                1
        );

        return client.orders.create(options);
    }


    // ==========================================
    // VERIFY PAYMENT
    // ==========================================

    public boolean verifyPayment(
            String orderId,
            String paymentId,
            String signature) {

        try {

            JSONObject options =
                    new JSONObject();

            options.put(
                    "razorpay_order_id",
                    orderId
            );

            options.put(
                    "razorpay_payment_id",
                    paymentId
            );

            options.put(
                    "razorpay_signature",
                    signature
            );

            return Utils.verifyPaymentSignature(
                    options,
                    keySecret
            );

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }


    public String getKeyId() {
        return keyId;
    }
}