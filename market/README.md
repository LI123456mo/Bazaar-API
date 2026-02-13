# 🛒 Limo Market API
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)

A high-performance, industry-standard backend solution for modern e-commerce marketplaces. Built with a focus on security, scalability, and clean code principles.

---

## 💎 Project Highlights
* **Clean Architecture:** Strict separation of concerns using the Layered Pattern (Web, Service, Domain, Persistence).
* **Enterprise Security:** Built-in password hashing with BCrypt and advanced Class-Level validation for user safety.
* **Proactive Error Handling:** Global Exception Interceptor providing structured, frontend-friendly JSON error responses.
* **Automated Mapping:** High-performance DTO mapping using MapStruct to prevent data leakage from the domain layer.

---

## 🛠 Tech Stack
| Technology | Usage |
| :--- | :--- |
| **Java 21** | Modern LTS programming language |
| **Spring Boot 3** | Core application framework |
| **Spring Security** | Authentication and authorization |
| **Spring Data JPA** | Object-Relational Mapping (ORM) |
| **PostgreSQL** | Production-grade relational database |
| **Lombok** | Boilerplate reduction |
| **MapStruct** | Type-safe bean mapping |

---

## 📂 Project Structure
```text
src/main/java/com/conel/market/
├── config/        # Security and Bean configurations
├── controller/    # REST API Endpoints
├── dto/           # Data Transfer Objects (Request/Response)
├── exceptions/    # Global Error Handling logic
├── mapper/        # DTO-to-Entity conversion
├── model/         # Database Entities
├── repository/    # Data Access Layer
├── service/       # Business Logic Layer
└── validation/    # Custom Business Rule Validators