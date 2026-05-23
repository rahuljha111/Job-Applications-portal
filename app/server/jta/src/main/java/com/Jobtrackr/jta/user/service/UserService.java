package com.Jobtrackr.jta.user.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import com.Jobtrackr.jta.config.JwtUtil;
import com.Jobtrackr.jta.exception.BadRequestException;
import com.Jobtrackr.jta.exception.ConflictException;
import com.Jobtrackr.jta.exception.NotFoundException;
import com.Jobtrackr.jta.exception.UnauthorizedActionException;
import com.Jobtrackr.jta.user.dto.AuthUser;
import com.Jobtrackr.jta.user.dto.ChangePasswordRequest;
import com.Jobtrackr.jta.user.dto.ForgotPasswordRequest;
import com.Jobtrackr.jta.user.dto.LoginRequest;
import com.Jobtrackr.jta.user.dto.LoginResponse;
import com.Jobtrackr.jta.user.dto.RegisterRequest;
import com.Jobtrackr.jta.user.dto.ResetPasswordRequest;
import com.Jobtrackr.jta.user.dto.UpdateProfileRequest;
import com.Jobtrackr.jta.user.dto.UserResponse;
import com.Jobtrackr.jta.user.entity.EmailVerificationToken;
import com.Jobtrackr.jta.user.entity.PasswordResetToken;
import com.Jobtrackr.jta.user.entity.Role;
import com.Jobtrackr.jta.user.entity.User;
import com.Jobtrackr.jta.user.repository.EmailVerificationTokenRepository;
import com.Jobtrackr.jta.user.repository.PasswordResetTokenRepository;
import com.Jobtrackr.jta.user.repository.UserRepository;
import com.Jobtrackr.jta.verification.entity.EmailVerification;
import com.Jobtrackr.jta.verification.service.EmailVerificationService;
import com.Jobtrackr.jta.user.dto.RequestEmailVerificationRequest;
import com.Jobtrackr.jta.user.dto.VerifyEmailRequest;
import com.Jobtrackr.jta.user.util.PasswordValidator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailVerificationService emailVerificationService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;

    private static final int RESET_TOKEN_VALID_MINUTES = 60;
    private static final int EMAIL_OTP_VALID_MINUTES = 15;

    public UserService(UserRepository userRepository,
                       PasswordResetTokenRepository resetTokenRepository,
                       EmailVerificationTokenRepository emailVerificationTokenRepository,
                       EmailVerificationService emailVerificationService,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailVerificationService = emailVerificationService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.mailSender = mailSender;
    }

    public UserResponse registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists");
        }
        PasswordValidator.validate(request.getPassword());

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.CANDIDATE);
        user.setLocation(null);
        
        // User starts with unverified email - must verify to login
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);
        
        // Send email verification OTP
        try {
            emailVerificationService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getName(),
                EmailVerification.VerificationType.REGISTRATION
            );
        } catch (Exception e) {
            // If email sending fails, still allow registration but log the error
            // User can request resend later
            System.err.println("Failed to send verification email: " + e.getMessage());
        }

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.isActive()
        );
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        );
    }

    public List<UserResponse> getAllUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User current = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (current.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException("Only admins can list all users");
        }
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.isActive()
                ))
                .toList();
    }

    public AuthUser updateProfile(UpdateProfileRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedActionException("Not authenticated");
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation().trim());
        }

        User saved = userRepository.save(user);
        return new AuthUser(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getRole(),
                saved.getLocation()
        );
    }

    public void changePassword(ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedActionException("Not authenticated");
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ConflictException("Current password is incorrect");
        }
        PasswordValidator.validate(request.getNewPassword());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ConflictException("Invalid password");
        }
        
        if (!user.isEmailVerified()) {
            throw new UnauthorizedActionException("Email not verified. Please check your email for verification code.");
        }

        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId()
        );
        
        String refreshToken = jwtUtil.generateRefreshToken(
                user.getEmail(),
                user.getId()
        );

        AuthUser authUser = new AuthUser(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getLocation()
        );
        return new LoginResponse(accessToken, refreshToken, authUser);
    }
    
    public LoginResponse refreshToken(String refreshToken) {
        String email = jwtUtil.validateToken(refreshToken);
        if (email == null) {
            throw new BadRequestException("Invalid refresh token");
        }
        
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Invalid token type");
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        String newAccessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId()
        );
        
        String newRefreshToken = jwtUtil.generateRefreshToken(
                user.getEmail(),
                user.getId()
        );
        
        AuthUser authUser = new AuthUser(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getLocation()
        );
        
        return new LoginResponse(newAccessToken, newRefreshToken, authUser);
    }

    public AuthUser getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return new AuthUser(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getLocation()
        );
    }

    public void forgotPassword(ForgotPasswordRequest request) {

        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {

            String token = UUID.randomUUID().toString().replace("-", "");
            Instant expiresAt = Instant.now().plusSeconds(RESET_TOKEN_VALID_MINUTES * 60L);

            resetTokenRepository.save(new PasswordResetToken(token, user, expiresAt));

            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Password Reset Request");
                message.setText(
                        "Reset your password using this link:\n\n" +
                                "http://localhost:5173/reset-password?token=" + token
                );

                mailSender.send(message);

                System.out.println("Password reset email sent to: " + user.getEmail());

            } catch (Exception e) {
                throw new RuntimeException("Failed to send reset email: " + e.getMessage());
            }
        });

        // Always return success to avoid revealing whether email exists
    }

    public void resetPassword(ResetPasswordRequest request) {
        PasswordValidator.validate(request.getNewPassword());

        PasswordResetToken resetToken =
                resetTokenRepository.findByTokenAndUsedFalse(request.getToken())
                        .orElseThrow(() ->
                                new NotFoundException("Invalid or expired reset link"));

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            resetTokenRepository.delete(resetToken);
            throw new BadRequestException("Reset link has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);
    }

    public void requestEmailVerification(RequestEmailVerificationRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String code = String.format("%06d",
                new java.util.Random().nextInt(1_000_000));

        Instant expiresAt =
                Instant.now().plusSeconds(EMAIL_OTP_VALID_MINUTES * 60L);

        emailVerificationTokenRepository.save(
                new EmailVerificationToken(code, user, expiresAt)
        );

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("JobTracker Email Verification Code");
            message.setText(
                    "Hello " + user.getName() + ",\n\n" +
                            "Your verification code is: " + code + "\n\n" +
                            "This code will expire in " + EMAIL_OTP_VALID_MINUTES + " minutes."
            );

            mailSender.send(message);

            System.out.println("Verification email sent to: " + user.getEmail());

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to send verification email: " + e.getMessage());
        }
    }

    public void verifyEmail(VerifyEmailRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        EmailVerificationToken token =
                emailVerificationTokenRepository
                        .findByUserAndCodeAndUsedFalse(user, request.getCode())
                        .orElseThrow(() ->
                                new BadRequestException("Invalid or expired verification code"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            emailVerificationTokenRepository.delete(token);
            throw new BadRequestException("Verification code has expired");
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        token.setUsed(true);
        emailVerificationTokenRepository.save(token);
    }
}