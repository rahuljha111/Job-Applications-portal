package com.Jobtrackr.jta.resume.controller;

import com.Jobtrackr.jta.resume.dto.ResumeResponse;
import com.Jobtrackr.jta.resume.service.ResumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private static final Logger log = LoggerFactory.getLogger(ResumeController.class);
    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "label", required = false) String label,
            @RequestParam(value = "setAsDefault", defaultValue = "false") boolean setAsDefault) {
        log.info("Uploading resume: {}", file.getOriginalFilename());
        ResumeResponse response = resumeService.uploadResume(file, label, setAsDefault);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getMyResumes() {
        return ResponseEntity.ok(resumeService.getUserResumes());
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<ResumeResponse> getResume(@PathVariable UUID resumeId) {
        return ResponseEntity.ok(resumeService.getResumeById(resumeId));
    }

    @GetMapping("/{resumeId}/download")
    public ResponseEntity<Resource> downloadResume(@PathVariable UUID resumeId) {
        ResumeResponse resumeInfo = resumeService.getResumeById(resumeId);
        Resource resource = resumeService.downloadResume(resumeId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resumeInfo.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + resumeInfo.getOriginalFileName() + "\"")
                .body(resource);
    }

    @PatchMapping("/{resumeId}/default")
    public ResponseEntity<ResumeResponse> setDefaultResume(@PathVariable UUID resumeId) {
        log.info("Setting default resume: {}", resumeId);
        return ResponseEntity.ok(resumeService.setDefaultResume(resumeId));
    }

    @PatchMapping("/{resumeId}/label")
    public ResponseEntity<ResumeResponse> updateLabel(
            @PathVariable UUID resumeId,
            @RequestBody Map<String, String> request) {
        String label = request.get("label");
        return ResponseEntity.ok(resumeService.updateResumeLabel(resumeId, label));
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<Map<String, String>> deleteResume(@PathVariable UUID resumeId) {
        log.info("Deleting resume: {}", resumeId);
        resumeService.deleteResume(resumeId);
        return ResponseEntity.ok(Map.of("message", "Resume deleted successfully"));
    }
}
