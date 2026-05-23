package com.Jobtrackr.jta.analytics.dto;

import java.util.Map;

public class RecruiterAnalyticsResponse {
    private int totalJobPostings;
    private int activeJobs;
    private int closedJobs;
    private int totalApplicationsReceived;
    private int pendingReview;
    private int shortlisted;
    private int rejected;
    private int hired;
    private double averageApplicationsPerJob;
    private double averageTimeToHire;
    private Map<String, Integer> applicationsByJob;
    private Map<String, Integer> hiringFunnel;
    private Map<String, Integer> applicationTrend;

    public RecruiterAnalyticsResponse() {}

    public int getTotalJobPostings() { return totalJobPostings; }
    public void setTotalJobPostings(int totalJobPostings) { this.totalJobPostings = totalJobPostings; }

    public int getActiveJobs() { return activeJobs; }
    public void setActiveJobs(int activeJobs) { this.activeJobs = activeJobs; }

    public int getClosedJobs() { return closedJobs; }
    public void setClosedJobs(int closedJobs) { this.closedJobs = closedJobs; }

    public int getTotalApplicationsReceived() { return totalApplicationsReceived; }
    public void setTotalApplicationsReceived(int totalApplicationsReceived) { this.totalApplicationsReceived = totalApplicationsReceived; }

    public int getPendingReview() { return pendingReview; }
    public void setPendingReview(int pendingReview) { this.pendingReview = pendingReview; }

    public int getShortlisted() { return shortlisted; }
    public void setShortlisted(int shortlisted) { this.shortlisted = shortlisted; }

    public int getRejected() { return rejected; }
    public void setRejected(int rejected) { this.rejected = rejected; }

    public int getHired() { return hired; }
    public void setHired(int hired) { this.hired = hired; }

    public double getAverageApplicationsPerJob() { return averageApplicationsPerJob; }
    public void setAverageApplicationsPerJob(double averageApplicationsPerJob) { this.averageApplicationsPerJob = averageApplicationsPerJob; }

    public double getAverageTimeToHire() { return averageTimeToHire; }
    public void setAverageTimeToHire(double averageTimeToHire) { this.averageTimeToHire = averageTimeToHire; }

    public Map<String, Integer> getApplicationsByJob() { return applicationsByJob; }
    public void setApplicationsByJob(Map<String, Integer> applicationsByJob) { this.applicationsByJob = applicationsByJob; }

    public Map<String, Integer> getHiringFunnel() { return hiringFunnel; }
    public void setHiringFunnel(Map<String, Integer> hiringFunnel) { this.hiringFunnel = hiringFunnel; }

    public Map<String, Integer> getApplicationTrend() { return applicationTrend; }
    public void setApplicationTrend(Map<String, Integer> applicationTrend) { this.applicationTrend = applicationTrend; }
}
