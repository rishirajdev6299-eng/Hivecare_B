package com.hivecare.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BrevoReset {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final RestClient restClient;

    public BrevoReset(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://api.brevo.com")
                .build();
    }

    // =====================================================
    // SEND FORGOT PASSWORD EMAIL
    // =====================================================

    public void sendPasswordResetEmail(
            String customerEmail,
            String customerName,
            String resetLink
    ) {

        Map<String, Object> sender = new HashMap<>();

        sender.put("name", senderName);
        sender.put("email", senderEmail);


        Map<String, Object> recipient = new HashMap<>();

        recipient.put(
                "email",
                customerEmail
        );

        recipient.put(
                "name",
                customerName != null
                        ? customerName
                        : "HiveCare User"
        );


        Map<String, Object> body = new HashMap<>();

        body.put(
                "sender",
                sender
        );

        body.put(
                "to",
                new Object[]{recipient}
        );

        body.put(
                "subject",
                "Reset Your HiveCare Password"
        );


        String htmlContent = """

            <!DOCTYPE html>

            <html>

            <head>

                <meta charset="UTF-8">

                <meta name="viewport"
                      content="width=device-width,
                      initial-scale=1.0">

                <title>Reset Password</title>

            </head>

            <body style="
                margin:0;
                padding:0;
                background:#f4f7fb;
                font-family:Arial,sans-serif;
            ">

                <div style="
                    max-width:600px;
                    margin:40px auto;
                    background:#ffffff;
                    border-radius:18px;
                    overflow:hidden;
                    box-shadow:0 10px 30px rgba(0,0,0,0.08);
                ">

                    <div style="
                        background:linear-gradient(
                            135deg,
                            #2563eb,
                            #4f46e5
                        );
                        padding:35px;
                        text-align:center;
                        color:white;
                    ">

                        <div style="
                            width:60px;
                            height:60px;
                            margin:auto;
                            border-radius:16px;
                            background:white;
                            color:#2563eb;
                            display:flex;
                            align-items:center;
                            justify-content:center;
                            font-size:30px;
                            font-weight:bold;
                        ">
                            H
                        </div>

                        <h1 style="
                            margin:20px 0 5px;
                        ">
                            HiveCare
                        </h1>

                        <p style="
                            margin:0;
                            opacity:.9;
                        ">
                            Password Reset Request
                        </p>

                    </div>


                    <div style="
                        padding:35px;
                        color:#1f2937;
                    ">

                        <h2>
                            Hello %s 👋
                        </h2>

                        <p style="
                            font-size:16px;
                            line-height:1.6;
                            color:#4b5563;
                        ">

                            We received a request to reset
                            your HiveCare account password.

                        </p>

                        <p style="
                            font-size:16px;
                            line-height:1.6;
                            color:#4b5563;
                        ">

                            Click the button below to create
                            a new password.

                        </p>


                        <div style="
                            text-align:center;
                            margin:35px 0;
                        ">

                            <a href="%s"
                               style="
                               display:inline-block;
                               padding:15px 30px;
                               background:#2563eb;
                               color:white;
                               text-decoration:none;
                               border-radius:10px;
                               font-weight:bold;
                               font-size:16px;
                               ">

                                Reset My Password

                            </a>

                        </div>


                        <p style="
                            font-size:14px;
                            color:#6b7280;
                            line-height:1.6;
                        ">

                            This password reset link will expire
                            in 30 minutes.

                        </p>


                        <p style="
                            font-size:14px;
                            color:#6b7280;
                            line-height:1.6;
                        ">

                            If you did not request a password
                            reset, you can safely ignore this email.

                        </p>

                    </div>


                    <div style="
                        padding:20px;
                        background:#f8fafc;
                        text-align:center;
                        color:#94a3b8;
                        font-size:13px;
                    ">

                        © HiveCare — Professional Home Services

                    </div>

                </div>

            </body>

            </html>

            """.formatted(
                    customerName != null
                            ? customerName
                            : "HiveCare User",
                    resetLink
            );


        body.put(
                "htmlContent",
                htmlContent
        );


        restClient.post()
                .uri("/v3/smtp/email")
                .header(
                        "api-key",
                        brevoApiKey
                )
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}