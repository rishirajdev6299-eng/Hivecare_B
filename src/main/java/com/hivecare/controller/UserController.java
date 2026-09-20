
package com.hivecare.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hivecare.dto.LoginRequest;
import com.hivecare.dto.LoginResponse;
import com.hivecare.security.SecurityUtils;
import com.hivecare.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hivecare.services.BrevoEmailService;
import com.hivecare.model.User;
import com.hivecare.repository.UserRepository;
import com.hivecare.services.BrevoReset;
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

	private final UserRepository userRepository;
	private final BrevoReset brevoReset;
	private final BrevoEmailService brevoEmailService;
	private final PasswordEncoder passwordEncoder;

	private final JwtService jwtService;
	
	private final SecurityUtils securityUtils;


	public UserController(

	        UserRepository userRepository,

	        BrevoReset brevoReset,

	        PasswordEncoder passwordEncoder,

	        JwtService jwtService,
	        
	        SecurityUtils securityUtils,
	        BrevoEmailService brevoEmailService ) {

	    this.userRepository = userRepository;

	    this.brevoReset = brevoReset;

	    this.passwordEncoder = passwordEncoder;

	    this.jwtService = jwtService;
	    this.securityUtils=  securityUtils;
	    this.brevoEmailService=brevoEmailService;
	}


	@PostMapping("/register")
	public ResponseEntity<?> register(
	        @RequestBody User user) {

	    try {

	        // ==============================================
	        // NORMALIZE EMAIL
	        // ==============================================

	        String email = user.getEmail()
	                .trim()
	                .toLowerCase();

	        // ==============================================
	        // CHECK EXISTING EMAIL
	        // ==============================================

	        if (userRepository.existsByEmail(email)) {

	            return ResponseEntity
	                    .badRequest()
	                    .body("Email already exists");
	        }

	        // ==============================================
	        // SECURITY:
	        // PUBLIC USERS CAN NEVER SELECT ADMIN OR WORKER
	        // ==============================================

	        user.setRole("USER");

	        user.setEmail(email);

	        // ==============================================
	        // HASH PASSWORD
	        // ==============================================

	        user.setPassword(
	                passwordEncoder.encode(
	                        user.getPassword()
	                )
	        );

	        // ==============================================
	        // DEFAULT SECURITY VALUES
	        // ==============================================

	        user.setBlocked(false);

	        user.setLoginAttempts(0);

	        user.setLoginLockedUntil(null);

	        // ==============================================
	        // SAVE USER
	        // ==============================================

	        User savedUser =
	                userRepository.save(user);

	        // ==============================================
	        // SEND WELCOME EMAIL
	        // ==============================================

	        try {

	            brevoEmailService.sendWelcomeEmail(
	                    savedUser
	            );

	            System.out.println(
	                    "WELCOME EMAIL SENT TO: "
	                            + savedUser.getEmail()
	            );

	        } catch (Exception emailError) {

	            // Email failure should NOT cancel registration

	            System.out.println(
	                    "USER REGISTERED BUT WELCOME EMAIL FAILED: "
	                            + emailError.getMessage()
	            );
	        }

	        // ==============================================
	        // REGISTRATION SUCCESS
	        // ==============================================

	        return ResponseEntity.ok(
	                "Registration Successful"
	        );

	    } catch (Exception e) {

	        e.printStackTrace();

	        return ResponseEntity
	                .status(500)
	                .body(
	                        "Registration failed: "
	                                + e.getMessage()
	                );
	    }
	}
	@PostMapping("/login")
	public ResponseEntity<?> login(

	        @RequestBody LoginRequest request) {


	    String email =
	            request.getEmail()
	                    .trim()
	                    .toLowerCase();


	    User existingUser =
	            userRepository.findByEmail(email);


	    // =============================================
	    // USER DOES NOT EXIST
	    // =============================================

	    if (existingUser == null) {

	        return ResponseEntity
	                .badRequest()
	                .body("Invalid Email or Password");
	    }


	    // =============================================
	    // BLOCKED
	    // =============================================

	    if (existingUser.isBlocked()) {

	        return ResponseEntity
	                .status(403)
	                .body(
	                        "Your account has been blocked."
	                );
	    }


	    long currentTime =
	            System.currentTimeMillis();


	    Long lockedUntil =
	            existingUser.getLoginLockedUntil();


	    // =============================================
	    // LOGIN LOCK
	    // =============================================

	    if (

	            lockedUntil != null

	            &&

	            currentTime < lockedUntil

	    ) {

	        long remainingSeconds =

	                Math.max(

	                        1,

	                        (lockedUntil - currentTime)
	                                / 1000
	                );


	        return ResponseEntity

	                .status(429)

	                .body(

	                        "Too many incorrect password attempts. "
	                                + "Please try again after "
	                                + remainingSeconds
	                                + " seconds."
	                );
	    }


	    // =============================================
	    // PASSWORD CHECK
	    // =============================================

	    if (

	            existingUser.getPassword() == null

	            ||

	            !passwordEncoder.matches(

	                    request.getPassword(),

	                    existingUser.getPassword()
	            )

	    ) {


	        int attempts =

	                existingUser.getLoginAttempts() == null

	                        ? 0

	                        : existingUser.getLoginAttempts();


	        attempts++;


	        existingUser.setLoginAttempts(
	                attempts
	        );


	        if (attempts >= 3) {


	            existingUser.setLoginLockedUntil(

	                    System.currentTimeMillis()

	                            + (30 * 1000L)
	            );


	            existingUser.setLoginAttempts(0);


	            userRepository.save(
	                    existingUser
	            );


	            return ResponseEntity

	                    .status(429)

	                    .body(

	                            "Too many incorrect password attempts. "
	                                    + "Login temporarily locked for 30 seconds."
	                    );
	        }


	        userRepository.save(
	                existingUser
	        );


	        return ResponseEntity

	                .badRequest()

	                .body(
	                        "Invalid Email or Password"
	                );
	    }


	    // =============================================
	    // SUCCESS
	    // =============================================

	    existingUser.setLoginAttempts(0);

	    existingUser.setLoginLockedUntil(null);

	    userRepository.save(existingUser);


	    // =============================================
	    // CREATE JWT
	    // =============================================

	    String token =
	            jwtService.generateToken(
	                    existingUser
	            );


	    LoginResponse response =

	            new LoginResponse(

	                    token,

	                    existingUser.getId(),

	                    existingUser.getName(),

	                    existingUser.getEmail(),

	                    existingUser.getRole()
	            );


	    return ResponseEntity.ok(response);
	}
	@GetMapping("/me")
	public ResponseEntity<?> getCurrentUser() {
		User currentUser = securityUtils.getCurrentUser();
		if (currentUser == null) {
			return ResponseEntity.status(401).body("Not authenticated");
		}

		Map<String, Object> data = new java.util.HashMap<>();
		data.put("id", currentUser.getId());
		data.put("name", currentUser.getName());
		data.put("email", currentUser.getEmail());
		data.put("phone", currentUser.getPhone());
		data.put("address", currentUser.getAddress());
		data.put("role", currentUser.getRole());
		data.put("provider", currentUser.getProvider());
		data.put("workerService", currentUser.getWorkerService());
		data.put("available", currentUser.isAvailable());
		data.put("availabilitySet", currentUser.isAvailabilitySet());
		data.put("blocked", currentUser.isBlocked());
		data.put("profileImage", currentUser.getProfileImage());
		return ResponseEntity.ok(data);
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getUserById(
	        @PathVariable Long id) {

	    if (!securityUtils.isCurrentUserOrAdmin(id)) {

	        return ResponseEntity
	                .status(403)
	                .body("Access denied");
	    }


	    return userRepository

	            .findById(id)

	            .map(ResponseEntity::ok)

	            .orElse(
	                    ResponseEntity.notFound().build()
	            );
	}


 // =========================================================
 // UPDATE PROFILE IMAGE
 // =========================================================

 @PutMapping("/{id}/profile-image")
 public ResponseEntity<?> updateProfileImage(
         @PathVariable Long id,
         @RequestBody Map<String, String> request) {

     return userRepository.findById(id)
             .map(user -> {

                 String profileImage =
                         request.get("profileImage");

                 if (profileImage == null ||
                     profileImage.isBlank()) {

                     return ResponseEntity.badRequest()
                             .body("Profile image is required.");
                 }

                 user.setProfileImage(profileImage);

                 User savedUser =
                         userRepository.save(user);

                 return ResponseEntity.ok(savedUser);
             })
             .orElse(
                 ResponseEntity.notFound().build()
             );
 }
    // =========================================================
    // UPDATE USER
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User userDetails) {

        return userRepository.findById(id)
                .map(user -> {

                    user.setName(userDetails.getName());
                    user.setEmail(userDetails.getEmail());
                    user.setPhone(userDetails.getPhone());
                    user.setAddress(userDetails.getAddress());

                    /*
                     * Only update password if a password
                     * was actually supplied.
                     */
                    if (userDetails.getPassword() != null &&
                        !userDetails.getPassword().isBlank()) {

                        user.setPassword(
                                userDetails.getPassword()
                        );
                    }

                    return ResponseEntity.ok(
                            userRepository.save(user)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

 // =========================================================
 // FORGOT PASSWORD
 // =========================================================

 @PostMapping("/forgot-password")
 public ResponseEntity<?> forgotPassword(
         @RequestBody Map<String, String> request
 ) {

     String email = request.get("email");

     if (email == null || email.isBlank()) {

         return ResponseEntity.badRequest()
                 .body("Email is required.");
     }

     email = email.trim().toLowerCase();

     User user =
             userRepository.findByEmail(email);

     /*
      * Don't reveal whether an email exists.
      */

     if (user == null) {

         return ResponseEntity.ok(
                 "If an account exists with this email, a password reset link has been sent."
         );
     }

     // =====================================================
     // CREATE RANDOM RESET TOKEN
     // =====================================================

     String resetToken =
             UUID.randomUUID().toString();

     // =====================================================
     // TOKEN VALID FOR 30 MINUTES
     // =====================================================

     long expiry =
             System.currentTimeMillis()
                     + (30 * 60 * 1000);

     user.setResetToken(resetToken);

     user.setResetTokenExpiry(expiry);

     userRepository.save(user);

     // =====================================================
     // FRONTEND RESET URL
     // =====================================================

     String resetLink =
             "http://localhost:3000/reset-password?token="
                     + resetToken;

     try {

         brevoReset.sendPasswordResetEmail(
                 user.getEmail(),
                 user.getName(),
                 resetLink
         );

         System.out.println(
                 "========================================"
         );

         System.out.println(
                 "PASSWORD RESET EMAIL SENT"
         );

         System.out.println(
                 "Email: " + user.getEmail()
         );

         System.out.println(
                 "========================================"
         );

     } catch (Exception e) {

         e.printStackTrace();

         return ResponseEntity.status(500)
                 .body(
                         "Unable to send password reset email. Please try again later."
                 );
     }

     return ResponseEntity.ok(
             "If an account exists with this email, a password reset link has been sent."
     );
 }


 // =========================================================
 // RESET PASSWORD
 // =========================================================

 @PostMapping("/reset-password")
 public ResponseEntity<?> resetPassword(
         @RequestBody Map<String, String> request
 ) {

     String token =
             request.get("token");

     String newPassword =
             request.get("password");

     if (token == null || token.isBlank()) {

         return ResponseEntity.badRequest()
                 .body("Invalid reset token.");
     }

     if (newPassword == null ||
             newPassword.isBlank()) {

         return ResponseEntity.badRequest()
                 .body("Password is required.");
     }

     if (newPassword.length() < 6) {

         return ResponseEntity.badRequest()
                 .body(
                         "Password must contain at least 6 characters."
                 );
     }

     // =====================================================
     // FIND USER USING TOKEN
     // =====================================================

     User user =
             userRepository.findByResetToken(token)
                     .orElse(null);

     if (user == null) {

         return ResponseEntity.badRequest()
                 .body(
                         "Invalid or expired password reset link."
                 );
     }

     // =====================================================
     // CHECK EXPIRY
     // =====================================================

     if (user.getResetTokenExpiry() == null ||
             user.getResetTokenExpiry()
                     < System.currentTimeMillis()) {

         user.setResetToken(null);

         user.setResetTokenExpiry(null);

         userRepository.save(user);

         return ResponseEntity.badRequest()
                 .body(
                         "This password reset link has expired. Please request a new one."
                 );
     }

     // =====================================================
     // UPDATE PASSWORD
     // =====================================================

     user.setPassword(newPassword);

     // =====================================================
     // TOKEN CAN ONLY BE USED ONCE
     // =====================================================

     user.setResetToken(null);

     user.setResetTokenExpiry(null);

     userRepository.save(user);

     return ResponseEntity.ok(
             "Password reset successfully. You can now login."
     );
 }
    // =========================================================
    // ADMIN - BLOCK USER
    // =========================================================

    @PutMapping("/admin/{id}/block")
    public ResponseEntity<?> blockUser(
            @PathVariable Long id) {

        return userRepository.findById(id)
                .map(user -> {

                    // Prevent accidentally blocking another admin
                    if ("ADMIN".equalsIgnoreCase(user.getRole())) {

                        return ResponseEntity.badRequest()
                                .body("Administrator accounts cannot be blocked.");
                    }

                    user.setBlocked(true);

                    userRepository.save(user);

                    return ResponseEntity.ok(
                            "User blocked successfully"
                    );
                })
                .orElse(
                        ResponseEntity.notFound().build()
                );
    }


    // =========================================================
    // ADMIN - UNBLOCK USER
    // =========================================================

    @PutMapping("/admin/{id}/unblock")
    public ResponseEntity<?> unblockUser(
            @PathVariable Long id) {

        return userRepository.findById(id)
                .map(user -> {

                    user.setBlocked(false);

                    userRepository.save(user);

                    return ResponseEntity.ok(
                            "User unblocked successfully"
                    );
                })
                .orElse(
                        ResponseEntity.notFound().build()
                );
    }
}

