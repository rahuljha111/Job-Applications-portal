package com.Jobtrackr.jta.analytics.dto;

import java.util.Map;

public class CandidateAnalyticsResponse {
    private int totalApplications;
    private int activeApplications;
    private int shortlisted;
    private int rejected;
    private int hired;
    private double successRate;
    private double averageResponseTime;
    private Map<String, Integer> applicationsByStatus;
    private Map<String, Integer> applicationsByMonth;
    private Map<String, Integer> applicationsByJobType;

    public CandidateAnalyticsResponse() {}

    public int getTotalApplications() { return totalApplications; }
    public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }

    public int getActiveApplications() { return activeApplications; }
    public void setActiveApplications(int activeApplications) { this.activeApplications = activeApplications; }

    public int getShortlisted() { return shortlisted; }
    public void setShortlisted(int shortlisted) { this.shortlisted = shortlisted; }

    public int getRejected() { return rejected; }
    public void setRejected(int rejected) { this.rejected = rejected; }

    public int getHired() { return hired; }
    public void setHired(int hired) { this.hired = hired; }

    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }

    public double getAverageResponseTime() { return averageResponseTime; }
    public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }

    public Map<String, Integer> getApplicationsByStatus() { return applicationsByStatus; }
    public void setApplicationsByStatus(Map<String, Integer> applicationsByStatus) { this.applicationsByStatus = applicationsByStatus; }

    public Map<String, Integer> getApplicationsByMonth() { return applicationsByMonth; }
    public void setApplicationsByMonth(Map<String, Integer> applicationsByMonth) { this.applicationsByMonth = applicationsByMonth; }

    public Map<String, Integer> getApplicationsByJobType() { return applicationsByJobType; }
    public void setApplicationsByJobType(Map<String, Integer> applicationsByJobType) { this.applicationsByJobType = applicationsByJobType; }
}
