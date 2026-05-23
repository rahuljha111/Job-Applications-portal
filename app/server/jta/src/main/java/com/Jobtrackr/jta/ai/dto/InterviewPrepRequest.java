package com.Jobtrackr.jta.ai.dto;

import jakarta.validation.constraints.NotBlank;

public class InterviewPrepRequest {
    
    @NotBlank(message = "Job title is required")
    private String jobTitle;
    
    private String jobDescription;
    
    private String companyName;
    
    private String experienceLevel;
    
    private int numberOfQuestions = 10;

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public int getNumberOfQuestions() { return numberOfQuestions; }
    public void setNumberOfQuestions(int numberOfQuestions) { this.numberOfQuestions = numberOfQuestions; }
}
