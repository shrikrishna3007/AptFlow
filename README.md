# AptFlow Backend API

AptFlow is a Spring Boot–based backend service designed to simplify the management of rental rooms and apartment data. It handles everything from user registration and room listings to bookings, billing, and payments — all in one place.

The system supports both monthly and checkout-based billing. Bills are automatically generated and sent to users via email, complete with PDF attachments. Behind the scenes, the app integrates with Razorpay for secure payments, uses Redis to manage session tokens, and stores data in both MySQL and MongoDB. A built-in scheduler ensures that billing happens on time without manual effort.

---

## 🚀 Features

* JWT-based authentication and role-based authorization (Admin/User)
* Refresh token support for secure multi-device login
* Email service for password resets
* Razorpay payment integration
* Redis support for session/token storage
* MySQL and MongoDB persistence
* Scheduled job to automatically generate bills at defined intervals
* Actuator endpoints for monitoring
* API documentation using Swagger (SpringDoc)

---

## 🏗️ Tech Stack

* **Java 17**
* **Spring Boot** (Spring Web, Security, Data JPA, Mail)
* **MySQL**, **MongoDB**
* **Redis**
* **JWT** for secure authentication
* **Razorpay** API
* **Swagger / OpenAPI** for API docs
* **Spring Boot Actuator**
* **Spring Dotenv** for environment variables
* **Lombok** for code generation
* **SLF4J** for logging
* **PDFBox** for PDF generation
* **JUnit** and **Mockito** for testing

---

## 📁 Project Structure

```
src/
 └── main/
     ├── java/
     └── resources/
         ├── application.properties
         └── logback-spring.xml
         
```
---

## ⚙️ Getting Started

### 1. Clone the repo

```bash
git clone https://github.com/your-username/aptflow-backend.git
cd aptflow-backend
```

### 2. Set up environment variables

* Copy `.env.example` to `.env`
* Fill in database credentials, JWT secrets, Razorpay keys, etc.

### 3. Configure databases

* Make sure **MySQL** and **MongoDB** are up and running.
* Create necessary databases or let Spring Boot auto-create using `ddl-auto=update`

### 4. Run the application

```bash
./mvnw spring-boot:run
```

Or use your preferred IDE.

---

## 📚 API Documentation

* Swagger UI: `http://localhost:8080/your-swagger-ui-path`
* OpenAPI JSON: `http://localhost:8080/your-api-docs-path`

Paths configurable via `.env` and `application.properties`.

---

## 🔐 Authentication

* JWT-based authentication with role support
* Refresh tokens are managed per device using Redis
* Access/refresh token expiration configurable in `.env`

---

## 🔧 Key Endpoints

| Endpoint                 | Method | Description                   |
| ------------------------ | ------ | ----------------------------- |
| `/api/register`          | POST   | Register a new user           |
| `/api/login`             | POST   | Login and receive tokens      |
| `/api/refresh-token`     | POST   | Refresh the access token      |
| `/api/reset-password`    | POST   | Trigger password reset email  |

---

## 📄 Environment Files

* `.env.example` → template file for your environment config
* `.env` → your actual keys and secrets (ignored by Git)
* `application.properties.example` → properties placeholder

---

## 🚫 .gitignore Highlights

* Ignores compiled files: `target/`, `build/`
* Ignores IDE files: `.idea/`, `.classpath`, `.vscode/`
* Ignores secrets: `.env`, `application.properties`

---

