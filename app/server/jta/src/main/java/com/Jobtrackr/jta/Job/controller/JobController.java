package com.Jobtrackr.jta.Job.controller;

import com.Jobtrackr.jta.Job.dto.JobCreateRequest;
import com.Jobtrackr.jta.Job.dto.JobListResponse;
import com.Jobtrackr.jta.Job.dto.JobResponse;
import com.Jobtrackr.jta.Job.service.JobService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobCreateRequest request) {
        log.info("Creating new job: {}", request.getTitle());
        JobResponse response = jobService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/recruiter")
    public ResponseEntity<Page<JobListResponse>> getMyJobs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(jobService.getJobsByRecruiter(pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<JobListResponse>> getAllJobs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(jobService.getAllJobsAdmin(pageable));
    }

    @GetMapping
    public ResponseEntity<Page<JobListResponse>> getOpenJobs(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(jobService.getOpenJobs(pageable, search, location, type));
    }
    
    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable UUID jobId) {
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }

    @PatchMapping("/{jobId}/close")
    public ResponseEntity<Map<String, String>> closeJob(@PathVariable UUID jobId) {
        log.info("Closing job: {}", jobId);
        jobService.closeJob(jobId);
        return ResponseEntity.ok(Map.of("message", "Job closed successfully"));
    }
}