package com.Jobtrackr.jta.analytics.service;

import com.Jobtrackr.jta.Job.entity.JobStatus;
import com.Jobtrackr.jta.Job.repository.JobRepo;
import com.Jobtrackr.jta.analytics.dto.CandidateAnalyticsResponse;
import com.Jobtrackr.jta.analytics.dto.RecruiterAnalyticsResponse;
import com.Jobtrackr.jta.application.entity.ApplicationStatus;
import com.Jobtrackr.jta.application.repository.ApplicationRepository;
import com.Jobtrackr.jta.exception.NotFoundException;
import com.Jobtrackr.jta.exception.UnauthorizedActionException;
import com.Jobtrackr.jta.user.entity.Role;
import com.Jobtrackr.jta.user.entity.User;
import com.Jobtrackr.jta.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final ApplicationRepository applicationRepository;
    private final JobRepo jobRepository;
    private final UserRepository userRepository;

    public AnalyticsService(ApplicationRepository applicationRepository,
                            JobRepo jobRepository,
                            UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    public CandidateAnalyticsResponse getCandidateAnalytics() {
        User user = getCurrentUser();
        
        if (user.getRole() != Role.CANDIDATE) {
            throw new UnauthorizedActionException("Only candidates can access this analytics");
        }

        CandidateAnalyticsResponse response = new CandidateAnalyticsResponse();
        
        long total = applicationRepository.countByCandidateId(user.getId());
        long applied = applicationRepository.countByCandidateIdAndStatus(user.getId(), ApplicationStatus.APPLIED);
        long shortlisted = applicationRepository.countByCandidateIdAndStatus(user.getId(), ApplicationStatus.SHORTLISTED);
        long rejected = applicationRepository.countByCandidateIdAndStatus(user.getId(), ApplicationStatus.REJECTED);
        long hired = applicationRepository.countByCandidateIdAndStatus(user.getId(), ApplicationStatus.HIRED);

        response.setTotalApplications((int) total);
        response.setActiveApplications((int) (applied + shortlisted));
        response.setShortlisted((int) shortlisted);
        response.setRejected((int) rejected);
        response.setHired((int) hired);

        if (total > 0) {
            response.setSuccessRate((double) hired / total * 100);
        }

        Map<String, Integer> byStatus = new HashMap<>();
        byStatus.put("APPLIED", (int) applied);
        byStatus.put("SHORTLISTED", (int) shortlisted);
        byStatus.put("REJECTED", (int) rejected);
        byStatus.put("HIRED", (int) hired);
        response.setApplicationsByStatus(byStatus);

        try {
            Map<String, Integer> byMonth = new LinkedHashMap<>();
            List<Object[]> monthData = applicationRepository.countByMonthForCandidate(user.getId());
            for (Object[] row : monthData) {
                byMonth.put((String) row[0], ((Long) row[1]).intValue());
            }
            response.setApplicationsByMonth(byMonth);
        } catch (Exception e) {
            log.warn("Could not fetch monthly data: {}", e.getMessage());
            response.setApplicationsByMonth(new HashMap<>());
        }

        try {
            Map<String, Integer> byJobType = new HashMap<>();
            List<Object[]> typeData = applicationRepository.countByJobTypeForCandidate(user.getId());
            for (Object[] row : typeData) {
                byJobType.put(row[0].toString(), ((Long) row[1]).intValue());
            }
            response.setApplicationsByJobType(byJobType);
        } catch (Exception e) {
            log.warn("Could not fetch job type data: {}", e.getMessage());
            response.setApplicationsByJobType(new HashMap<>());
        }

        log.info("Analytics generated for candidate: {}", user.getEmail());
        return response;
    }

    public RecruiterAnalyticsResponse getRecruiterAnalytics() {
        User user = getCurrentUser();
        
        if (user.getRole() != Role.RECRUITER) {
            throw new UnauthorizedActionException("Only recruiters can access this analytics");
        }

        RecruiterAnalyticsResponse response = new RecruiterAnalyticsResponse();

        long totalJobs = jobRepository.countByRecruiterId(user.getId());
        long openJobs = jobRepository.findByRecruiterId(user.getId(), null)
                .stream()
                .filter(j -> j.getStatus() == JobStatus.OPEN)
                .count();
        
        response.setTotalJobPostings((int) totalJobs);
        response.setActiveJobs((int) openJobs);
        response.setClosedJobs((int) (totalJobs - openJobs));

        long totalApps = applicationRepository.countByRecruiterId(user.getId());
        long applied = applicationRepository.countByRecruiterIdAndStatus(user.getId(), ApplicationStatus.APPLIED);
        long shortlisted = applicationRepository.countByRecruiterIdAndStatus(user.getId(), ApplicationStatus.SHORTLISTED);
        long rejected = applicationRepository.countByRecruiterIdAndStatus(user.getId(), ApplicationStatus.REJECTED);
        long hired = applicationRepository.countByRecruiterIdAndStatus(user.getId(), ApplicationStatus.HIRED);

        response.setTotalApplicationsReceived((int) totalApps);
        response.setPendingReview((int) applied);
        response.setShortlisted((int) shortlisted);
        response.setRejected((int) rejected);
        response.setHired((int) hired);

        if (totalJobs > 0) {
            response.setAverageApplicationsPerJob((double) totalApps / totalJobs);
        }

        Map<String, Integer> funnel = new LinkedHashMap<>();
        funnel.put("Applied", (int) applied);
        funnel.put("Shortlisted", (int) shortlisted);
        funnel.put("Rejected", (int) rejected);
        funnel.put("Hired", (int) hired);
        response.setHiringFunnel(funnel);

        log.info("Analytics generated for recruiter: {}", user.getEmail());
        return response;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedActionException("Not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
