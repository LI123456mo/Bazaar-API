# 🛒 Bazaar — Multi-Vendor E-Commerce REST API

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6-blue.svg)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A high-performance, production-ready E-commerce REST API built with **Spring Boot 3**, **Spring Security 6**, and **PostgreSQL**. Features dynamic product searching via JPA Specifications, advanced pagination/sorting, soft-delete data integrity, multi-vendor support with role-based access control (RBAC), and AI-powered RAG assistant for intelligent product recommendations.

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [RBAC & Permissions](#rbac--permissions)
- [API Endpoints](#api-endpoints)
- [Setup & Installation](#setup--installation)
- [Environment Variables](#environment-variables)
- [Running the Application](#running-the-application)
- [Features](#features)
- [AI RAG Assistant](#ai-rag-assistant)
- [Security](#security)

---

## 🎯 Project Overview

**Bazaar** is a scalable, enterprise-grade marketplace platform that enables:

- **Multi-vendor support** — Vendors can register, manage inventory, and fulfill orders independently
- **RBAC-based access control** — Fine-grained permissions for CUSTOMER, VENDOR, ADMIN, and SUPER_ADMIN roles
- **Dynamic product search** — Filter products by name, category, price using JPA Specifications
- **Order management** — Full lifecycle from placement to fulfillment with soft-delete audit trails
- **Email verification** — Secure registration with HTML email templates via JavaMailSender
- **JWT authentication** — Stateless security with access & refresh tokens
- **AI-powered recommendations** — RAG system using Ollama embeddings + Groq LLM for intelligent assistance

### 💎 Key Highlights

- ✅ **Clean Architecture** — Strict separation of concerns (Web → Service → Repository → DB)
- ✅ **Enterprise Security** — BCrypt password hashing, JWT authentication, method-level authorization
- ✅ **Global Exception Handling** — Structured JSON error responses with business error codes
- ✅ **Data Integrity** — Soft-delete pattern, transaction management, optimistic locking
- ✅ **Performance Optimized** — Lazy loading, pagination, indexed queries, N+1 prevention
- ✅ **API Documentation** — Swagger UI at `/swagger-ui/index.html`

---

## 🛠 Tech Stack

| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **Java** | 17 (LTS) | Programming language |
| **Spring Boot** | 3.3.5 | Application framework |
| **Spring Security** | 6 | Authentication & authorization |
| **Spring Data JPA** | 3.3.5 | Object-Relational Mapping |
| **Spring Mail** | 3.3.5 | Email delivery (SMTP) |
| **PostgreSQL** | 16+ | Production database |
| **JWT (JJWT)** | 0.12.6 | Stateless authentication |
| **Lombok** | 1.18.30 | Boilerplate reduction |
| **MapStruct** | *Latest* | Type-safe DTO mapping |
| **SpringDoc OpenAPI** | 2.5.0 | Swagger/OpenAPI documentation |
| **Spring AI** | 1.0.0 | RAG/LLM integration |
| **Ollama** | Local | Embeddings (nomic-embed-text) |
| **Groq** | Cloud API | LLM inference (llama-3.1-8b) |

---

## 🏗 Architecture
