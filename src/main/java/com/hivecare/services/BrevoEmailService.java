package com.hivecare.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hivecare.model.Booking;
import com.hivecare.model.User;
import com.hivecare.repository.UserRepository;


@Service
public class BrevoEmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final UserRepository userRepository;

    private final HttpClient httpClient;

    public BrevoEmailService(UserRepository userRepository) {

        this.userRepository = userRepository;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // =====================================================
    // COMMON EMAIL METHOD
    // =====================================================

    private boolean sendEmail(
            String customerEmail,
            String customerName,
            String subject,
            String htmlContent) {

        try {

            if (customerEmail == null ||
                    customerEmail.isBlank()) {

                System.out.println(
                        "BREVO EMAIL SKIPPED: Customer email is empty"
                );

                return false;
            }

            // =================================================
            // ESCAPE JSON VALUES
            // =================================================

            String safeEmail =
                    escapeJson(customerEmail);

            String safeName =
                    escapeJson(
                            customerName != null
                                    ? customerName
                                    : "Customer"
                    );

            String safeSenderEmail =
                    escapeJson(senderEmail);

            String safeSenderName =
                    escapeJson(senderName);

            String safeSubject =
                    escapeJson(subject);

            String safeHtml =
                    escapeJson(htmlContent);

            // =================================================
            // BREVO JSON
            // =================================================

            String json = """
                    {
                        "sender": {
                            "name": "%s",
                            "email": "%s"
                        },
                        "to": [
                            {
                                "email": "%s",
                                "name": "%s"
                            }
                        ],
                        "subject": "%s",
                        "htmlContent": "%s"
                    }
                    """.formatted(
                            safeSenderName,
                            safeSenderEmail,
                            safeEmail,
                            safeName,
                            safeSubject,
                            safeHtml
                    );

            // =================================================
            // HTTP REQUEST
            // =================================================

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            "https://api.brevo.com/v3/smtp/email"
                                    )
                            )
                            .timeout(
                                    Duration.ofSeconds(20)
                            )
                            .header(
                                    "accept",
                                    "application/json"
                            )
                            .header(
                                    "api-key",
                                    apiKey
                            )
                            .header(
                                    "content-type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(json)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            // =================================================
            // DEBUG RESPONSE
            // =================================================

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "BREVO EMAIL RESPONSE"
            );

            System.out.println(
                    "To: " + customerEmail
            );

            System.out.println(
                    "Subject: " + subject
            );

            System.out.println(
                    "HTTP Status: " +
                            response.statusCode()
            );

            System.out.println(
                    "Response: " +
                            response.body()
            );

            System.out.println(
                    "========================================"
            );

            if (response.statusCode() >= 200 &&
                    response.statusCode() < 300) {

                System.out.println(
                        "BREVO EMAIL SENT SUCCESSFULLY"
                );

                return true;
            }

            System.out.println(
                    "BREVO EMAIL FAILED"
            );

            return false;

        } catch (Exception e) {

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "BREVO EMAIL ERROR"
            );

            System.out.println(
                    e.getMessage()
            );

            System.out.println(
                    "========================================"
            );

            return false;
        }
    }

    // =====================================================
    // BOOKING CREATED EMAIL
    // =====================================================

    public void sendBookingCreatedEmail(
            Booking booking) {

        String customerEmail =
                booking.getCustomerEmail();

        String customerName =
                booking.getName();

        String subject =
                "Hivecare Booking Received - #" +
                        booking.getId();

        String html = """
                <html>

                <body style="margin:0;padding:0;background:#f4f8ff;font-family:Arial,sans-serif;">

                    <div style="max-width:650px;margin:30px auto;background:white;border-radius:14px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">

                        <div style="background:#1565c0;color:white;padding:25px;text-align:center;">

                            <h1 style="margin:0;">
                                Hivecare
                            </h1>

                            <p style="margin:8px 0 0;">
                                Booking Confirmation
                            </p>

                        </div>

                        <div style="padding:30px;">

                            <h2 style="color:#1565c0;">
                                Hello %s,
                            </h2>

                            <p>
                                Your Hivecare service booking has been
                                received successfully.
                            </p>

                            <div style="background:#f4f8ff;padding:20px;border-radius:10px;margin:20px 0;">

                                <p>
                                    <strong>Booking ID:</strong> #%s
                                </p>

                                <p>
                                    <strong>Service:</strong> %s
                                </p>

                                <p>
                                    <strong>Date:</strong> %s
                                </p>

                                <p>
                                    <strong>Time Slot:</strong> %s
                                </p>

                                <p>
                                    <strong>Address:</strong> %s
                                </p>

                                <p>
                                    <strong>Amount:</strong> ₹%s
                                </p>

                                <p>
                                    <strong>Status:</strong>

                                    <span style="color:#f39c12;">
                                        PENDING
                                    </span>
                                </p>

                            </div>

                            <p>
                                We will notify you by email when a worker
                                accepts your service request.
                            </p>

                            <p>
                                Thank you for choosing
                                <strong>Hivecare</strong>.
                            </p>

                        </div>

                        <div style="background:#f4f8ff;padding:18px;text-align:center;color:#777;font-size:13px;">

                            Hivecare Service Booking System

                        </div>

                    </div>

                </body>

                </html>
                """.formatted(

                        htmlEscape(customerName),

                        booking.getId(),

                        htmlEscape(booking.getService()),

                        htmlEscape(booking.getDate()),

                        htmlEscape(
                                booking.getTimeSlot() != null
                                        ? booking.getTimeSlot()
                                        : "Not specified"
                        ),

                        htmlEscape(booking.getAddress()),

                        booking.getAmount() != null
                                ? booking.getAmount()
                                : "0"
                );

        sendEmail(
                customerEmail,
                customerName,
                subject,
                html
        );
    }

    // =====================================================
    // WORKER ACCEPTED EMAIL
    // =====================================================

    public void sendWorkerAcceptedEmail(
            Booking booking,
            User worker) {

        String customerEmail =
                booking.getCustomerEmail();

        String customerName =
                booking.getName();

        String workerName =
                worker.getName();

        String subject =
                "Hivecare Worker Accepted Your Booking - #" +
                        booking.getId();

        String html = """
                <html>

                <body style="margin:0;padding:0;background:#f4f8ff;font-family:Arial,sans-serif;">

                    <div style="max-width:650px;margin:30px auto;background:white;border-radius:14px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">

                        <div style="background:#1565c0;color:white;padding:25px;text-align:center;">

                            <h1 style="margin:0;">
                                Hivecare
                            </h1>

                            <p style="margin:8px 0 0;">
                                Worker Assigned
                            </p>

                        </div>

                        <div style="padding:30px;">

                            <h2 style="color:#1565c0;">
                                Hello %s,
                            </h2>

                            <p>
                                Good news! A Hivecare worker has accepted
                                your service request.
                            </p>

                            <div style="background:#f4f8ff;padding:20px;border-radius:10px;margin:20px 0;">

                                <p>
                                    <strong>Booking ID:</strong> #%s
                                </p>

                                <p>
                                    <strong>Service:</strong> %s
                                </p>

                                <p>
                                    <strong>Worker:</strong> %s
                                </p>

                                <p>
                                    <strong>Worker Phone:</strong> %s
                                </p>

                                <p>
                                    <strong>Date:</strong> %s
                                </p>

                                <p>
                                    <strong>Time Slot:</strong> %s
                                </p>

                                <p>
                                    <strong>Status:</strong>

                                    <span style="color:#198754;">
                                        ACCEPTED
                                    </span>
                                </p>

                            </div>

                            <p>
                                Your assigned worker will provide the
                                requested service.
                            </p>

                            <p>
                                Thank you for choosing
                                <strong>Hivecare</strong>.
                            </p>

                        </div>

                        <div style="background:#f4f8ff;padding:18px;text-align:center;color:#777;font-size:13px;">

                            Hivecare Service Booking System

                        </div>

                    </div>

                </body>

                </html>
                """.formatted(

                        htmlEscape(customerName),

                        booking.getId(),

                        htmlEscape(booking.getService()),

                        htmlEscape(workerName),

                        htmlEscape(worker.getPhone()),

                        htmlEscape(booking.getDate()),

                        htmlEscape(
                                booking.getTimeSlot() != null
                                        ? booking.getTimeSlot()
                                        : "Not specified"
                        )
                );

        sendEmail(
                customerEmail,
                customerName,
                subject,
                html
        );
    }

    // =====================================================
    // SERVICE COMPLETED EMAIL
    // =====================================================

    public void sendServiceCompletedEmail(
            Booking booking,
            User worker) {

        String customerEmail =
                booking.getCustomerEmail();

        String customerName =
                booking.getName();

        String workerName =
                worker.getName();

        String subject =
                "Hivecare Service Completed - #" +
                        booking.getId();

        String html = """
                <html>

                <body style="margin:0;padding:0;background:#f4f8ff;font-family:Arial,sans-serif;">

                    <div style="max-width:650px;margin:30px auto;background:white;border-radius:14px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">

                        <div style="background:#1565c0;color:white;padding:25px;text-align:center;">

                            <h1 style="margin:0;">
                                Hivecare
                            </h1>

                            <p style="margin:8px 0 0;">
                                Service Completed
                            </p>

                        </div>

                        <div style="padding:30px;">

                            <h2 style="color:#1565c0;">
                                Hello %s,
                            </h2>

                            <p>
                                Your Hivecare service has been completed
                                successfully.
                            </p>

                            <div style="background:#f4f8ff;padding:20px;border-radius:10px;margin:20px 0;">

                                <p>
                                    <strong>Booking ID:</strong> #%s
                                </p>

                                <p>
                                    <strong>Service:</strong> %s
                                </p>

                                <p>
                                    <strong>Worker:</strong> %s
                                </p>

                                <p>
                                    <strong>Date:</strong> %s
                                </p>

                                <p>
                                    <strong>Time Slot:</strong> %s
                                </p>

                                <p>
                                    <strong>Completed At:</strong> %s
                                </p>

                                <p>
                                    <strong>Status:</strong>

                                    <span style="color:#198754;">
                                        COMPLETED
                                    </span>
                                </p>

                            </div>

                            <p>
                                Thank you for using
                                <strong>Hivecare</strong>.
                            </p>

                            <p>
                                We hope you had a great experience.
                            </p>

                        </div>

                        <div style="background:#f4f8ff;padding:18px;text-align:center;color:#777;font-size:13px;">

                            Hivecare Service Booking System

                        </div>

                    </div>

                </body>

                </html>
                """.formatted(

                        htmlEscape(customerName),

                        booking.getId(),

                        htmlEscape(booking.getService()),

                        htmlEscape(workerName),

                        htmlEscape(booking.getDate()),

                        htmlEscape(
                                booking.getTimeSlot() != null
                                        ? booking.getTimeSlot()
                                        : "Not specified"
                        ),

                        htmlEscape(booking.getCompletedAt())
                );

        sendEmail(
                customerEmail,
                customerName,
                subject,
                html
        );
    }

 // =====================================================
 // WELCOME EMAIL
 // =====================================================

 public void sendWelcomeEmail(User user) {

     if (user == null) {
         return;
     }

     String customerEmail = user.getEmail();
     String customerName = user.getName();

     String subject = "Welcome to HiveCare!";

     String html = """
             <html>

             <body style="margin:0;padding:0;background:#f4f8ff;font-family:Arial,sans-serif;">

                 <div style="max-width:650px;margin:30px auto;background:white;border-radius:14px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">

                     <div style="background:#1565c0;color:white;padding:30px;text-align:center;">

                         <h1 style="margin:0;">
                             HiveCare
                         </h1>

                         <p style="margin:10px 0 0;font-size:16px;">
                             Welcome to HiveCare!
                         </p>

                     </div>

                     <div style="padding:35px;">

                         <h2 style="color:#1565c0;">
                             Hello %s 👋
                         </h2>

                         <p style="font-size:16px;line-height:1.6;">
                             Welcome to <strong>HiveCare</strong>.
                             Your account has been created successfully.
                         </p>

                         <div style="background:#f4f8ff;padding:22px;border-radius:10px;margin:25px 0;">

                             <p style="margin:0 0 12px;">
                                 <strong>Name:</strong> %s
                             </p>

                             <p style="margin:0;">
                                 <strong>Email:</strong> %s
                             </p>

                         </div>

                         <p style="font-size:15px;line-height:1.6;">
                             You can now log in to HiveCare and book
                             trusted home services from verified professionals.
                         </p>

                         <div style="text-align:center;margin:30px 0;">

                             <a href="http://localhost:3000/login"
                                style="display:inline-block;background:#1565c0;color:white;text-decoration:none;padding:14px 28px;border-radius:8px;font-weight:bold;">

                                 Login to HiveCare

                             </a>

                         </div>

                         <p style="font-size:15px;">
                             Thank you for joining HiveCare.
                         </p>

                         <p style="font-size:15px;">
                             We look forward to serving you!
                         </p>

                         <p style="margin-top:25px;">
                             <strong>HiveCare Team</strong>
                         </p>

                     </div>

                     <div style="background:#f4f8ff;padding:18px;text-align:center;color:#777;font-size:13px;">

                         HiveCare Service Booking System

                     </div>

                 </div>

             </body>

             </html>
             """.formatted(
                     htmlEscape(customerName),
                     htmlEscape(customerName),
                     htmlEscape(customerEmail)
             );

     sendEmail(
             customerEmail,
             customerName,
             subject,
             html
     );
 }
    // =====================================================
    // JSON ESCAPE
    // =====================================================

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    // =====================================================
    // HTML ESCAPE
    // =====================================================

    private String htmlEscape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}