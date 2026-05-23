package com.Jobtrackr.jta.verification.repository;

import com.Jobtrackr.jta.verification.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {
    
    Optional<EmailVerification> findByEmailAndTypeAndVerifiedFalse(String email, EmailVerification.VerificationType type);
    
    Optional<EmailVerification> findByEmailAndOtpCodeAndTypeAndVerifiedFalse(String email, String otpCode, EmailVerification.VerificationType type);
    
    List<EmailVerification> findByEmailAndTypeOrderByCreatedAtDesc(String email, EmailVerification.VerificationType type);
    
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.expiresAt < :now")
    List<EmailVerification> findExpiredVerifications(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiresAt < :now")
    void deleteExpiredVerifications(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.email = :email AND ev.type = :type")
    void deleteByEmailAndType(@Param("email") String email, @Param("type") EmailVerification.VerificationType type);
    
    @Query("SELECT COUNT(ev) FROM EmailVerification ev WHERE ev.email = :email AND ev.createdAt > :since")
    Long countRecentVerificationsByEmail(@Param("email") String email, @Param("since") LocalDateTime since);
}