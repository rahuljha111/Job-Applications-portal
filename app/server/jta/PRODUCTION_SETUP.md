# Job Application Tracker - Production Setup Guide

## ✅ Fixed Issues

### 1. **Email Verification Enabled**
- Users must verify their email before logging in
- Verification emails are sent automatically upon registration
- OTP-based verification with 10-minute expiration
- Maximum 3 attempts per verification code
- Daily rate limit of 5 verification requests per email

### 2. **Database Connection**
- PostgreSQL database successfully connected
- Connection pooling configured with HikariCP
- JPA entities properly mapped

### 3. **Authentication Flow**
- Registration → Email Verification → Login
- JWT-based authentication with access and refresh tokens
- Access token: 1 hour expiration
- Refresh token: 7 days expiration

### 4. **Fixed Bugs**
- Fixed SQL column naming issue in Job entity (createdAt → created_at)
- Fixed duplicate dependency in pom.xml
- Fixed package naming in JwtAuthenticationFilter
- Added SMTP connection timeout configuration

---

## 🚀 How to Run the Application

### Prerequisites
- Java 17 or higher
- PostgreSQL 15.x
- Maven 3.6+
- Gmail account with App Password (for SMTP)

### 1. Database Setup

```sql
-- Create database
CREATE DATABASE jobportal1;

-- Connect to database
\c jobportal1

-- Tables will be created automatically by Hibernate
```

### 2. Environment Configuration

For local development, `src/main/resources/application.properties` still includes safe defaults.
For production, use `SPRING_PROFILES_ACTIVE=prod` and set the following environment variables:

`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `APP_MAIL_FROM`, `OPENAI_API_KEY`, `FILE_UPLOAD_DIR`.

The production profile is defined in `src/main/resources/application-prod.properties` and uses `ddl-auto=validate`, `open-in-view=false`, and INFO-level logging.

### 3. Gmail SMTP Setup

1. Go to https://myaccount.google.com/security
2. Enable 2-Step Verification
3. Go to https://myaccount.google.com/apppasswords
4. Create a new App Password for "Mail"
5. Copy the 16-character password
6. Use it in `spring.mail.password`

### 4. Build and Run

```bash
# Clean and compile
./mvnw clean compile

# Run the application
./mvnw spring-boot:run
```

The application will start on **http://localhost:8081**

### 5. Docker

Build the backend image from `server/jta`:

```bash
docker build -t jobtracker-backend .
```

Run it with production env vars:

```bash
docker run --rm -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/jobportal1 \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=YOUR_DB_PASSWORD \
  -e JWT_SECRET=YOUR_SECURE_256_BIT_SECRET_KEY_MINIMUM_32_CHARACTERS \
  -e MAIL_HOST=smtp.gmail.com \
  -e MAIL_USERNAME=YOUR_GMAIL@gmail.com \
  -e MAIL_PASSWORD=YOUR_GMAIL_APP_PASSWORD \
  -e APP_MAIL_FROM=YOUR_GMAIL@gmail.com \
  jobtracker-backend
```

---

## 📝 API Endpoints

### Authentication

#### 1. Register User
```http
POST /api/users/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePassword123!",
  "role": "CANDIDATE"
}
```

**Response:**
```json
{
  "id": "uuid",
  "name": "John Doe",
  "email": "john@example.com",
  "role": "CANDIDATE",
  "active": true
}
```

**Important:** After registration, user receives a 6-digit verification code via email.

#### 2. Verify Email
```http
POST /api/verification/verify-email
Content-Type: application/json

{
  "email": "john@example.com",
  "otpCode": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Email verified successfully"
}
```

#### 3. Resend Verification Code
```http
POST /api/verification/resend-verification
Content-Type: application/json

{
  "email": "john@example.com"
}
```

#### 4. Login
```http
POST /api/users/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePassword123!"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "name": "John Doe",
    "email": "john@example.com",
    "role": "CANDIDATE",
    "location": null
  }
}
```

**Note:** Login will fail if email is not verified:
```json
{
  "message": "Email not verified. Please check your email for verification code.",
  "status": 403
}
```

#### 5. Refresh Token
```http
POST /api/users/refresh-token
Content-Type: application/json

{
  "refreshToken": "your-refresh-token"
}
```

---

## 🔒 Security Features

### 1. **Email Verification**
- ✅ Mandatory email verification before login
- ✅ 6-digit OTP codes sent via email
- ✅ 10-minute expiration on verification codes
- ✅ Maximum 3 attempts per code
- ✅ Daily rate limiting (5 requests per email)
- ✅ Automatic cleanup of expired verifications (hourly)

### 2. **Authentication**
- ✅ JWT-based stateless authentication
- ✅ BCrypt password hashing
- ✅ Role-based access control (ADMIN, RECRUITER, CANDIDATE)
- ✅ Refresh token rotation

### 3. **Rate Limiting**
- General API: 60 requests per minute
- Login attempts: 5 per minute
- AI endpoints: 20 requests per hour

### 4. **CORS Configuration**
- Configured for frontend origins
- Exposed headers for rate limit information

---

## 🧪 Testing the Application

### Test Registration Flow

```bash
# 1. Register a user
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "TestPassword123!",
    "role": "CANDIDATE"
  }'

# 2. Check your email for the 6-digit code

# 3. Verify email
curl -X POST http://localhost:8081/api/verification/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "otpCode": "123456"
  }'

# 4. Login
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPassword123!"
  }'
```

### Test Email Verification Requirement

```bash
# Try to login without verifying email (should fail)
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "unverified@example.com",
    "password": "Password123!"
  }'

# Expected Response:
# {
#   "message": "Email not verified. Please check your email for verification code.",
#   "status": 403
# }
```

---

## 📊 Database Tables

The application automatically creates the following tables:

- **users** - User accounts with email verification status
- **email_verifications** - OTP codes for email verification
- **password_reset_tokens** - Tokens for password reset
- **jobs** - Job postings
- **applications** - Job applications
- **companies** - Company information
- **resumes** - User resumes

---

## 🔧 Troubleshooting

### Issue: Email not sending

**Solution:**
1. Check Gmail App Password is correct
2. Ensure 2-Step Verification is enabled on Gmail
3. Check SMTP settings in application.properties
4. Review logs for email sending errors

### Issue: Database connection failed

**Solution:**
1. Verify PostgreSQL is running: `pg_isready`
2. Check database credentials in application.properties
3. Ensure database `jobportal1` exists
4. Check firewall settings for port 5432

### Issue: JWT token invalid

**Solution:**
1. Ensure `jwt.secret` is at least 32 characters
2. Check token expiration times
3. Use refresh token to get new access token

---

## 📱 Frontend Integration

### Authentication Flow

```javascript
// 1. Register
const register = async (userData) => {
  const response = await fetch('http://localhost:8081/api/users/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(userData)
  });
  return response.json();
};

// 2. Verify Email
const verifyEmail = async (email, otpCode) => {
  const response = await fetch('http://localhost:8081/api/verification/verify-email', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, otpCode })
  });
  return response.json();
};

// 3. Login
const login = async (email, password) => {
  const response = await fetch('http://localhost:8081/api/users/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  const data = await response.json();
  // Store tokens
  localStorage.setItem('accessToken', data.token);
  localStorage.setItem('refreshToken', data.refreshToken);
  return data;
};

// 4. Make authenticated requests
const fetchWithAuth = async (url, options = {}) => {
  const token = localStorage.getItem('accessToken');
  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${token}`
    }
  });
  return response;
};
```

---

## 🌐 Production Deployment Checklist

- [ ] Change `jwt.secret` to a strong random value
- [ ] Update database credentials
- [ ] Configure production SMTP server
- [ ] Set up environment variables instead of hardcoded values
- [ ] Enable HTTPS
- [ ] Configure proper CORS origins
- [ ] Set up database backups
- [ ] Configure logging levels
- [ ] Set up monitoring and alerts
- [ ] Review and update rate limits
- [ ] Enable SQL query logging for debugging (optional)

---

## 📄 Environment Variables (Recommended for Production)

```bash
# Database
export DB_URL=jdbc:postgresql://localhost:5432/jobportal1
export DB_USERNAME=postgres
export DB_PASSWORD=secure_password

# JWT
export JWT_SECRET=your_very_secure_and_long_secret_key_here_minimum_32_characters
export JWT_ACCESS_EXPIRATION=3600000
export JWT_REFRESH_EXPIRATION=604800000

# Mail
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password

# Application
export APP_MAIL_FROM=noreply@yourcompany.com
export APP_NAME="Your Job Tracker"
```

---

## 📞 Support

For issues or questions, check the logs at:
- Application logs: `backend.log`
- Detached run logs: Check temp directory

---

**Last Updated:** 2026-03-31
**Application Version:** 0.0.1-SNAPSHOT
**Spring Boot Version:** 3.5.10
