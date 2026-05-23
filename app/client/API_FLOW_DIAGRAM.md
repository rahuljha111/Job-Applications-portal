# API Flow Diagram - Registration & Email Verification

## ✅ CORRECTED FLOW (After Fixes)

```
┌─────────────────────────────────────────────────────────────────┐
│                    USER REGISTRATION FLOW                        │
└─────────────────────────────────────────────────────────────────┘

1. USER CLICKS "SIGN UP"
   │
   ├─→ Frontend: Auth.tsx
   │   ├─→ Collects: firstName, lastName, email, password, role
   │   └─→ Calls: apiService.register()
   │
   ├─→ API Service: api.ts
   │   ├─→ Transforms to: { name, email, password, role }
   │   └─→ POST http://localhost:8081/api/users/register
   │
   ├─→ Backend: UserController.java
   │   ├─→ Route: POST /api/users/register
   │   ├─→ Validates data
   │   ├─→ Creates user in database (emailVerified = false)
   │   ├─→ Generates 6-digit OTP
   │   ├─→ Saves to email_verifications table
   │   └─→ Sends OTP via Gmail SMTP ✅
   │
   └─→ Response: 201 Created
       └─→ { id, name, email, role, createdAt }

2. EMAIL ARRIVES IN INBOX ✅
   │
   ├─→ Subject: "Email Verification Code - Job Tracker"
   ├─→ Body: "Your verification code is: 123456"
   └─→ Valid for: 15 minutes

3. USER REDIRECTED TO VERIFICATION PAGE
   │
   ├─→ Route: /verify-email?email=user@example.com
   └─→ Component: EmailVerification.tsx

4. USER ENTERS OTP
   │
   ├─→ Frontend: EmailVerification.tsx
   │   ├─→ 6-digit input fields
   │   └─→ Calls: apiService.verifyEmail(email, otp)
   │
   ├─→ API Service: api.ts
   │   └─→ POST http://localhost:8081/api/verification/verify-email ✅
   │       └─→ Body: { email, otpCode }
   │
   ├─→ Backend: EmailVerificationController.java ✅
   │   ├─→ Route: POST /api/verification/verify-email
   │   ├─→ Validates OTP (max 3 attempts)
   │   ├─→ Checks expiration (15 min)
   │   ├─→ Updates user: emailVerified = true
   │   └─→ Marks verification as used
   │
   └─→ Response: 200 OK
       └─→ { success: true, message: "Email verified successfully" }

5. VERIFICATION SUCCESS
   │
   ├─→ Frontend redirects to: /auth?verified=true
   └─→ User can now login ✅

┌─────────────────────────────────────────────────────────────────┐
│                    RESEND OTP FLOW                               │
└─────────────────────────────────────────────────────────────────┘

USER CLICKS "RESEND"
   │
   ├─→ Frontend: EmailVerification.tsx
   │   └─→ Calls: apiService.resendVerification(email)
   │
   ├─→ API Service: api.ts
   │   └─→ POST http://localhost:8081/api/verification/resend-verification ✅
   │       └─→ Body: { email }
   │
   ├─→ Backend: EmailVerificationController.java ✅
   │   ├─→ Route: POST /api/verification/resend-verification
   │   ├─→ Checks rate limiting (max 5 per day)
   │   ├─→ Generates new OTP
   │   └─→ Sends new email ✅
   │
   └─→ Response: 200 OK
       └─→ { success: true, message: "Verification code sent" }

┌─────────────────────────────────────────────────────────────────┐
│                    LOGIN FLOW                                    │
└─────────────────────────────────────────────────────────────────┘

USER LOGS IN
   │
   ├─→ Frontend: Auth.tsx
   │   └─→ Calls: apiService.login(email, password)
   │
   ├─→ API Service: api.ts
   │   └─→ POST http://localhost:8081/api/users/login ✅
   │       └─→ Body: { email, password }
   │
   ├─→ Backend: UserController.java
   │   ├─→ Route: POST /api/users/login
   │   ├─→ Validates credentials
   │   ├─→ Checks emailVerified = true ✅
   │   ├─→ Generates JWT tokens
   │   └─→ Returns tokens + user data
   │
   └─→ Response: 200 OK
       └─→ {
             token: "eyJhbGc...",
             refreshToken: "eyJhbGc...",
             user: { id, name, email, role }
           }

FRONTEND STORES
   │
   ├─→ localStorage.setItem("authToken", token)
   ├─→ localStorage.setItem("refreshToken", refreshToken)
   ├─→ localStorage.setItem("userData", JSON.stringify(user))
   └─→ Redirects to dashboard ✅

┌─────────────────────────────────────────────────────────────────┐
│                    PROFILE MANAGEMENT FLOW                       │
└─────────────────────────────────────────────────────────────────┘

GET PROFILE
   │
   ├─→ Frontend: ProfilePage.tsx
   │   └─→ Calls: apiService.getUserProfile()
   │
   ├─→ API Service: api.ts
   │   └─→ GET http://localhost:8081/api/users/me ✅ FIXED
   │       └─→ Headers: { Authorization: "Bearer <token>" }
   │
   ├─→ Backend: UserController.java
   │   ├─→ Route: GET /api/users/me ✅
   │   ├─→ Validates JWT token
   │   └─→ Returns user data
   │
   └─→ Response: 200 OK
       └─→ { id, name, email, role, ... }

UPDATE PROFILE
   │
   ├─→ Frontend: ProfilePage.tsx
   │   └─→ Calls: apiService.updateUserProfile(data)
   │
   ├─→ API Service: api.ts
   │   └─→ PATCH http://localhost:8081/api/users/me/profile ✅ FIXED
   │       └─→ Body: { name, phone, bio, ... }
   │
   ├─→ Backend: UserController.java
   │   ├─→ Route: PATCH /api/users/me/profile ✅
   │   ├─→ Validates JWT token
   │   └─→ Updates user data
   │
   └─→ Response: 200 OK
       └─→ Updated user object

```

## 🔴 PREVIOUS ISSUES (Before Fixes)

```
❌ ISSUE 1: Profile Endpoint Mismatch
   Frontend: GET /api/users/profile
   Backend:  GET /api/users/me
   Result:   404 Not Found

❌ ISSUE 2: Update Profile Method Mismatch
   Frontend: PUT /api/users/profile
   Backend:  PATCH /api/users/me/profile
   Result:   404 Not Found / 405 Method Not Allowed
```

## ✅ FIXES APPLIED

```
✅ FIX 1: Updated API.USERS.PROFILE
   Old: "/users/profile"
   New: "/users/me"

✅ FIX 2: Updated API.USERS.UPDATE_PROFILE
   Old: PUT to "/users/profile"
   New: PATCH to "/users/me/profile"

✅ FIX 3: Added API.USERS.CHANGE_PASSWORD
   New: PATCH to "/users/me/password"

✅ FIX 4: Added comprehensive API methods
   - All CRUD operations
   - File uploads
   - AI services
   - Analytics
   - Notifications
   - Admin operations
```

## 📊 VERIFICATION STATUS

```
Component                Status
────────────────────────────────────────
Registration            ✅ Working
Email Sending           ✅ Working
Email Verification      ✅ Working (verified correct endpoint)
Resend Verification     ✅ Working (verified correct endpoint)
Login                   ✅ Working
Token Management        ✅ Working
Get Profile             ✅ Fixed (now uses /users/me)
Update Profile          ✅ Fixed (now uses PATCH /users/me/profile)
Change Password         ✅ Added
All Other Endpoints     ✅ Added and aligned
```

## 🎯 ENDPOINT MAPPING SUMMARY

| Frontend Method | HTTP | Endpoint | Backend Controller |
|----------------|------|----------|-------------------|
| register() | POST | /users/register | UserController ✅ |
| login() | POST | /users/login | UserController ✅ |
| verifyEmail() | POST | /verification/verify-email | EmailVerificationController ✅ |
| resendVerification() | POST | /verification/resend-verification | EmailVerificationController ✅ |
| getUserProfile() | GET | /users/me | UserController ✅ FIXED |
| updateUserProfile() | PATCH | /users/me/profile | UserController ✅ FIXED |
| changePassword() | PATCH | /users/me/password | UserController ✅ ADDED |

**All endpoints now correctly aligned! 🎉**
