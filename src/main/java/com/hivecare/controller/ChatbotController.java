package com.hivecare.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hivecare.model.Booking;
import com.hivecare.repository.BookingRepository;
import com.hivecare.repository.UserRepository;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {
"http://localhost:3000",
"https://hivecare.vercel.app"
})
public class ChatbotController {

@Value("${groq.api.key}")
private String groqApiKey;

private final BookingRepository bookingRepository;
private final UserRepository userRepository;

public ChatbotController(
        BookingRepository bookingRepository,
        UserRepository userRepository) {

    this.bookingRepository = bookingRepository;
    this.userRepository = userRepository;
}

// =====================================================
// CHAT
// =====================================================

@PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public ResponseEntity<?> chat(
        @RequestBody ChatRequest request) {

    try {

        // =================================================
        // VALIDATE MESSAGE
        // =================================================

        if (request == null
                || request.getMessage() == null
                || request.getMessage().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "reply",
                            "Please enter a message."
                    ));
        }

        // =================================================
        // VALIDATE GROQ KEY
        // =================================================

        if (groqApiKey == null
                || groqApiKey.trim().isEmpty()) {

            return ResponseEntity
                    .status(500)
                    .body(Map.of(
                            "reply",
                            "HiveCare AI support is not configured right now."
                    ));
        }

        String message =
                request.getMessage().trim();

        String userName =
                request.getUserName() == null
                        ? "Guest"
                        : request.getUserName().trim();

        String userEmail =
                request.getUserEmail() == null
                        ? ""
                        : request.getUserEmail().trim();

        String userRole =
                request.getUserRole() == null
                        ? "USER"
                        : request.getUserRole()
                                .trim()
                                .toUpperCase();

        String workerService =
                request.getWorkerService() == null
                        ? ""
                        : request.getWorkerService().trim();

        // =================================================
        // GET USER'S REAL BOOKINGS
        // =================================================

        List<Booking> bookings =
                getBookingsForUser(request);

        // =================================================
        // BUILD BOOKING CONTEXT
        // =================================================

        String bookingContext =
                buildBookingContext(
                        bookings,
                        userRole
                );

        // =================================================
        // ROLE-SPECIFIC INSTRUCTIONS
        // =================================================

        String roleInstructions;

        if ("WORKER".equalsIgnoreCase(userRole)) {

            roleInstructions = """

                    =====================================================
                    WORKER PERSPECTIVE
                    =====================================================

                    The current user is a HIVECARE WORKER.

                    Answer from the worker's perspective.

                    The booking data supplied below represents bookings
                    assigned to this worker.

                    The worker may ask about:
                    - assigned bookings
                    - customer details needed for the service
                    - service type
                    - booking date
                    - booking amount
                    - booking status
                    - payment status
                    - completed bookings
                    - pending bookings
                    - cancelled or rejected bookings
                    - their work-related booking information

                    Use only the supplied booking data for
                    booking-specific information.

                    If a worker has no booking data, say that no
                    assigned bookings were found.

                    Do not confuse the worker with the customer.

                    Worker service:
                    """ + escapeForPrompt(workerService) + """

                    """;

        } else {

            roleInstructions = """

                    =====================================================
                    CUSTOMER PERSPECTIVE
                    =====================================================

                    The current user is a HIVECARE CUSTOMER.

                    Answer from the customer's perspective.

                    The booking data supplied below represents this
                    customer's bookings.

                    The customer may ask about their:
                    - bookings
                    - booking status
                    - assigned worker
                    - service
                    - booking date
                    - amount
                    - payment
                    - booking history
                    - completed or pending bookings

                    Use only the supplied booking data for
                    booking-specific information.

                    If the customer has no booking data, say that no
                    bookings were found for their account.

                    """;
        }

        // =================================================
        // SYSTEM PROMPT
        // =================================================

        String systemPrompt = """

                You are HiveCare AI Support Assistant.

                HiveCare is a home-service booking platform.

                Your job is to provide helpful support about HiveCare
                services, bookings, workers, payments and application
                usage.

                =====================================================
                OFFICIAL HIVCARE SUPPORT CONTACT
                =====================================================

                These are the ONLY official HiveCare support contact
                details:

                Official HiveCare Support Email:
                hivecare2@gmail.com

                Official HiveCare Support Phone:
                9852056217

                IMPORTANT:

                If the user asks for:
                - HiveCare support email
                - HiveCare support phone number
                - customer support contact
                - helpdesk contact
                - contact HiveCare
                - how to reach HiveCare
                - customer care
                - support details
                - official HiveCare contact information

                ALWAYS provide:

                Email: hivecare2@gmail.com
                Phone: 9852056217

                NEVER provide a different HiveCare support email
                address or phone number.

                NEVER invent, guess, or substitute HiveCare contact
                information.

                In particular, NEVER use old, incorrect or example
                contact details such as:

                supportiveivecare@gmail.com
                01234567990

                Those are NOT official HiveCare support details.

                =====================================================
                IMPORTANT DATA RULE
                =====================================================

                Real booking information is provided below.

                For booking-specific questions, the supplied booking
                data is the SOURCE OF TRUTH.

                Never invent or guess booking information.

                Never make up:
                - booking ID
                - service
                - customer
                - worker
                - date
                - amount
                - booking status
                - payment status
                - payment method
                - payment timing

                If information is not present in the booking data,
                clearly say that it is not available.

                Understand natural language. Users do not need to ask
                questions using a specific format.

                =====================================================
                BOOKING INFORMATION
                =====================================================

                Booking statuses can include:

                PENDING
                ACCEPTED
                COMPLETED
                REJECTED
                CANCELLED

                Payment statuses can include:

                PENDING
                PAID

                Payment timing can include:

                PAY_NOW
                PAY_AFTER_SERVICE

                =====================================================
                PAYMENT RULE
                =====================================================

                Never say that a payment has been completed unless
                paymentStatus is PAID.

                Never expose:
                - Razorpay payment IDs
                - Razorpay signatures
                - API keys
                - passwords
                - internal credentials

                =====================================================
                HIVCARE SERVICES
                =====================================================

                HiveCare provides services such as:

                Home Cleaning
                Home Appliances
                Appliance Repair
                Beautician
                Tutor
                Electrician
                Plumber
                Yoga Instructor
                Pet Sitter
                Carpenter
                Baby Sitter
                Gym Trainer
                Maid

                =====================================================
                RESPONSE STYLE
                =====================================================

                Be friendly, natural and concise.

                Answer the actual question directly.

                Do not unnecessarily list every booking field.

                When multiple bookings are relevant, distinguish them
                clearly.

                Never claim that you performed an action when you only
                provided information.

                =====================================================
                CURRENT USER
                =====================================================

                Name:
                """ + escapeForPrompt(userName) + """

                Email:
                """ + escapeForPrompt(userEmail) + """

                Role:
                """ + escapeForPrompt(userRole) + """

                """ + roleInstructions + """

                =====================================================
                REAL BOOKING DATA
                =====================================================

                """ + bookingContext + """

                =====================================================
                FINAL RULE
                =====================================================

                Answer the user's question naturally.

                For booking questions, use the REAL BOOKING DATA above.

                Never fabricate missing information.

                If the user asks for HiveCare support contact details,
                ALWAYS use exactly:

                Email: hivecare2@gmail.com
                Phone: 9852056217

                =====================================================
                USER QUESTION
                =====================================================

                """ + message;

        // =================================================
        // GROQ REQUEST
        // =================================================

        String jsonBody =
                "{"
                + "\"model\":\"openai/gpt-oss-20b\","
                + "\"messages\":["
                + "{"
                + "\"role\":\"user\","
                + "\"content\":\""
                + escapeJson(systemPrompt)
                + "\""
                + "}"
                + "],"
                + "\"temperature\":0.2,"
                + "\"max_tokens\":1000"
                + "}";

        HttpClient client =
                HttpClient.newBuilder()
                        .connectTimeout(
                                Duration.ofSeconds(30)
                        )
                        .build();

        HttpRequest httpRequest =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "https://api.groq.com/openai/v1/chat/completions"
                                )
                        )
                        .timeout(
                                Duration.ofSeconds(60)
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .header(
                                "Authorization",
                                "Bearer " + groqApiKey
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(jsonBody)
                        )
                        .build();

        HttpResponse<String> response =
                client.send(
                        httpRequest,
                        HttpResponse.BodyHandlers.ofString()
                );

        // =================================================
        // GROQ ERROR
        // =================================================

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            System.out.println(
                    "Groq API error. Status: "
                            + response.statusCode()
            );

            System.out.println(
                    "Groq response: "
                            + response.body()
            );

            return ResponseEntity
                    .status(500)
                    .body(Map.of(
                            "reply",
                            "Sorry 😔 I'm having trouble connecting to HiveCare AI Support right now. Please try again in a moment."
                    ));
        }

        // =================================================
        // EXTRACT RESPONSE
        // =================================================

        String reply =
                extractReply(response.body());

        if (reply == null
                || reply.trim().isEmpty()) {

            reply =
                    "Sorry, I couldn't generate a response right now.";
        }

        return ResponseEntity.ok(
                Map.of(
                        "reply",
                        reply
                )
        );

    } catch (Exception e) {

        e.printStackTrace();

        return ResponseEntity
                .status(500)
                .body(Map.of(
                        "reply",
                        "Sorry 😔 I'm having trouble connecting to HiveCare Support right now. Please try again in a moment."
                ));
    }
}

// =====================================================
// GET BOOKINGS FOR USER / WORKER
// =====================================================

private List<Booking> getBookingsForUser(
        ChatRequest request) {

    List<Booking> bookings =
            new ArrayList<>();

    if (request.getUserId() == null) {
        return bookings;
    }

    try {

        if ("WORKER".equalsIgnoreCase(
                request.getUserRole())) {

            // Worker gets only their assigned bookings
            bookings =
                    bookingRepository.findByWorkerId(
                            request.getUserId()
                    );

        } else {

            // Customer gets only their own bookings
            bookings =
                    bookingRepository.findByUserId(
                            request.getUserId()
                    );
        }

        // =================================================
        // GET WORKER NAME
        // =================================================

        for (Booking booking : bookings) {

            if (booking.getWorkerId() != null) {

                userRepository
                        .findById(
                                booking.getWorkerId()
                        )
                        .ifPresent(worker -> {

                            if ("WORKER".equalsIgnoreCase(
                                    worker.getRole())) {

                                booking.setWorkerName(
                                        worker.getName()
                                );
                            }
                        });
            }
        }

    } catch (Exception e) {

        System.out.println(
                "Unable to load chatbot booking data: "
                        + e.getMessage()
        );
    }

    return bookings;
}

// =====================================================
// BUILD BOOKING CONTEXT
// =====================================================

private String buildBookingContext(
        List<Booking> bookings,
        String userRole) {

    if (bookings == null
            || bookings.isEmpty()) {

        if ("WORKER".equalsIgnoreCase(userRole)) {

            return "NO ASSIGNED BOOKINGS FOUND FOR THIS WORKER.";

        } else {

            return "NO BOOKINGS FOUND FOR THIS CUSTOMER.";
        }
    }

    StringBuilder context =
            new StringBuilder();

    context.append(
            "These are the real booking records available "
                    + "for the current user:\n\n"
    );

    for (Booking booking : bookings) {

        context.append(
                "----------------------------------------\n"
        );

        context.append(
                "Booking ID: "
        ).append(
                safeValue(booking.getId())
        ).append("\n");

        context.append(
                "Service: "
        ).append(
                safeValue(booking.getService())
        ).append("\n");

        context.append(
                "Customer Name: "
        ).append(
                safeValue(booking.getName())
        ).append("\n");

        context.append(
                "Date: "
        ).append(
                safeValue(booking.getDate())
        ).append("\n");

        context.append(
                "Course/Option: "
        ).append(
                safeValue(booking.getCourse())
        ).append("\n");

        context.append(
                "Status: "
        ).append(
                safeValue(booking.getStatus())
        ).append("\n");

        context.append(
                "Worker: "
        ).append(
                safeValue(booking.getWorkerName())
        ).append("\n");

        context.append(
                "Amount: "
        ).append(
                safeValue(booking.getAmount())
        ).append("\n");

        context.append(
                "Payment Method: "
        ).append(
                safeValue(booking.getPaymentMethod())
        ).append("\n");

        context.append(
                "Payment Timing: "
        ).append(
                safeValue(booking.getPaymentTiming())
        ).append("\n");

        context.append(
                "Payment Status: "
        ).append(
                safeValue(booking.getPaymentStatus())
        ).append("\n");

        context.append(
                "Completed At: "
        ).append(
                safeValue(booking.getCompletedAt())
        ).append("\n");

        context.append(
                "Paid At: "
        ).append(
                safeValue(booking.getPaidAt())
        ).append("\n");
    }

    context.append(
            "----------------------------------------\n"
    );

    return context.toString();
}

// =====================================================
// SAFE VALUE
// =====================================================

private String safeValue(
        Object value) {

    if (value == null) {
        return "Not available";
    }

    String text =
            String.valueOf(value);

    if (text.trim().isEmpty()) {
        return "Not available";
    }

    return text;
}

// =====================================================
// ESCAPE JSON
// =====================================================

private String escapeJson(
        String value) {

    if (value == null) {
        return "";
    }

    return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t");
}

// =====================================================
// ESCAPE PROMPT
// =====================================================

private String escapeForPrompt(
        String value) {

    if (value == null) {
        return "";
    }

    return value
            .replace("\r", " ")
            .replace("\n", " ")
            .trim();
}

// =====================================================
// EXTRACT GROQ RESPONSE
// =====================================================

private String extractReply(
        String json) {

    if (json == null
            || json.trim().isEmpty()) {

        return "";
    }

    try {

        String marker =
                "\"content\":\"";

        int start =
                json.indexOf(marker);

        if (start == -1) {
            return "";
        }

        start += marker.length();

        StringBuilder result =
                new StringBuilder();

        boolean escaped = false;

        for (int i = start;
                i < json.length();
                i++) {

            char c =
                    json.charAt(i);

            if (escaped) {

                switch (c) {

                    case 'n':
                        result.append('\n');
                        break;

                    case 'r':
                        result.append('\r');
                        break;

                    case 't':
                        result.append('\t');
                        break;

                    case '"':
                        result.append('"');
                        break;

                    case '\\':
                        result.append('\\');
                        break;

                    default:
                        result.append(c);
                }

                escaped = false;

            } else if (c == '\\') {

                escaped = true;

            } else if (c == '"') {

                break;

            } else {

                result.append(c);
            }
        }

        return result.toString();

    } catch (Exception e) {

        e.printStackTrace();

        return "";
    }
}

// =====================================================
// REQUEST DTO
// =====================================================

public static class ChatRequest {

    private String message;

    private String userName;

    private String userEmail;

    private String userRole;

    private String workerService;

    private Long userId;

    public String getMessage() {
        return message;
    }

    public void setMessage(
            String message) {

        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(
            String userName) {

        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(
            String userEmail) {

        this.userEmail = userEmail;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(
            String userRole) {

        this.userRole = userRole;
    }

    public String getWorkerService() {
        return workerService;
    }

    public void setWorkerService(
            String workerService) {

        this.workerService = workerService;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(
            Long userId) {

        this.userId = userId;
    }
}


}
