# API Endpoints Reference

**Base URL:** `http://localhost:8081/api`

This document lists all available API endpoints with their correct paths to ensure frontend-backend consistency.

## ✅ Authentication & User Management

### User Registration & Login
- `POST /api/users/register` - Register new user
  - Request: `{ name, email, password, role }`
  - Response: `{ id, name, email, role, createdAt }`
  - **Note:** Automatically triggers email verification

- `POST /api/users/login` - User login
  - Request: `{ email, password }`
  - Response: `{ token, refreshToken, user: {...} }`

- `POST /api/users/refresh-token` - Refresh access token
  - Request: `{ refreshToken }`
  - Response: `{ token, refreshToken, user: {...} }`

### Email Verification
- `POST /api/verification/verify-email` - Verify email with OTP
  - Request: `{ email, otpCode }`
  - Response: `{ success, message, remainingAttempts?, canResend? }`

- `POST /api/verification/resend-verification` - Resend verification code
  - Request: `{ email }`
  - Response: `{ success, message }`

- `GET /api/verification/status/{email}` - Check verification status
  - Response: `{ success, remainingAttempts, canResend, message }`

- `POST /api/users/request-email-verification` - Request email verification (alternate endpoint)
  - Request: `{ email }`
  - Response: `{ message }`

### Password Management
- `POST /api/users/forgot-password` - Request password reset
  - Request: `{ email }`
  - Response: `{ message }`

- `POST /api/verification/verify-password-reset` - Verify password reset OTP
  - Request: `{ email, otpCode }`
  - Response: `{ success, message }`

- `POST /api/users/reset-password` - Reset password
  - Request: `{ token, newPassword }`
  - Response: `{ message }`

### Profile Management
- `GET /api/users/me` - Get current user profile
  - Response: `{ id, name, email, role, ... }`

- `PATCH /api/users/me/profile` - Update profile
  - Request: `{ name?, phone?, bio?, ... }`
  - Response: Updated user object

- `PATCH /api/users/me/password` - Change password
  - Request: `{ currentPassword, newPassword }`
  - Response: `{ message }`

- `GET /api/users/{id}` - Get user by ID
- `GET /api/users` - Get all users (admin only)

---

## 💼 Jobs Management

- `GET /api/jobs` - Get open jobs (with filters)
  - Query params: `page, size, search, location, type`
  - Response: Paginated job list

- `GET /api/jobs/{jobId}` - Get job details
- `POST /api/jobs` - Create job posting (recruiter only)
- `GET /api/jobs/recruiter` - Get recruiter's jobs (paginated)
- `GET /api/jobs/all` - Get all jobs (admin only)
- `PATCH /api/jobs/{jobId}/close` - Close job posting

---

## 📝 Job Applications

- `POST /api/applications/jobs/{jobId}/apply` - Submit application
  - Request: `{ resumeId, coverLetter }`
  
- `GET /api/applications/jobs/{jobId}` - Get applications for a job (recruiter)
  - Query params: `page, size`

- `GET /api/applications/me/applications` - Get my applications (candidate)
  - Query params: `page, size`

- `PATCH /api/applications/{applicationId}/status` - Update application status
  - Request: `{ status, notes? }`

---

## 📄 Resume Management

- `POST /api/resumes` - Upload resume
  - Content-Type: `multipart/form-data`
  - Fields: `file, label?`

- `GET /api/resumes` - Get my resumes
- `GET /api/resumes/{resumeId}` - Get resume details
- `GET /api/resumes/{resumeId}/download` - Download resume file
- `PATCH /api/resumes/{resumeId}/default` - Set as default resume
- `PATCH /api/resumes/{resumeId}/label` - Update resume label
- `DELETE /api/resumes/{resumeId}` - Delete resume

---

## 🏢 Company Management

- `POST /api/companies` - Create company
- `GET /api/companies` - Get all companies
- `GET /api/companies/{companyId}` - Get company details
- `POST /api/companies/{companyId}/recruiters/{userId}` - Assign recruiter
- `POST /api/companies/{companyId}/assign-me` - Assign current user

---

## 🤖 AI Services

- `POST /api/ai/analyze-resume` - Analyze resume for target role
  - Request: `{ resumeText, targetRole }`

- `POST /api/ai/interview-prep` - Generate interview questions
  - Request: `{ jobTitle, jobDescription }`

- `POST /api/ai/skill-suggestions` - Get skill suggestions
  - Request: `{ currentRole, targetRole }`

---

## 📊 Analytics

- `GET /api/analytics/candidate` - Get candidate analytics
  - Response: Application stats, trends, recommendations

- `GET /api/analytics/recruiter` - Get recruiter analytics
  - Response: Job performance, application metrics

---

## 🔔 Notifications

- `GET /api/notifications` - Get notifications (paginated)
  - Query params: `page, size`

- `GET /api/notifications/unread` - Get unread notifications
- `GET /api/notifications/unread/count` - Get unread count
- `PATCH /api/notifications/{notificationId}/read` - Mark as read
- `PATCH /api/notifications/read-all` - Mark all as read

---

## 👨‍💼 Admin Operations

- `GET /api/admin/stats` - Get platform statistics
- `GET /api/admin/users` - Get users (with filters)
  - Query params: `page, size, search, role`

- `PATCH /api/admin/users/role` - Update user role
  - Request: `{ userId, role }`

- `PATCH /api/admin/users/{userId}/toggle-status` - Toggle user status
- `DELETE /api/admin/users/{userId}` - Delete user

---

## 🔒 Authentication Headers

All authenticated endpoints require:
```
Authorization: Bearer <JWT_TOKEN>
```

Token is stored in localStorage as `authToken` and automatically attached by the API service.

---

## 📝 Notes

### User Roles
- `CANDIDATE` - Job seekers
- `RECRUITER` - Job posters
- `ADMIN` - Platform administrators

### Application Status Flow
`PENDING` → `UNDER_REVIEW` → `INTERVIEW_SCHEDULED` → `ACCEPTED` / `REJECTED`

### JWT Token Lifetimes
- **Access Token:** 1 hour (3600000ms)
- **Refresh Token:** 7 days (604800000ms)

### Rate Limits
- **Default:** 60 requests/minute
- **Login:** 5 attempts/minute
- **AI Services:** 20 requests/hour

### File Upload Limits
- **Max file size:** 10MB
- **Supported formats:** PDF, DOC, DOCX (for resumes)

---

## 🐛 Common Issues & Solutions

### Issue: "Email verification failed - API address mismatch"
**Solution:** The frontend now correctly uses `/api/verification/verify-email` endpoint.

### Issue: "Profile update failed"
**Solution:** Frontend now uses `PATCH /api/users/me/profile` instead of `PUT /api/users/profile`.

### Issue: "Token expired"
**Solution:** Use the refresh token endpoint: `POST /api/users/refresh-token`

---

## 🔄 Changelog

### 2026-03-31 - API Endpoints Fixed
- ✅ Fixed profile endpoint: `/users/profile` → `/users/me`
- ✅ Fixed profile update: `PUT` → `PATCH` to `/users/me/profile`
- ✅ Added change password endpoint: `PATCH /users/me/password`
- ✅ Verified email verification endpoints are correct
- ✅ Added comprehensive API methods for all backend endpoints
- ✅ Added missing endpoints: resumes, applications, companies, AI, analytics, notifications, admin

---

**Last Updated:** 2026-03-31  
**Backend Version:** Spring Boot 3.5.10  
**Frontend Version:** React 18.3.1 + Vite 6.3.5
