package com.Jobtrackr.jta.verification.service;

import com.Jobtrackr.jta.user.entity.User;
import com.Jobtrackr.jta.user.repository.UserRepository;
import com.Jobtrackr.jta.verification.entity.EmailVerification;
import com.Jobtrackr.jta.verification.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
    
    private final EmailVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Value("${app.verification.max-daily-requests:5}")
    private int maxDailyRequests;
    
    @Transactional
    public void sendVerificationEmail(String email, String userName, EmailVerification.VerificationType type) {
        // Check daily limit
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
        Long recentRequests = verificationRepository.countRecentVerificationsByEmail(email, oneDayAgo);
        
        if (recentRequests >= maxDailyRequests) {
            throw new IllegalStateException("Daily verification request limit exceeded. Please try again later.");
        }
        
        // Invalidate existing verification codes for this email and type
        verificationRepository.deleteByEmailAndType(email, type);
        
        // Generate new OTP
        String otpCode = generateOTP();
        
        // Create verification record
        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .otpCode(otpCode)
                .type(type)
                .build();
        
        verificationRepository.save(verification);
        
        // Send email asynchronously
        sendVerificationEmailAsync(email, otpCode, userName, type);
        
        log.info("Verification email requested for: {} (type: {})", email, type);
    }
    
    @Async
    public void sendVerificationEmailAsync(String email, String otpCode, String userName, EmailVerification.VerificationType type) {
        try {
            switch (type) {
                case REGISTRATION -> emailService.sendVerificationEmail(email, otpCode, userName);
                case PASSWORD_RESET -> emailService.sendPasswordResetEmail(email, otpCode, userName);
                case EMAIL_CHANGE -> emailService.sendVerificationEmail(email, otpCode, userName);
            }
        } catch (Exception e) {
            log.error("Failed to send verification email: {}", e.getMessage());
            // Could add retry logic here or notification to admin
        }
    }
    
    @Transactional
    public boolean verifyOTP(String email, String otpCode, EmailVerification.VerificationType type) {
        Optional<EmailVerification> verificationOpt = verificationRepository
                .findByEmailAndOtpCodeAndTypeAndVerifiedFalse(email, otpCode, type);
        
        if (verificationOpt.isEmpty()) {
            log.warn("Invalid OTP attempt for email: {} (type: {})", email, type);
            return false;
        }
        
        EmailVerification verification = verificationOpt.get();
        
        // Increment attempts
        verification.incrementAttempts();
        
        if (!verification.canAttempt()) {
            verificationRepository.save(verification);
            
            if (verification.isExpired()) {
                log.warn("Expired OTP attempt for email: {} (type: {})", email, type);
            } else if (verification.isMaxAttemptsReached()) {
                log.warn("Max attempts reached for email: {} (type: {})", email, type);
            }
            return false;
        }
        
        // Mark as verified
        verification.setVerified(true);
        verificationRepository.save(verification);
        
        // If registration verification, update user
        if (type == EmailVerification.VerificationType.REGISTRATION) {
            updateUserEmailVerification(email);
        }
        
        log.info("Email verification successful for: {} (type: {})", email, type);
        return true;
    }
    
    @Transactional
    public void resendVerificationCode(String email, EmailVerification.VerificationType type) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }
        
        User user = userOpt.get();
        
        // Check if already verified
        if (type == EmailVerification.VerificationType.REGISTRATION && user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }
        
        sendVerificationEmail(email, user.getName(), type);
    }
    
    public boolean hasValidVerification(String email, EmailVerification.VerificationType type) {
        Optional<EmailVerification> verification = verificationRepository
                .findByEmailAndTypeAndVerifiedFalse(email, type);
        
        return verification.map(EmailVerification::canAttempt).orElse(false);
    }
    
    public int getRemainingAttempts(String email, EmailVerification.VerificationType type) {
        Optional<EmailVerification> verification = verificationRepository
                .findByEmailAndTypeAndVerifiedFalse(email, type);
        
        return verification.map(v -> v.getMaxAttempts() - v.getAttempts()).orElse(0);
    }
    
    private void updateUserEmailVerification(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setEmailVerified(true);
            userRepository.save(user);
            
            // Send welcome email
            try {
                emailService.sendWelcomeEmail(email, user.getName());
            } catch (Exception e) {
                log.warn("Failed to send welcome email to: {} - {}", email, e.getMessage());
            }
        });
    }
    
    private String generateOTP() {
        // Generate 6-digit OTP
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }
    
    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanupExpiredVerifications() {
        LocalDateTime now = LocalDateTime.now();
        verificationRepository.deleteExpiredVerifications(now);
        log.debug("Cleaned up expired email verifications");
    }
}