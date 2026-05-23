package com.Jobtrackr.jta.application.entity;

import com.Jobtrackr.jta.Job.entity.Job;
import com.Jobtrackr.jta.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "applications",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"job_id", "candidate_id"})
        })
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne(optional = false)
    @JoinColumn(name = "candidate_id")
    private User candidate;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private LocalDateTime appliedAt;

    private LocalDateTime updatedAt;

    // Optional relationship to a Resume (candidate's uploaded resume)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private com.Jobtrackr.jta.resume.entity.Resume resume;

    @Column(columnDefinition = "text")
    private String coverLetter;

    private String portfolioUrl;

    @PrePersist
    public void prePersist() {
        appliedAt = LocalDateTime.now();
        status = ApplicationStatus.APPLIED;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

