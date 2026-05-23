package com.Jobtrackr.jta.admin.dto;

import com.Jobtrackr.jta.user.entity.Role;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserManagementResponse {
    private UUID id;
    private String name;
    private String email;
    private Role role;
    private boolean isActive;
    private boolean emailVerified;
    private String companyName;
    private LocalDateTime createdAt;
    private long applicationCount;
    private long jobPostCount;

    public UserManagementResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public long getApplicationCount() { return applicationCount; }
    public void setApplicationCount(long applicationCount) { this.applicationCount = applicationCount; }

    public long getJobPostCount() { return jobPostCount; }
    public void setJobPostCount(long jobPostCount) { this.jobPostCount = jobPostCount; }
}
