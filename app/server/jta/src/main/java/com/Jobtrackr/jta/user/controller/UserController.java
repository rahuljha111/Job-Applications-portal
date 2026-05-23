package com.Jobtrackr.jta.user.controller;

import com.Jobtrackr.jta.user.dto.AuthUser;
import com.Jobtrackr.jta.user.dto.ChangePasswordRequest;
import com.Jobtrackr.jta.user.dto.ForgotPasswordRequest;
import com.Jobtrackr.jta.user.dto.LoginRequest;
import com.Jobtrackr.jta.user.dto.LoginResponse;
import com.Jobtrackr.jta.user.dto.RegisterRequest;
import com.Jobtrackr.jta.user.dto.RequestEmailVerificationRequest;
import com.Jobtrackr.jta.user.dto.ResetPasswordRequest;
import com.Jobtrackr.jta.user.dto.UpdateProfileRequest;
import com.Jobtrackr.jta.user.dto.UserResponse;
import com.Jobtrackr.jta.user.dto.VerifyEmailRequest;
import com.Jobtrackr.jta.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.getEmail());
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUser> getMe(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getCurrentUser(auth.getName()));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<AuthUser> updateProfile(@Valid @RequestBody UpdateProfileRequest request, Authentication auth) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "If an account exists with this email, you will receive a recovery link."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password has been reset. You can now sign in."));
    }

    @PostMapping("/request-email-verification")
    public ResponseEntity<Map<String, String>> requestEmailVerification(@Valid @RequestBody RequestEmailVerificationRequest request) {
        userService.requestEmailVerification(request);
        return ResponseEntity.ok(Map.of("message", "If an account exists with this email, a verification code has been sent."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        userService.verifyEmail(request);
        return ResponseEntity.ok(Map.of("message", "Email verified. You can now sign in."));
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userService.refreshToken(refreshToken));
    }
}
