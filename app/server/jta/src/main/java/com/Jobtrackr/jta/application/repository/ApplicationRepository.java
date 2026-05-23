package com.Jobtrackr.jta.application.repository;

import com.Jobtrackr.jta.application.entity.Application;
import com.Jobtrackr.jta.application.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    boolean existsByJobIdAndCandidateId(UUID jobId, UUID candidateId);

    List<Application> findByCandidateId(UUID candidateId);

    List<Application> findByJobId(UUID jobId);
    
    Page<Application> findByCandidateId(UUID candidateId, Pageable pageable);

    long countByCandidateId(UUID candidateId);
    
    long countByCandidateIdAndStatus(UUID candidateId, ApplicationStatus status);
    
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.recruiter.id = :recruiterId")
    long countByRecruiterId(@Param("recruiterId") UUID recruiterId);
    
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.recruiter.id = :recruiterId AND a.status = :status")
    long countByRecruiterIdAndStatus(@Param("recruiterId") UUID recruiterId, @Param("status") ApplicationStatus status);
    
    @Query("SELECT a FROM Application a WHERE a.job.recruiter.id = :recruiterId")
    List<Application> findByRecruiterId(@Param("recruiterId") UUID recruiterId);
    
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.candidate.id = :candidateId GROUP BY a.status")
    List<Object[]> countByStatusForCandidate(@Param("candidateId") UUID candidateId);
    
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.job.recruiter.id = :recruiterId GROUP BY a.status")
    List<Object[]> countByStatusForRecruiter(@Param("recruiterId") UUID recruiterId);
    
    @Query("SELECT FUNCTION('TO_CHAR', a.appliedAt, 'YYYY-MM'), COUNT(a) FROM Application a WHERE a.candidate.id = :candidateId GROUP BY FUNCTION('TO_CHAR', a.appliedAt, 'YYYY-MM') ORDER BY FUNCTION('TO_CHAR', a.appliedAt, 'YYYY-MM')")
    List<Object[]> countByMonthForCandidate(@Param("candidateId") UUID candidateId);
    
    @Query("SELECT j.type, COUNT(a) FROM Application a JOIN a.job j WHERE a.candidate.id = :candidateId GROUP BY j.type")
    List<Object[]> countByJobTypeForCandidate(@Param("candidateId") UUID candidateId);
}
