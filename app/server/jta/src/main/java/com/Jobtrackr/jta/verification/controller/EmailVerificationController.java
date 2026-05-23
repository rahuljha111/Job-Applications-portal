package com.Jobtrackr.jta.verification.controller;

import com.Jobtrackr.jta.verification.dto.ResendVerificationRequest;
import com.Jobtrackr.jta.verification.dto.VerificationResponse;
import com.Jobtrackr.jta.verification.dto.VerifyOTPRequest;
import com.Jobtrackr.jta.verification.entity.EmailVerification;
import com.Jobtrackr.jta.verification.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationController {
    
    private final EmailVerificationService verificationService;
    
    @PostMapping("/verify-email")
    public ResponseEntity<VerificationResponse> verifyEmail(@Valid @RequestBody VerifyOTPRequest request) {
        try {
            boolean success = verificationService.verifyOTP(
                request.getEmail(), 
                request.getOtpCode(), 
                EmailVerification.VerificationType.REGISTRATION
            );
            
            if (success) {
                VerificationResponse response = VerificationResponse.builder()
                        .success(true)
                        .message("Email verified successfully")
                        .build();
                return ResponseEntity.ok(response);
            } else {
                int remainingAttempts = verificationService.getRemainingAttempts(
                    request.getEmail(), 
                    EmailVerification.VerificationType.REGISTRATION
                );
                
                VerificationResponse response = VerificationResponse.builder()
                        .success(false)
                        .message("Invalid verification code")
                        .remainingAttempts(remainingAttempts)
                        .canResend(remainingAttempts == 0)
                        .build();
                        
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("Email verification error: {}", e.getMessage());
            VerificationResponse response = VerificationResponse.builder()
                    .success(false)
                    .message("Verification failed. Please try again.")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<VerificationResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        try {
            verificationService.resendVerificationCode(
                request.getEmail(), 
                EmailVerification.VerificationType.REGISTRATION
            );
            
            VerificationResponse response = VerificationResponse.builder()
                    .success(true)
                    .message("Verification code sent to your email")
                    .build();
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            VerificationResponse response = VerificationResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (IllegalArgumentException e) {
            VerificationResponse response = VerificationResponse.builder()
                    .success(false)
                    .message("User not found")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            
        } catch (Exception e) {
            log.error("Resend verification error: {}", e.getMessage());
            VerificationResponse response = VerificationResponse.builder()
                    .success(false)
                    .message("Failed to send verification code. Please try again later.")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/verify-password-reset")
    public ResponseEntity<VerificationResponse> verifyPasswordReset(@Valid @RequestBody VerifyOTPRequest request) {
        try {
            boolean success = verificationService.verifyOTP(
                request.getEmail(), 
                request.getOtpCode(), 
                EmailVerification.VerificationType.PASSWORD_RESET
            );
            
            if (success) {
                VerificationResponse response = VerificationResponse.builder()
                        .success(true)
                        .message("Code verified successfully. You can now reset your password.")
                        .build();
                return ResponseEntity.ok(response);
            } else {
                int remainingAttempts = verificationService.getRemainingAttempts(
                    request.getEmail(), 
                    EmailVerification.VerificationType.PASSWORD_RESET
                );
                
                VerificationResponse response = VerificationResponse.builder()
                        .success(false)
                        .message("Invalid verification code")
                        .remainingAttempts(remainingAttempts)
                        .canResend(remainingAttempts == 0)
                        .build();
                        
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("Password reset verification error: {}", e.getMessage());
            VerificationResponse response = VerificationResponse.builder()
                    .success(false)
                    .message("Verification failed. Please try again.")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/status/{email}")
    public ResponseEntity<VerificationResponse> getVerificationStatus(@PathVariable String email) {
        try {
            boolean hasValidVerification = verificationService.hasValidVerification(
                email, 
                EmailVerification.VerificationType.REGISTRATION
            );
            
            int remainingAttempts = verificationService.getRemainingAttempts(
                email, 
                EmailVerification.VerificationType.REGISTRATION
            );
            
            VerificationResponse response = VerificationResponse.builder()
                    .success(hasValidVerification)
                    .remainingAttempts(remainingAttempts)
                    .canResend(!hasValidVerification || remainingAttempts == 0)
                    .message(hasValidVerification ? "Verification pending" : "No active verification")
                    .build();
                    
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Get verification status error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}