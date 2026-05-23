package com.Jobtrackr.jta.verification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.mail.from:noreply@jobtracker.com}")
    private String fromEmail;
    
    @Value("${app.name:Job Tracker}")
    private String appName;
    
    public void sendVerificationEmail(String to, String otpCode, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject("Email Verification - " + appName);
            message.setText(buildVerificationEmailContent(otpCode, userName));
            
            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
            
        } catch (Exception e) {
            log.error("Failed to send verification email to: {} - {}", to, e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
    
    public void sendPasswordResetEmail(String to, String otpCode, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject("Password Reset - " + appName);
            message.setText(buildPasswordResetEmailContent(otpCode, userName));
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
            
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {} - {}", to, e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
    
    public void sendWelcomeEmail(String to, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject("Welcome to " + appName);
            message.setText(buildWelcomeEmailContent(userName));
            
            mailSender.send(message);
            log.info("Welcome email sent to: {}", to);
            
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {} - {}", to, e.getMessage());
            // Don't throw exception for welcome email failures
        }
    }
    
    private String buildVerificationEmailContent(String otpCode, String userName) {
        return String.format("""
            Hi %s,
            
            Thank you for registering with %s!
            
            To complete your registration, please use the following verification code:
            
            %s
            
            This code will expire in 10 minutes for security reasons.
            
            If you didn't create an account with us, please ignore this email.
            
            Best regards,
            %s Team
            """, userName, appName, otpCode, appName);
    }
    
    private String buildPasswordResetEmailContent(String otpCode, String userName) {
        return String.format("""
            Hi %s,
            
            We received a request to reset your password for your %s account.
            
            Use the following code to reset your password:
            
            %s
            
            This code will expire in 10 minutes for security reasons.
            
            If you didn't request a password reset, please ignore this email and your password will remain unchanged.
            
            Best regards,
            %s Team
            """, userName, appName, otpCode, appName);
    }
    
    private String buildWelcomeEmailContent(String userName) {
        return String.format("""
            Hi %s,
            
            Welcome to %s! Your email has been successfully verified.
            
            You can now:
            • Track your job applications
            • Get AI-powered insights for roles
            • Manage your professional profile
            • Access personalized recommendations
            
            Get started by logging into your account and exploring our features.
            
            Best regards,
            %s Team
            """, userName, appName, appName);
    }
}