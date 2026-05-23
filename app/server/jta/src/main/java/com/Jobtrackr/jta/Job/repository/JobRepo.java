package com.Jobtrackr.jta.Job.repository;

import com.Jobtrackr.jta.Job.entity.Job;
import com.Jobtrackr.jta.Job.entity.JobStatus;
import com.Jobtrackr.jta.Job.entity.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JobRepo extends JpaRepository<Job, UUID> {
    List<Job> findByStatus(JobStatus status);
    Page<Job> findByStatus(JobStatus status, Pageable pageable);
    Page<Job> findByRecruiterId(UUID recruiterId, Pageable pageable);
    
    @Query(value = "SELECT * FROM jobs j WHERE j.status = :status " +
           "AND (:search IS NULL OR LOWER(j.title) LIKE LOWER('%' || :search || '%') " +
           "OR LOWER(CAST(j.description AS TEXT)) LIKE LOWER('%' || :search || '%')) " +
           "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER('%' || :location || '%')) " +
           "AND (:type IS NULL OR j.type = :type) " +
           "ORDER BY j.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM jobs j WHERE j.status = :status " +
           "AND (:search IS NULL OR LOWER(j.title) LIKE LOWER('%' || :search || '%') " +
           "OR LOWER(CAST(j.description AS TEXT)) LIKE LOWER('%' || :search || '%')) " +
           "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER('%' || :location || '%')) " +
           "AND (:type IS NULL OR j.type = :type)",
           nativeQuery = true)
    Page<Job> searchOpenJobs(
            @Param("status") String status,
            @Param("search") String search,
            @Param("location") String location,
            @Param("type") String type,
            Pageable pageable
    );
    
    long countByStatus(JobStatus status);
    
    long countByRecruiterId(UUID recruiterId);
    
    @Query("SELECT COUNT(j) FROM Job j WHERE j.company.id = :companyId")
    long countByCompanyId(@Param("companyId") UUID companyId);
}