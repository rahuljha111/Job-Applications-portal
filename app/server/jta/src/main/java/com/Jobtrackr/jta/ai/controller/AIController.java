package com.Jobtrackr.jta.ai.controller;

import com.Jobtrackr.jta.ai.dto.*;
import com.Jobtrackr.jta.ai.service.AIService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private static final Logger log = LoggerFactory.getLogger(AIController.class);
    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/analyze-resume")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(
            @Valid @RequestBody ResumeAnalysisRequest request) {
        log.info("Resume analysis requested for role: {}", request.getTargetRole());
        return ResponseEntity.ok(aiService.analyzeResume(request));
    }

    @PostMapping("/interview-prep")
    public ResponseEntity<InterviewPrepResponse> generateInterviewQuestions(
            @Valid @RequestBody InterviewPrepRequest request) {
        log.info("Interview prep requested for: {}", request.getJobTitle());
        return ResponseEntity.ok(aiService.generateInterviewQuestions(request));
    }

    @PostMapping("/skill-suggestions")
    public ResponseEntity<SkillSuggestionResponse> suggestSkills(
            @Valid @RequestBody SkillSuggestionRequest request) {
        log.info("Skill suggestions requested for role: {}", request.getTargetRole());
        return ResponseEntity.ok(aiService.suggestSkills(request));
    }
}
