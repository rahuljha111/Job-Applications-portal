package com.Jobtrackr.jta.admin.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class PlatformStatsResponse {
    private long totalUsers;
    private long totalCandidates;
    private long totalRecruiters;
    private long totalAdmins;
    private long totalCompanies;
    private long totalJobs;
    private long activeJobs;
    private long totalApplications;
    private Map<String, Long> usersByRole;
    private Map<String, Long> jobsByStatus;
    private Map<String, Long> applicationsByStatus;
    private LocalDateTime generatedAt;

    public PlatformStatsResponse() {
        this.generatedAt = LocalDateTime.now();
    }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalCandidates() { return totalCandidates; }
    public void setTotalCandidates(long totalCandidates) { this.totalCandidates = totalCandidates; }

    public long getTotalRecruiters() { return totalRecruiters; }
    public void setTotalRecruiters(long totalRecruiters) { this.totalRecruiters = totalRecruiters; }

    public long getTotalAdmins() { return totalAdmins; }
    public void setTotalAdmins(long totalAdmins) { this.totalAdmins = totalAdmins; }

    public long getTotalCompanies() { return totalCompanies; }
    public void setTotalCompanies(long totalCompanies) { this.totalCompanies = totalCompanies; }

    public long getTotalJobs() { return totalJobs; }
    public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }

    public long getActiveJobs() { return activeJobs; }
    public void setActiveJobs(long activeJobs) { this.activeJobs = activeJobs; }

    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }

    public Map<String, Long> getUsersByRole() { return usersByRole; }
    public void setUsersByRole(Map<String, Long> usersByRole) { this.usersByRole = usersByRole; }

    public Map<String, Long> getJobsByStatus() { return jobsByStatus; }
    public void setJobsByStatus(Map<String, Long> jobsByStatus) { this.jobsByStatus = jobsByStatus; }

    public Map<String, Long> getApplicationsByStatus() { return applicationsByStatus; }
    public void setApplicationsByStatus(Map<String, Long> applicationsByStatus) { this.applicationsByStatus = applicationsByStatus; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
