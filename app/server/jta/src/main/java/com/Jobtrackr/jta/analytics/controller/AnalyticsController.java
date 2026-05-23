package com.Jobtrackr.jta.analytics.controller;

import com.Jobtrackr.jta.analytics.dto.CandidateAnalyticsResponse;
import com.Jobtrackr.jta.analytics.dto.RecruiterAnalyticsResponse;
import com.Jobtrackr.jta.analytics.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/candidate")
    public ResponseEntity<CandidateAnalyticsResponse> getCandidateAnalytics() {
        log.info("Fetching candidate analytics");
        return ResponseEntity.ok(analyticsService.getCandidateAnalytics());
    }

    @GetMapping("/recruiter")
    public ResponseEntity<RecruiterAnalyticsResponse> getRecruiterAnalytics() {
        log.info("Fetching recruiter analytics");
        return ResponseEntity.ok(analyticsService.getRecruiterAnalytics());
    }
}
