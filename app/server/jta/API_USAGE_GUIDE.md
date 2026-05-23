# API Usage Guide - Common Errors & Solutions

## ❌ Common Errors Found in Console

### Error 1: Invalid Role Value
```
ERROR: Cannot deserialize value of type `Role` from String "JOB_SEEKER": 
not one of the values accepted for Enum class: [RECRUITER, ADMIN, CANDIDATE]
```

**Cause:** Using incorrect role value in registration

**Solution:** Use only these valid roles:
- `CANDIDATE` - For job seekers (default)
- `RECRUITER` - For company recruiters
- `ADMIN` - For system administrators

**Correct Example:**
```json
POST /api/users/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "role": "CANDIDATE"
}
```

**WRONG Example:**
```json
{
  "role": "JOB_SEEKER"  // ❌ WRONG - Use "CANDIDATE" instead
}
```

---

### Error 2: JSON Parsing Error
```
ERROR: Cannot deserialize value of type `String` from Object value
```

**Cause:** Sending incorrect JSON structure or nested objects where strings are expected

**Common Mistakes:**
```json
// ❌ WRONG - Nested object
{
  "email": { "value": "test@example.com" },
  "password": "test123"
}

// ✅ CORRECT - Flat structure
{
  "email": "test@example.com",
  "password": "test123"
}
```

---

### Error 3: PageImpl Serialization Warning
```
WARN: Serializing PageImpl instances as-is is not supported
```

**Cause:** Spring Data pagination response structure

**Impact:** Non-critical warning, pagination still works

**Note:** This is a framework-level warning and doesn't affect functionality

---

## ✅ Correct API Usage Examples

### 1. User Registration (CANDIDATE)

```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "email": "jane@example.com",
    "password": "SecurePassword123!",
    "role": "CANDIDATE"
  }'
```

**Expected Response:**
```json
{
  "id": "7e8ce2ff-32c7-400a-b328-c27d43ee4a6a",
  "name": "Jane Doe",
  "email": "jane@example.com",
  "role": "CANDIDATE",
  "active": true
}
```

**What Happens Next:**
1. User receives verification email with 6-digit code
2. User must verify email before login
3. Verification code expires in 10 minutes

---

### 2. User Registration (RECRUITER)

```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Recruiter Name",
    "email": "recruiter@company.com",
    "password": "SecurePassword123!",
    "role": "RECRUITER"
  }'
```

---

### 3. Email Verification

```bash
curl -X POST http://localhost:8081/api/verification/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane@example.com",
    "otpCode": "123456"
  }'
```

**Expected Response (Success):**
```json
{
  "success": true,
  "message": "Email verified successfully"
}
```

**Expected Response (Failure):**
```json
{
  "success": false,
  "message": "Invalid verification code",
  "remainingAttempts": 2,
  "canResend": false
}
```

---

### 4. Login

```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane@example.com",
    "password": "SecurePassword123!"
  }'
```

**Expected Response (Success):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "7e8ce2ff-32c7-400a-b328-c27d43ee4a6a",
    "name": "Jane Doe",
    "email": "jane@example.com",
    "role": "CANDIDATE",
    "location": null
  }
}
```

**Expected Response (Email Not Verified):**
```json
{
  "message": "Email not verified. Please check your email for verification code.",
  "status": 403,
  "timestamp": "2026-03-31T09:10:00.000Z"
}
```

**Expected Response (Invalid Credentials):**
```json
{
  "message": "Invalid password",
  "status": 409,
  "timestamp": "2026-03-31T09:10:00.000Z"
}
```

---

### 5. Authenticated Requests

After login, include the JWT token in the Authorization header:

```bash
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**
```json
{
  "id": "7e8ce2ff-32c7-400a-b328-c27d43ee4a6a",
  "name": "Jane Doe",
  "email": "jane@example.com",
  "role": "CANDIDATE",
  "location": null
}
```

---

### 6. Resend Verification Code

If verification code expired or wasn't received:

```bash
curl -X POST http://localhost:8081/api/verification/resend-verification \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane@example.com"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Verification code sent to your email"
}
```

---

## 🔧 Password Requirements

Passwords must meet the following criteria:
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)

**Valid Examples:**
- `SecurePass123!`
- `MyP@ssw0rd`
- `Test1234!`

**Invalid Examples:**
- `password` (too simple)
- `12345678` (no letters)
- `TestTest` (no numbers or special chars)

---

## 📝 Request Validation Errors

### Missing Required Fields
```json
{
  "message": "Validation failed",
  "errors": {
    "email": "Email is required",
    "password": "Password is required"
  },
  "status": 400
}
```

### Invalid Email Format
```json
{
  "message": "Validation failed",
  "errors": {
    "email": "Email should be valid"
  },
  "status": 400
}
```

### Duplicate Email
```json
{
  "message": "Email already exists",
  "status": 409
}
```

---

## 🔒 Rate Limiting

The API has rate limits to prevent abuse:

- **General endpoints:** 60 requests per minute
- **Login endpoint:** 5 attempts per minute
- **AI endpoints:** 20 requests per hour
- **Email verification:** 5 requests per day per email

**Rate Limit Response:**
```json
{
  "message": "Rate limit exceeded. Please try again later.",
  "status": 429
}
```

**Rate Limit Headers:**
```
X-Rate-Limit-Remaining: 3
X-Rate-Limit-Retry-After-Seconds: 45
```

---

## 🌐 CORS Configuration

The API allows requests from these origins:
- `http://localhost:5173` to `http://localhost:5180`
- `http://localhost:3000`
- `https://applications-portal.netlify.app`

If you get a CORS error, ensure your frontend is running on one of these ports.

---

## 📊 HTTP Status Codes

| Code | Meaning | Common Causes |
|------|---------|---------------|
| 200 | Success | Request processed successfully |
| 201 | Created | User registered successfully |
| 400 | Bad Request | Invalid JSON or validation error |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Email not verified or insufficient permissions |
| 404 | Not Found | User or resource doesn't exist |
| 409 | Conflict | Email already exists or invalid password |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Server Error | Unexpected server error (check logs) |

---

## 🐛 Debugging Tips

### 1. Check Request Format
Ensure your JSON is valid:
```bash
# Use a JSON validator
echo '{"email":"test@example.com"}' | python -m json.tool
```

### 2. View Application Logs
```bash
# Check recent errors
tail -f backend.log

# Or check detached logs
Get-Content C:\Users\rahul\AppData\Local\Temp\copilot-detached-*.log | Select-Object -Last 50
```

### 3. Test with Curl First
Before using your application, test endpoints with curl to isolate issues:
```bash
curl -v -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@test.com","password":"Test123!","role":"CANDIDATE"}'
```

### 4. Common Mistakes

**❌ Wrong Content-Type:**
```javascript
// WRONG
fetch('/api/users/login', {
  method: 'POST',
  body: { email: 'test@test.com', password: 'test' }
})

// CORRECT
fetch('/api/users/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email: 'test@test.com', password: 'test' })
})
```

**❌ Wrong Role Value:**
```javascript
// WRONG
role: "JOB_SEEKER"

// CORRECT
role: "CANDIDATE"
```

**❌ Missing Authorization Header:**
```javascript
// WRONG
fetch('/api/users/me')

// CORRECT
fetch('/api/users/me', {
  headers: { 'Authorization': `Bearer ${token}` }
})
```

---

## 📱 Frontend Integration Examples

### React Example

```javascript
// Registration
const register = async (userData) => {
  try {
    const response = await fetch('http://localhost:8081/api/users/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: userData.name,
        email: userData.email,
        password: userData.password,
        role: "CANDIDATE" // Must be uppercase
      })
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message);
    }

    return await response.json();
  } catch (error) {
    console.error('Registration failed:', error);
    throw error;
  }
};

// Verify Email
const verifyEmail = async (email, otpCode) => {
  try {
    const response = await fetch('http://localhost:8081/api/verification/verify-email', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, otpCode })
    });

    const data = await response.json();
    
    if (!data.success) {
      throw new Error(data.message);
    }

    return data;
  } catch (error) {
    console.error('Verification failed:', error);
    throw error;
  }
};

// Login
const login = async (email, password) => {
  try {
    const response = await fetch('http://localhost:8081/api/users/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    if (!response.ok) {
      const error = await response.json();
      
      if (response.status === 403) {
        // Email not verified
        throw new Error('EMAIL_NOT_VERIFIED');
      }
      
      throw new Error(error.message);
    }

    const data = await response.json();
    
    // Store tokens
    localStorage.setItem('accessToken', data.token);
    localStorage.setItem('refreshToken', data.refreshToken);
    localStorage.setItem('user', JSON.stringify(data.user));

    return data;
  } catch (error) {
    console.error('Login failed:', error);
    throw error;
  }
};

// Make authenticated requests
const fetchWithAuth = async (url, options = {}) => {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });

  // Handle token expiration
  if (response.status === 401) {
    // Try to refresh token
    const refreshToken = localStorage.getItem('refreshToken');
    const refreshResponse = await fetch('http://localhost:8081/api/users/refresh-token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });

    if (refreshResponse.ok) {
      const data = await refreshResponse.json();
      localStorage.setItem('accessToken', data.token);
      localStorage.setItem('refreshToken', data.refreshToken);
      
      // Retry original request
      return fetchWithAuth(url, options);
    } else {
      // Refresh failed, logout
      localStorage.clear();
      window.location.href = '/login';
    }
  }

  return response;
};
```

---

## 🎯 Testing Checklist

Before integrating with frontend:

- [ ] Test registration with valid CANDIDATE role
- [ ] Test registration with valid RECRUITER role
- [ ] Test registration with invalid JOB_SEEKER role (should fail)
- [ ] Check email inbox for verification code
- [ ] Test email verification with correct code
- [ ] Test email verification with wrong code (should fail)
- [ ] Test login without email verification (should fail)
- [ ] Test login after email verification (should succeed)
- [ ] Test accessing protected endpoints with JWT
- [ ] Test token refresh functionality
- [ ] Test password strength validation
- [ ] Test duplicate email registration (should fail)

---

## 📞 Need Help?

If you encounter errors:

1. Check this guide for common solutions
2. Review console logs for detailed error messages
3. Verify your request format matches the examples
4. Ensure you're using correct role values
5. Check that email verification is complete before login

**Remember:** The application is working correctly. Most errors are due to incorrect API usage from the client side.

---

**Last Updated:** 2026-03-31
