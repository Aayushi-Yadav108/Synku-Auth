# RecN Platform - Auth Service

Authentication and Authorization Microservice with JWT support for multi-service architecture.

## 🚀 Quick Start

```bash
# 1. Setup database
mysql -u root -p < database-migration.sql

# 2. Configure
vim src/main/resources/application.yml
# Update: database credentials, JWT secret

# 3. Run
./run.sh
```

Server: `http://localhost:8081/api/v1`

---

## 🎯 What It Does

This service handles **authentication only**. Profile data (student details, campus info, etc.) is managed by respective services.

**Two-Phase Registration:**
1. User registers here → Gets JWT token + `profileCompleted: false`
2. User completes profile in respective service (Student/Campus/Company)
3. Respective service notifies this service → `profileCompleted: true`

**On Login:**
- If `profileCompleted = false` → Redirect to profile form
- If `profileCompleted = true` → Redirect to dashboard

👉 **See [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md) for architecture details and integration guide**

---

## 🔐 Features

- JWT authentication (Access + Refresh tokens)
- Role-based access control (RBAC)
- Two-phase registration support
- Account lockout protection
- Token refresh mechanism
- Microservices-ready

---

## 📡 API Endpoints

### **Authentication**
```bash
POST /auth/register        # Register new user
POST /auth/login           # Login
POST /auth/refresh-token   # Refresh token
POST /auth/logout          # Logout
GET  /auth/me              # Get current user
POST /auth/profile/complete # Mark profile complete (called by other services)
```

### **User Management (Admin)**
```bash
GET    /users              # List all users
GET    /users/{id}         # Get user by ID
PATCH  /users/{id}/status  # Activate/deactivate
PATCH  /users/{id}/verify  # Verify user
DELETE /users/{id}         # Delete user
```

---

## 🧪 Test It

```bash
# Register
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","userType":"STUDENT"}'

# Response includes:
# "profileCompleted": false
# "redirectTo": "student-service"

# Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

---

## ⚙️ Configuration

Update `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auth_db
    username: root
    password: your_password

jwt:
  secret: your-secret-key-256-bits-minimum
  access-token-expiration: 900000      # 15 minutes
  refresh-token-expiration: 604800000  # 7 days
```

**Generate JWT secret:**
```bash
openssl rand -hex 32
```

---

## 🛠️ Tech Stack

- Java 17 + Spring Boot 3.2
- Spring Security 6 + JWT
- MySQL 8 + Spring Data JPA
- Maven 3.6+

---

## 📚 For Developers

Building frontend or other microservices? Check out:

**[DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md)** - Complete technical reference:
- Architecture flow diagrams
- Frontend integration examples
- Service-to-service communication
- Database schema details
- What to build next

---

## 🔐 Security Notes

- **Passwords**: BCrypt hashing (strength 12), minimum 8 characters
- **Account Lockout**: 5 failed attempts = 30 min lock
- **JWT Tokens**: Access (15 min), Refresh (7 days)
- **Admin User**: Create manually in database (see DEVELOPER_GUIDE.md)

---

## 📦 Services Architecture

```
Auth Service (8081)     ← You are here
Student Service (8083)  ← To be built
Campus Service (8082)   ← To be built
Company Service (8084)  ← To be built
Admin Service (8085)    ← To be built
```

Each service has its own database. No cross-database foreign keys.

---

## 🚀 Deployment

```bash
# Development
mvn spring-boot:run

# Production
mvn clean package
java -jar target/auth-service-1.0.0.jar
```

---

## 📄 License

Copyright © 2025 RecN Platform. All rights reserved.
