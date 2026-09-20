package com.hivecare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String service;

    private String name;

    private String address;

    private String date;

    // =====================================================
    // TIME SLOT
    // =====================================================

    private String timeSlot;

    private String course;

    /*
     * This field can still contain the old value if
     * you already have existing data.
     *
     * For Razorpay payment, the important field is
     * paymentTiming.
     */
    private String paymentMethod;

    private Double amount;

    private Long userId;

    private Long workerId;

    private String customerEmail;

    private String customerPhone;

    @Transient
    private String workerName;

    /*
     * Booking status:
     *
     * PENDING
     * ACCEPTED
     * COMPLETED
     */
    private String status;

    /*
     * Payment timing:
     *
     * PAY_NOW
     * PAY_AFTER_SERVICE
     */
    private String paymentTiming;

    /*
     * Payment status:
     *
     * PENDING
     * PAID
     */
    private String paymentStatus;

    /*
     * Razorpay order created by backend.
     */
    private String razorpayOrderId;

    /*
     * Razorpay payment ID returned after
     * successful payment.
     */
    private String razorpayPaymentId;

    /*
     * Razorpay signature used for verification.
     */
    private String razorpaySignature;

    /*
     * Payment completion date/time.
     */
    private String paidAt;

    /*
     * Service completion date/time.
     */
    private String completedAt;

    @Column
    private Double latitude;

    @Column
    private Double longitude;


    // =====================================================
    // ID
    // =====================================================

    public Long getId() {
        return id;
    }


    // =====================================================
    // SERVICE
    // =====================================================

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }


    // =====================================================
    // CUSTOMER NAME
    // =====================================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    // =====================================================
    // ADDRESS
    // =====================================================

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    // =====================================================
    // DATE
    // =====================================================

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    // =====================================================
    // TIME SLOT
    // =====================================================

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }


    // =====================================================
    // COURSE
    // =====================================================

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }


    // =====================================================
    // PAYMENT METHOD
    // =====================================================

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }


    // =====================================================
    // AMOUNT
    // =====================================================

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }


    // =====================================================
    // USER ID
    // =====================================================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }


    // =====================================================
    // CUSTOMER EMAIL
    // =====================================================

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }


    // =====================================================
    // CUSTOMER PHONE
    // =====================================================

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }


    // =====================================================
    // WORKER ID
    // =====================================================

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }


    // =====================================================
    // BOOKING STATUS
    // =====================================================

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    // =====================================================
    // PAYMENT TIMING
    // =====================================================

    public String getPaymentTiming() {
        return paymentTiming;
    }

    public void setPaymentTiming(String paymentTiming) {
        this.paymentTiming = paymentTiming;
    }


    // =====================================================
    // PAYMENT STATUS
    // =====================================================

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }


    // =====================================================
    // RAZORPAY ORDER ID
    // =====================================================

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }


    // =====================================================
    // RAZORPAY PAYMENT ID
    // =====================================================

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }


    // =====================================================
    // RAZORPAY SIGNATURE
    // =====================================================

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
    }


    // =====================================================
    // PAID AT
    // =====================================================

    public String getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(String paidAt) {
        this.paidAt = paidAt;
    }


    // =====================================================
    // COMPLETED AT
    // =====================================================

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }


    // =====================================================
    // WORKER NAME
    // =====================================================

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }


    // =====================================================
    // LATITUDE
    // =====================================================

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }


    // =====================================================
    // LONGITUDE
    // =====================================================

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}