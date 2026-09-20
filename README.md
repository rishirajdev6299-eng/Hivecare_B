# HiveCare Backend

HiveCare Backend is a secure RESTful API built with **Spring Boot** that powers the HiveCare home-service booking platform.

It provides backend services for customer authentication, role-based access, service management, worker management, booking operations, payments, reviews, profile management, reporting, and AI-assisted customer support.

The backend uses **Spring Boot, Spring Data JPA, MySQL, JWT authentication, OAuth, and Razorpay integration** to provide a complete backend infrastructure for the HiveCare platform.

## Features

### Authentication & Security

* User Registration and Login
* JWT-Based Authentication
* Role-Based Access Control
* Customer, Worker, and Admin Roles
* Protected REST APIs
* Backend-Based Role Verification
* Login Attempt Protection
* Temporary Account Lockout After Failed Login Attempts
* Blocked User Management
* Password Reset Support
* Google OAuth Authentication
* Secure Session Management
* CORS Configuration

### User Management

* User Registration
* User Login
* User Profile Management
* Profile Image Upload
* User Details Management
* User Blocking and Unblocking
* Role Management
* Customer Management
* Worker Account Management

### Service Management

* Create Services
* View Services
* Update Services
* Delete Services
* Service Search
* Service-Specific Options
* Tutor Subject Management
* Home Service Categories

### Booking Management

* Create Bookings
* View Bookings
* User-Specific Bookings
* Worker-Specific Bookings
* Booking Assignment
* Booking Status Management
* Accept Booking
* Reject Booking
* Complete Booking
* Booking Cancellation
* Date and Time Slot Management
* Problem Description
* Worker Information
* Booking History

### Worker Management

* Worker Account Creation
* Worker Profile Management
* Worker Availability
* Online / Offline Status
* Worker Booking Assignment
* Assigned Booking Management
* Accept / Reject Requests
* Complete Services
* Worker Statistics

### Payment System

HiveCare integrates **Razorpay** for secure online payments.

Payment functionality includes:

* Razorpay Order Creation
* Online Payments
* Pay Now
* Pay After Service
* Payment Verification
* Razorpay Signature Verification
* Payment Status Tracking
* Razorpay Order ID Management
* Razorpay Payment ID Management
* Payment Completion Tracking
* Payment Receipts

### Reviews

* Customer Reviews
* Review Management
* Booking-Based Reviews
* Admin Review Management

### Admin Management

The backend provides APIs supporting the HiveCare Admin Dashboard.

Admin functionality includes:

* Dashboard Statistics
* User Management
* Worker Management
* Service Management
* Booking Management
* Worker Assignment
* Booking Status Management
* Payment Statistics
* Revenue Analytics
* Customer Reviews
* Booking Reports
* Monthly Reports
* Yearly Reports
* Full Booking Reports
* CSV Report Data
* User Blocking / Unblocking

### Support Chat

HiveCare includes backend support for the application's customer support chat.

The chat API supports:

* Customer Support Messages
* User Information
* User Role Information
* Worker Service Information
* AI-Assisted Support
* Gemini API Integration

## Tech Stack

### Backend

* Java
* Spring Boot
* Spring MVC
* Spring Data JPA
* Hibernate
* Spring Security
* JWT
* Maven

### Database

* MySQL
* JPA / Hibernate ORM

### Authentication

* JWT Authentication
* Google OAuth
* Role-Based Authorization

### Payments

* Razorpay API

### External Services

* Google OAuth
* Google Places / Maps Services
* Gemini AI API

## API Architecture

HiveCare follows a layered REST API architecture:

```text
React Frontend
       │
       ▼
   REST APIs
       │
       ▼
Controller Layer
       │
       ▼
Service Layer
       │
       ▼
Repository Layer
       │
       ▼
JPA / Hibernate
       │
       ▼
   MySQL Database
```

## Project Structure

```text
src/
└── main/
    ├── java/
    │   └── com/
    │       └── hivecare/
    │           ├── controller/
    │           ├── service/
    │           ├── repository/
    │           ├── model/
    │           ├── dto/
    │           ├── security/
    │           ├── config/
    │           └── util/
    │
    └── resources/
        └── application.properties
```

### Controller Layer

Handles incoming HTTP requests and exposes REST endpoints.

Examples include:

* User APIs
* Admin APIs
* Worker APIs
* Booking APIs
* Service APIs
* Payment APIs
* Review APIs
* Profile APIs
* Chat APIs

### Service Layer

Contains the application's business logic.

Responsibilities include:

* Authentication
* User management
* Booking processing
* Worker assignment
* Payment verification
* Service management
* Review processing
* Admin operations

### Repository Layer

Uses Spring Data JPA repositories for database operations.

Repositories handle:

* User data
* Booking data
* Service data
* Review data
* Database queries
* Entity persistence

### Model / Entity Layer

The backend contains entities representing the main HiveCare data.

Major entities include:

* User
* Service
* Booking
* Review

## Booking Status

The booking system supports multiple stages of a service request, including:

```text
PENDING
   ↓
ACCEPTED
   ↓
COMPLETED
```

Additional states such as rejection and cancellation are also supported by the booking workflow.

## Payment Status

Payments are tracked independently from booking status.

```text
PENDING
   ↓
PAID
```

The backend stores payment information such as:

* Payment Status
* Payment Method
* Payment Timing
* Razorpay Order ID
* Razorpay Payment ID
* Razorpay Signature
* Payment Date
* Completion Information

## User Roles

HiveCare supports three primary roles:

```text
USER
WORKER
ADMIN
```

The backend is responsible for determining and enforcing the user's role rather than relying only on frontend data.

### USER

Customers can:

* Browse services
* Create bookings
* View booking history
* Make payments
* Review completed services
* Manage their profile

### WORKER

Workers can:

* Manage availability
* View assigned bookings
* Accept bookings
* Reject bookings
* Complete services

### ADMIN

Administrators can:

* Manage users
* Manage workers
* Manage services
* Manage bookings
* Assign workers
* Monitor payments
* View analytics
* Manage reviews
* Generate reports

## Database

HiveCare uses **MySQL** with Spring Data JPA and Hibernate.

The database stores information related to:

* Users
* Services
* Bookings
* Workers
* Reviews
* Payments
* Authentication and account security

The backend can also be configured to use a managed MySQL-compatible database for deployment.

## CORS

The backend supports Cross-Origin Resource Sharing (CORS) to allow the React frontend to communicate with the Spring Boot API.

This allows HiveCare to work with:

* Local React development
* Deployed React frontend
* Desktop/Electron frontend
* Different frontend environments

## Frontend Integration

The backend provides REST APIs consumed by the HiveCare React frontend.

```text
HiveCare React Frontend
          │
          │ HTTP / REST
          ▼
HiveCare Spring Boot Backend
          │
          │ JPA / Hibernate
          ▼
       MySQL
```

## API Responsibilities

The backend is responsible for:

* Authentication
* Authorization
* User Management
* Worker Management
* Service Management
* Booking Management
* Payment Processing
* Payment Verification
* Reviews
* Reports
* Profile Management
* Support Chat
* Database Operations

## Deployment

The HiveCare backend can be deployed on cloud platforms that support Spring Boot applications and Docker.

The backend can be connected to a managed MySQL database such as **Aiven**.

Environment-specific configuration can be used for:

* Database credentials
* JWT secret
* JWT expiration
* Razorpay credentials
* OAuth credentials
* Gemini API credentials
* Frontend URLs
* CORS configuration

## Project Status

HiveCare Backend is an actively developed Spring Boot REST API supporting a complete home-service management platform.

### Current Architecture

```text
                    HiveCare Platform
                           │
              ┌────────────┴────────────┐
              │                         │
        React Frontend             Spring Boot API
              │                         │
              │                  ┌──────┴──────┐
              │                  │             │
              │               Security      Business
              │                  │             │
              │                JWT/OAuth     Services
              │                                │
              └────────────── REST ─────────────┤
                                               │
                                      Spring Data JPA
                                               │
                                             MySQL
                                               │
                                  ┌────────────┴────────────┐
                                  │                         │
                              Razorpay                  Gemini AI
```

## Future Enhancements

Potential future improvements include:

* Real-Time Notifications
* Email Notifications
* Push Notifications
* Advanced Worker Scheduling
* Live Worker Location Tracking
* Advanced Analytics
* Automated Payment Reminders
* Improved AI Support
* Additional OAuth Providers
* Performance Optimization
* API Rate Limiting
* Expanded Security Monitoring