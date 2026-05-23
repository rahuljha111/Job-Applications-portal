package com.Jobtrackr.jta.ai.dto;

import jakarta.validation.constraints.NotBlank;

public class SkillSuggestionRequest {
    
    @NotBlank(message = "Job description is required")
    private String jobDescription;
    
    private String currentSkills;
    
    private String targetRole;

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public String getCurrentSkills() { return currentSkills; }
    public void setCurrentSkills(String currentSkills) { this.currentSkills = currentSkills; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
}
