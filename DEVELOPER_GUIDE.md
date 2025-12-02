# Developer Guide - Auth Service Integration

Technical reference for developers building frontend or integrating with other microservices.

---

## 📋 Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Registration Flow](#registration-flow)
3. [Login Flow](#login-flow)
4. [Frontend Integration](#frontend-integration)
5. [Service-to-Service Communication](#service-to-service-communication)
6. [Database Schema](#database-schema)
7. [What to Build Next](#what-to-build-next)

---

## 🏗️ Architecture Overview

### **The Problem**
- Landing page with Register/Sign In buttons
- User chooses: Campus, Student, or Company
- Each type has **3-4 pages of different registration fields**
- 5 microservices need to work together

### **The Solution: Two-Phase Registration**

**Phase 1 - Auth Service (Lightweight):**
- Stores: email, password, user type
- Returns: JWT token + `profileCompleted: false`

**Phase 2 - Respective Service (Detailed):**
- Student Service → Student profile (education, skills, etc.)
- Campus Service → Campus profile (courses, faculty, etc.)
- Company Service → Company profile (jobs, etc.)
- Returns: profile ID (student_id, campus_id, etc.)
- Calls back: Auth Service to mark profile complete

### **Services & Ports**
```
Auth Service     → localhost:8081 (Ready ✅)
Campus Service   → localhost:8082 (To build)
Student Service  → localhost:8083 (To build)
Company Service  → localhost:8084 (To build)
Admin Service    → localhost:8085 (To build)
```

---

## 🔄 Registration Flow

```
Step 1: Landing Page
User clicks "Register as Student"

Step 2: Frontend calls Auth Service
POST http://localhost:8081/api/v1/auth/register
Body: {
  "email": "student@example.com",
  "password": "password123",
  "userType": "STUDENT"
}

Response: {
  "userId": "abc-123",
  "accessToken": "jwt...",
  "refreshToken": "refresh...",
  "profileCompleted": false,  ← Check this!
  "redirectTo": "student-service"
}

Step 3: Frontend checks profileCompleted
if (!profileCompleted) {
  // Redirect to profile completion form
  navigate('/student/profile/complete');
}

Step 4: User fills 3-4 page profile form
Page 1: Personal Info
Page 2: Education Details
Page 3: Skills & Experience
Page 4: Documents Upload

Step 5: Frontend submits to Student Service
POST http://localhost:8083/api/v1/students/profile
Headers: Authorization: Bearer <jwt-token>
Body: {
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "2000-01-01",
  "education": [...],
  "skills": [...],
  // ... all profile data
}

Response: {
  "studentId": "xyz-789"
}

Step 6: Frontend notifies Auth Service
POST http://localhost:8081/api/v1/auth/profile/complete
Headers: Authorization: Bearer <jwt-token>
Body: {
  "profileServiceId": "xyz-789"
}

Step 7: Frontend redirects to dashboard
navigate('/student/dashboard');
```

---

## 🔐 Login Flow

```
Step 1: User enters credentials
Email: student@example.com
Password: password123

Step 2: Frontend calls Auth Service
POST http://localhost:8081/api/v1/auth/login
Body: {
  "email": "student@example.com",
  "password": "password123"
}

Response: {
  "userId": "abc-123",
  "accessToken": "jwt...",
  "userType": "STUDENT",
  "profileCompleted": true/false,  ← Check this!
  "profileServiceId": "xyz-789" (if completed)
}

Step 3: Frontend routing logic
if (!response.profileCompleted) {
  // Profile not complete - redirect to form
  navigate('/student/profile/complete');
} else {
  // Profile complete - redirect to dashboard
  navigate('/student/dashboard');
}
```

---

## 📱 Frontend Integration

### **React/Angular Example - Registration**

```javascript
// RegisterPage.jsx
const handleRegister = async (userType) => {
  const response = await fetch('http://localhost:8081/api/v1/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      email: formData.email,
      password: formData.password,
      userType: userType  // "STUDENT", "CAMPUS", or "COMPANY"
    })
  });

  const data = await response.json();
  
  if (data.success) {
    // Save tokens
    localStorage.setItem('accessToken', data.data.accessToken);
    localStorage.setItem('refreshToken', data.data.refreshToken);
    localStorage.setItem('userType', data.data.userType);
    
    // Redirect to profile form
    if (!data.data.profileCompleted) {
      const routes = {
        'STUDENT': '/student/profile/complete',
        'CAMPUS': '/campus/profile/complete',
        'COMPANY': '/company/profile/complete'
      };
      navigate(routes[data.data.userType]);
    }
  }
};
```

### **Login**

```javascript
// LoginPage.jsx
const handleLogin = async (email, password) => {
  const response = await fetch('http://localhost:8081/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });

  const data = await response.json();
  
  if (data.success) {
    localStorage.setItem('accessToken', data.data.accessToken);
    localStorage.setItem('refreshToken', data.data.refreshToken);
    
    // Routing logic
    const userType = data.data.userType.toLowerCase();
    
    if (!data.data.profileCompleted) {
      navigate(`/${userType}/profile/complete`);
    } else {
      navigate(`/${userType}/dashboard`);
    }
  }
};
```

### **Profile Completion (Example: Student)**

```javascript
// StudentProfileForm.jsx
const submitProfile = async (profileData) => {
  const token = localStorage.getItem('accessToken');
  
  try {
    // Step 1: Submit to Student Service
    const response1 = await fetch('http://localhost:8083/api/v1/students/profile', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(profileData)
    });
    
    const data1 = await response1.json();
    const studentId = data1.data.studentId;
    
    // Step 2: Notify Auth Service
    await fetch('http://localhost:8081/api/v1/auth/profile/complete', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ profileServiceId: studentId })
    });
    
    // Step 3: Update local state and redirect
    localStorage.setItem('profileCompleted', 'true');
    navigate('/student/dashboard');
    
  } catch (error) {
    console.error('Profile submission failed:', error);
  }
};
```

### **Route Guards**

```javascript
// AuthGuard.jsx - Requires authentication
function AuthGuard({ children }) {
  const token = localStorage.getItem('accessToken');
  if (!token) {
    return <Navigate to="/signin" />;
  }
  return children;
}

// ProfileGuard.jsx - Ensures profile is completed
function ProfileGuard({ children }) {
  const profileCompleted = localStorage.getItem('profileCompleted') === 'true';
  const userType = localStorage.getItem('userType');
  
  if (!profileCompleted) {
    return <Navigate to={`/${userType.toLowerCase()}/profile/complete`} />;
  }
  return children;
}

// Usage in App.jsx
<Routes>
  {/* Public */}
  <Route path="/" element={<LandingPage />} />
  <Route path="/signin" element={<LoginPage />} />
  <Route path="/register" element={<RegisterPage />} />
  
  {/* Student - Profile form (auth required, profile not required) */}
  <Route path="/student/profile/complete" element={
    <AuthGuard><StudentProfileForm /></AuthGuard>
  } />
  
  {/* Student - Dashboard (auth + profile required) */}
  <Route path="/student/dashboard" element={
    <AuthGuard><ProfileGuard><StudentDashboard /></ProfileGuard></AuthGuard>
  } />
</Routes>
```

---

## 🔗 Service-to-Service Communication

### **Example: Student Service validates user**

When Student Service receives profile creation request:

```java
// Student Service - StudentController.java
@PostMapping("/students/profile")
public ResponseEntity<?> createProfile(
    @AuthenticationPrincipal UserPrincipal principal,
    @RequestBody StudentProfileRequest request
) {
    // JWT provides userId and userType
    String userId = principal.getUserId();
    String userType = principal.getUserType();
    
    // 1. Validate user type
    if (!userType.equals("STUDENT")) {
        throw new ForbiddenException("Only students can create student profiles");
    }
    
    // 2. Create student profile
    Student student = new Student();
    student.setUserId(userId);  // Link to auth service
    student.setFirstName(request.getFirstName());
    student.setLastName(request.getLastName());
    // ... set other fields
    
    student = studentRepository.save(student);
    
    // 3. Return student_id (Frontend will use this to call Auth Service)
    return ResponseEntity.ok(new StudentResponse(student.getStudentId()));
}
```

**Note:** Student Service should NOT call Auth Service directly. Let the frontend handle it for better error handling and user feedback.

---

## 🗄️ Database Schema

### **Auth Service (auth_db)**

**users table:**
```sql
CREATE TABLE users (
    user_id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    user_type ENUM('CAMPUS', 'STUDENT', 'COMPANY', 'ADMIN'),
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    profile_completed BOOLEAN DEFAULT FALSE,  -- NEW
    profile_service_id VARCHAR(36) NULL,      -- NEW (student_id, campus_id, etc.)
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Run migration:**
```bash
mysql -u root -p < database-migration.sql
```

### **Student Service (student_db)** - To be built

```sql
CREATE TABLE students (
    student_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,  -- From auth service, NO FK!
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    date_of_birth DATE,
    email VARCHAR(255),
    phone VARCHAR(20),
    -- ... all student-specific fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### **Campus Service (campus_db)** - To be built

```sql
CREATE TABLE campuses (
    campus_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,  -- From auth service, NO FK!
    campus_name VARCHAR(255),
    campus_type VARCHAR(50),
    -- ... all campus-specific fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Why no Foreign Keys?**
- Each service has its own database
- Services must remain independent
- Link via user_id (stored as string, validated via JWT)

---

## 📝 What to Build Next

### **Priority 1: Campus Service (Port 8082)**

**Campus Registration Endpoint:**
```bash
POST /api/v1/campus/register
Headers: Authorization: Bearer <jwt>
Body: {
  "campusName": "ABC University",
  "campusLogoUrl": "https://...",
  "campusRank": 50,
  "adminName": "John Doe",
  "adminEmail": "admin@abc.edu",
  "adminPhone": "+91-9876543210",
  "adminDepartment": "Administration",
  "adminDesignation": "Registrar",
  "websiteUrl": "https://abc.edu",
  "aboutCampus": "Leading university...",
  "campusAddress": "123 Main St, City, State"
}
Response: { 
  "campusId": "xyz-789",
  "approvalStatus": "PENDING"
}
```

**Frontend Flow:**
```javascript
// After form submission
const response = await fetch('http://localhost:8082/api/v1/campus/register', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(formData)
});

const data = await response.json();
const campusId = data.data.campusId;

// Mark profile complete
await fetch('http://localhost:8081/api/v1/auth/profile/complete', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ profileServiceId: campusId })
});
```

### **Priority 2: Student Service (Port 8083)**

**Required Endpoint:**
```java
POST /api/v1/students/profile
Headers: Authorization: Bearer <jwt>
Body: {
  firstName, lastName, dateOfBirth,
  education: [...],
  skills: [...],
  experience: [...],
  documents: [...]
}
Response: { studentId }
```

**Steps:**
1. Create Spring Boot project
2. Setup MySQL database `student_db`
3. Create Student entity with `user_id` field
4. Implement JWT validation (copy from Auth Service)
5. Create profile endpoint
6. Return student_id in response

### **Priority 2: Frontend (React/Angular)**

**Required Pages:**
1. Landing page with Register/Sign In
2. User type selection (Campus/Student/Company)
3. Multi-page student profile form (3-4 pages)
4. Student dashboard
5. Similar for Campus and Company

**Key Logic:**
- Check `profileCompleted` flag on login
- Route users based on profile status
- Call Auth Service after profile creation

### **Priority 3: Campus & Company Services**

Similar to Student Service, but with their specific fields.

---

## 🔐 Admin User Setup

Admin users cannot register via API. Create manually:

```sql
-- 1. Hash password (use bcrypt online tool or Spring's PasswordEncoder)
-- Example: "admin123" → "$2a$12$..."

-- 2. Insert admin user
INSERT INTO users (user_id, email, password_hash, user_type, is_verified, is_active, profile_completed)
VALUES (UUID(), 'admin@recn.com', '$2a$12$your_hashed_password', 'ADMIN', TRUE, TRUE, TRUE);

-- 3. Assign SUPER_ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT 
    (SELECT user_id FROM users WHERE email = 'admin@recn.com'),
    (SELECT role_id FROM roles WHERE role_name = 'SUPER_ADMIN');
```

---

## 🧪 Testing Checklist

### **Auth Service**
- [ ] Register new student user
- [ ] Check `profileCompleted = false` in response
- [ ] Login with incomplete profile
- [ ] Verify redirect info in response
- [ ] Call profile complete endpoint
- [ ] Login again, check `profileCompleted = true`

### **Integration (once other services are built)**
- [ ] Register → Student Service → Mark Complete → Login
- [ ] Check JWT validation across services
- [ ] Test error scenarios (invalid token, wrong user type)

---

## 🔧 Environment Configuration

**For Frontend:**
```javascript
// config.js
const API_BASE_URLS = {
  development: {
    auth: 'http://localhost:8081/api/v1',
    student: 'http://localhost:8083/api/v1',
    campus: 'http://localhost:8082/api/v1',
    company: 'http://localhost:8084/api/v1'
  },
  production: {
    auth: 'https://api.recn.com/auth/v1',
    student: 'https://api.recn.com/student/v1',
    campus: 'https://api.recn.com/campus/v1',
    company: 'https://api.recn.com/company/v1'
  }
};

export default API_BASE_URLS[process.env.NODE_ENV || 'development'];
```

---

## 📊 API Response Format

All endpoints return:

**Success:**
```json
{
  "success": true,
  "message": "Optional message",
  "data": { ... },
  "statusCode": 200,
  "timestamp": "2025-12-01T10:30:00"
}
```

**Error:**
```json
{
  "success": false,
  "error": "Error message",
  "statusCode": 400,
  "timestamp": "2025-12-01T10:30:00"
}
```

---

## 🎯 Key Takeaways

1. **Auth Service** stores only credentials, not profiles
2. **profileCompleted flag** drives frontend routing
3. **Each service** owns its domain data
4. **No cross-database FKs** = microservices independence
5. **JWT tokens** enable stateless communication
6. **Frontend** orchestrates the flow between services

---

## 💡 Common Questions

**Q: Where do I store student education details?**
A: In Student Service database, not Auth Service.

**Q: How does Student Service know which user is creating profile?**
A: JWT token contains userId. Extract it from token.

**Q: What if Student Service is down during registration?**
A: User can still register in Auth Service. Complete profile later when service is up.

**Q: Can I change user type after registration?**
A: Not recommended. User type determines role and which service handles profile.

---

Need more help? Check the code in `src/` folder for implementation examples.

