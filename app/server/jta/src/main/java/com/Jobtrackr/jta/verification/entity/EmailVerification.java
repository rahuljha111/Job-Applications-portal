package com.Jobtrackr.jta.verification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String otpCode;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Builder.Default
    @Column(nullable = false)
    private Integer attempts = 0;
    
    @Builder.Default
    @Column(nullable = false)
    private Integer maxAttempts = 3;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean verified = false;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private VerificationType type = VerificationType.REGISTRATION;
    
    public enum VerificationType {
        REGISTRATION,
        PASSWORD_RESET,
        EMAIL_CHANGE
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusMinutes(10); // 10 minute expiry
        }
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isMaxAttemptsReached() {
        return attempts >= maxAttempts;
    }
    
    public void incrementAttempts() {
        this.attempts++;
    }
    
    public boolean canAttempt() {
        return !isExpired() && !isMaxAttemptsReached() && !verified;
    }
}