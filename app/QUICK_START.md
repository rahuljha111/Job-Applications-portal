# 🚀 Quick Start Guide - Job Applications Portal

## Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+
- Gmail account (for email verification)

---

## 1. Database Setup

```bash
# Start PostgreSQL
# Create database
createdb jobportal1

# Or use psql
psql -U postgres
CREATE DATABASE jobportal1;
\q
```

---

## 2. Backend Setup

```bash
# Navigate to backend
cd app/server/jta

# Configure application.properties (optional - defaults work for local)
# Edit: src/main/resources/application.properties

# Required: Update mail settings if using your own Gmail
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# Build and run
./mvnw clean install
./mvnw spring-boot:run

# Or on Windows
mvnw.cmd clean install
mvnw.cmd spring-boot:run

# Backend will start on: http://localhost:8081
```

### Get Gmail App Password:
1. Go to Google Account Settings → Security
2. Enable 2-Factor Authentication
3. Go to "App Passwords"
4. Generate password for "Mail"
5. Use this password in `application.properties`

---

## 3. Frontend Setup

```bash
# Navigate to frontend
cd app/client

# Install dependencies
npm install

# Configure environment (optional - default is correct)
# Edit: .env
VITE_API_URL=http://localhost:8081/api

# Start development server
npm run dev

# Frontend will start on: http://localhost:5173
```

---

## 4. Test the Application

### Registration Flow:
1. Open browser: http://localhost:5173
2. Click "Sign Up"
3. Fill registration form:
   - First Name: John
   - Last Name: Doe
   - Email: your-email@gmail.com
   - Password: Test@123
   - Role: CANDIDATE or RECRUITER
4. Click "Register"
5. **Check your email** for 6-digit OTP
6. Enter OTP on verification page
7. Click "Verify Email"
8. Redirected to login → Enter credentials → Success!

### Expected Flow:
```
Register → Email Sent → Enter OTP → Verified → Login → Dashboard
```

---

## 5. Available User Roles

### CANDIDATE (Job Seeker)
- Search and apply for jobs
- Upload and manage resumes
- Track application status
- Get AI-powered career suggestions
- View analytics dashboard

### RECRUITER (Job Poster)
- Post job openings
- Review applications
- Manage company profile
- Track recruitment metrics
- Communicate with candidates

### ADMIN (Platform Administrator)
- Manage all users
- View platform statistics
- Moderate content
- Configure settings

---

## 6. Key Features to Test

### ✅ Authentication
- [x] Registration with email verification
- [x] Login with JWT tokens
- [x] Token refresh
- [x] Forgot password
- [x] Profile management

### ✅ Jobs
- [x] Browse jobs (public)
- [x] Search and filter jobs
- [x] View job details
- [x] Post new jobs (recruiter)
- [x] Close job postings (recruiter)

### ✅ Applications
- [x] Apply to jobs (candidate)
- [x] Track application status
- [x] View applicants (recruiter)
- [x] Update application status (recruiter)

### ✅ Resumes
- [x] Upload resume (PDF/DOC)
- [x] Download resume
- [x] Set default resume
- [x] Manage multiple resumes

### ✅ AI Features
- [x] Resume analysis
- [x] Interview preparation
- [x] Skill suggestions

---

## 7. API Testing with Postman

### Import Collection:
```json
Base URL: http://localhost:8081/api
```

### Example: Register User
```bash
POST http://localhost:8081/api/users/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "Test@123",
  "role": "CANDIDATE"
}
```

### Example: Verify Email
```bash
POST http://localhost:8081/api/verification/verify-email
Content-Type: application/json

{
  "email": "john@example.com",
  "otpCode": "123456"
}
```

### Example: Login
```bash
POST http://localhost:8081/api/users/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "Test@123"
}

# Response includes:
{
  "token": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "user": { ... }
}
```

### Example: Get Profile (Authenticated)
```bash
GET http://localhost:8081/api/users/me
Authorization: Bearer <your-jwt-token>
```

---

## 8. Troubleshooting

### Issue: "Email not sending"
**Solution:**
- Verify Gmail credentials in `application.properties`
- Enable "Less secure app access" or use App Password
- Check SMTP settings (port 587 for TLS)

### Issue: "Database connection failed"
**Solution:**
```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Verify database exists
psql -U postgres -l | grep jobportal1

# Check connection string in application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/jobportal1
```

### Issue: "Frontend can't connect to backend"
**Solution:**
- Verify backend is running on port 8081
- Check `.env` file: `VITE_API_URL=http://localhost:8081/api`
- Check CORS settings (already configured in backend)
- Look at browser console for errors

### Issue: "Token expired"
**Solution:**
- Use refresh token endpoint
- Or login again to get new tokens

### Issue: "OTP verification fails"
**Solution:**
- Check that email and OTP are correct
- OTP expires after 15 minutes (configurable)
- Maximum 3 attempts per OTP
- Use "Resend" button if needed

---

## 9. Development Tips

### Backend Logs
```bash
# View logs in real-time
tail -f app/server/jta/backend.log

# Or check console output
```

### Frontend Dev Tools
```bash
# Check API calls in browser
F12 → Network tab → Filter: Fetch/XHR

# Check console logs
F12 → Console tab
```

### Hot Reload
- **Frontend:** Automatically reloads on file changes
- **Backend:** Use Spring DevTools or restart manually

---

## 10. Production Deployment

### Environment Variables

**Backend:**
```bash
DB_URL=jdbc:postgresql://prod-host:5432/jobportal
DB_USERNAME=prod_user
DB_PASSWORD=secure_password
JWT_SECRET=your-256-bit-production-secret-key-min-32-chars
MAIL_USERNAME=noreply@yourcompany.com
MAIL_PASSWORD=production-app-password
```

**Frontend:**
```bash
VITE_API_URL=https://api.yourcompany.com/api
```

### Build for Production

**Frontend:**
```bash
npm run build
# Output: dist/ folder
# Deploy to Nginx/Apache/CDN
```

**Backend:**
```bash
./mvnw clean package
# Output: target/jta-0.0.1-SNAPSHOT.jar
# Deploy with: java -jar target/jta-0.0.1-SNAPSHOT.jar
```

---

## 11. Default Configuration

| Service | Port | URL |
|---------|------|-----|
| Backend | 8081 | http://localhost:8081 |
| Frontend Dev | 5173 | http://localhost:5173 |
| PostgreSQL | 5432 | localhost:5432 |
| API Base | - | http://localhost:8081/api |
| Swagger UI | - | http://localhost:8081/swagger-ui.html |

---

## 12. Useful Commands

```bash
# Backend
./mvnw clean                    # Clean build artifacts
./mvnw test                     # Run tests
./mvnw spring-boot:run          # Start server

# Frontend  
npm install                     # Install dependencies
npm run dev                     # Development server
npm run build                   # Production build
npm run preview                 # Preview production build

# Database
psql -U postgres jobportal1     # Connect to database
\dt                             # List tables
\d users                        # Describe users table
```

---

## 13. Support & Documentation

- **API Documentation:** `/client/API_ENDPOINTS.md`
- **Fix Summary:** `/client/FIXES_SUMMARY.md`
- **Backend Docs:** `/server/jta/API_USAGE_GUIDE.md`

---

**Application Status:** ✅ Fully functional with all API endpoints aligned

**Last Updated:** 2026-03-31  
**Version:** 1.0.0
