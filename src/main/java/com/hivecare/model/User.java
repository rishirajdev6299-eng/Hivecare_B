
package com.hivecare.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String phone;

    private String address;

    private String role;

    /*
     * LOCAL    = Email/Password
     * GOOGLE   = Google Login
     * FACEBOOK = Facebook Login
     * APPLE    = Apple Login
     */
    
    @Column(nullable = true)
    private String provider;

    @Column(nullable = true)
    private String providerId;

    /*
     * Used only when role = WORKER
     */
    private String workerService;

    /*
     * false = user can login
     * true  = user cannot login
     */
    @Column(name = "available",nullable = false)
    private boolean available = false;
    
    @Column(nullable = false)
    private boolean availabilitySet = false;
    
    @Column(nullable = false)
    private boolean blocked = false;

    @Column(name = "rejected_bookings")
    private Integer rejectedBookings = 0;
    
    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private Long resetTokenExpiry;
    
    @Column(name = "login_attempts", nullable = false)
    private Integer loginAttempts = 0;

    @Column(name = "login_locked_until")
    private Long loginLockedUntil;
    
    @Column(name = "profile_image", columnDefinition = "MEDIUMTEXT")
    private String profileImage;

    // =====================================================
    // GETTERS AND SETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }


    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }


    public String getWorkerService() {
        return workerService;
    }

    public void setWorkerService(String workerService) {
        this.workerService = workerService;
    }


    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }


    public Integer getRejectedBookings() {
        return rejectedBookings;
    }

    public void setRejectedBookings(Integer rejectedBookings) {
        this.rejectedBookings = rejectedBookings;
    }
    
    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
    public boolean isAvailabilitySet() {
        return availabilitySet;
    }

    public void setAvailabilitySet(boolean availabilitySet) {
        this.availabilitySet = availabilitySet;
    }
    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public Long getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public void setResetTokenExpiry(Long resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }
    
    public Integer getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(Integer loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    public Long getLoginLockedUntil() {
        return loginLockedUntil;
    }

    public void setLoginLockedUntil(Long loginLockedUntil) {
        this.loginLockedUntil = loginLockedUntil;
    }
    
    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
