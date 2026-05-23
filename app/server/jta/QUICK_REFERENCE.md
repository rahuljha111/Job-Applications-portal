# Quick Reference - Job Application Portal API

## 🚀 Application Status
✅ Running on: http://localhost:8081
✅ Database: Connected (PostgreSQL)
✅ Email: Configured (Gmail SMTP)
✅ Verification: ACTIVE

## 🔑 Valid User Roles
```
CANDIDATE  - Job seekers (default)
RECRUITER  - Company recruiters
ADMIN      - System administrators
```

## 📝 Quick Start - Registration Flow

### Step 1: Register
```bash
POST /api/users/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "role": "CANDIDATE"
}
```

### Step 2: Verify Email
Check email for 6-digit code, then:
```bash
POST /api/verification/verify-email
{
  "email": "john@example.com",
  "otpCode": "123456"
}
```

### Step 3: Login
```bash
POST /api/users/login
{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

### Step 4: Use Token
```bash
GET /api/users/me
Headers: Authorization: Bearer YOUR_TOKEN
```

## ⚠️ Common Errors

### ❌ WRONG: Using "JOB_SEEKER"
```json
{"role": "JOB_SEEKER"}  // Invalid!
```

### ✅ CORRECT: Use "CANDIDATE"
```json
{"role": "CANDIDATE"}  // Valid!
```

### ❌ WRONG: Login without verification
```
Error 403: Email not verified
```

### ✅ CORRECT: Verify email first
```
1. Register → 2. Verify Email → 3. Login
```

## 🔒 Password Requirements
- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 number
- At least 1 special character

Valid: `SecurePass123!`
Invalid: `password123`

## 📧 Email Verification
- Code expires in: 10 minutes
- Max attempts: 3 per code
- Daily limit: 5 requests per email
- Can resend via: `/api/verification/resend-verification`

## 🌐 Endpoints Quick Reference

### Public (No Auth Required)
```
POST   /api/users/register
POST   /api/users/login
POST   /api/users/refresh-token
POST   /api/users/forgot-password
POST   /api/users/reset-password
POST   /api/verification/verify-email
POST   /api/verification/resend-verification
GET    /api/jobs
GET    /api/jobs/{id}
```

### Authenticated (JWT Required)
```
GET    /api/users/me
PATCH  /api/users/me/profile
PATCH  /api/users/me/password
```

### Role-Based
```
CANDIDATE:  POST /api/applications/jobs/{id}/apply
RECRUITER:  POST /api/jobs
ADMIN:      GET  /api/users
```

## 🔧 Rate Limits
- General API: 60 req/min
- Login: 5 attempts/min
- AI: 20 req/hour
- Email: 5 req/day

## 📊 HTTP Status Codes
```
200 ✅ Success
201 ✅ Created (registration)
400 ❌ Bad Request (invalid JSON)
401 ❌ Unauthorized (no/invalid token)
403 ❌ Forbidden (email not verified)
404 ❌ Not Found
409 ❌ Conflict (email exists)
429 ❌ Too Many Requests
500 ❌ Server Error
```

## 🐛 Debugging
```powershell
# Check if app is running
Invoke-WebRequest http://localhost:8081/api/jobs

# View logs
tail -f backend.log

# Test registration
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@test.com","password":"Test123!","role":"CANDIDATE"}'
```

## 📱 Frontend Auth Flow
```javascript
// 1. Register
const response = await fetch('/api/users/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    name: 'John',
    email: 'john@example.com',
    password: 'Pass123!',
    role: 'CANDIDATE'
  })
});

// 2. Verify Email (use code from email)
await fetch('/api/verification/verify-email', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'john@example.com',
    otpCode: '123456'
  })
});

// 3. Login
const loginRes = await fetch('/api/users/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'john@example.com',
    password: 'Pass123!'
  })
});

const { token, refreshToken, user } = await loginRes.json();

// 4. Use token for requests
fetch('/api/users/me', {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

## 📚 Documentation Files
- `PRODUCTION_SETUP.md` - Complete setup guide
- `API_USAGE_GUIDE.md` - Detailed API documentation

## ✅ Testing Checklist
- [ ] Register with CANDIDATE role
- [ ] Check email for verification code
- [ ] Verify email with code
- [ ] Try login before verification (should fail)
- [ ] Try login after verification (should succeed)
- [ ] Access protected endpoint with token
- [ ] Test invalid role (should fail)
- [ ] Test duplicate email (should fail)

---
**Last Updated:** 2026-03-31
**Application:** Job Application Portal v0.0.1
