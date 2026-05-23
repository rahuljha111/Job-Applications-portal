package com.Jobtrackr.jta.application.dto;

import java.util.UUID;

public class ApplyRequest {
    private UUID resumeId;
    private String coverLetter;
    private String portfolioUrl;

    public ApplyRequest() {}

    public UUID getResumeId() { return resumeId; }
    public void setResumeId(UUID resumeId) { this.resumeId = resumeId; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }
}

