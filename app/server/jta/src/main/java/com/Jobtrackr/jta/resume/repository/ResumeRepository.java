package com.Jobtrackr.jta.resume.repository;

import com.Jobtrackr.jta.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeRepository extends JpaRepository<Resume, UUID> {
    
    List<Resume> findByUserIdOrderByUploadedAtDesc(UUID userId);
    
    Optional<Resume> findByIdAndUserId(UUID id, UUID userId);
    
    Optional<Resume> findByUserIdAndIsDefaultTrue(UUID userId);
    
    @Query("SELECT COUNT(r) FROM Resume r WHERE r.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COALESCE(MAX(r.version), 0) FROM Resume r WHERE r.user.id = :userId")
    int findMaxVersionByUserId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("UPDATE Resume r SET r.isDefault = false WHERE r.user.id = :userId")
    void clearDefaultForUser(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM Resume r WHERE r.user.id = :userId AND r.id = :resumeId")
    void deleteByIdAndUserId(@Param("resumeId") UUID resumeId, @Param("userId") UUID userId);
}
