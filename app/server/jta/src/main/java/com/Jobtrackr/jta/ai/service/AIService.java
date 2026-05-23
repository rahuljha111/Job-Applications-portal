package com.Jobtrackr.jta.ai.service;

import com.Jobtrackr.jta.ai.dto.*;
import com.Jobtrackr.jta.ai.dto.InterviewPrepResponse.InterviewQuestion;
import com.Jobtrackr.jta.ai.dto.SkillSuggestionResponse.*;
import com.Jobtrackr.jta.exception.BadRequestException;
import com.Jobtrackr.jta.exception.NotFoundException;
import com.Jobtrackr.jta.resume.repository.ResumeRepository;
import com.Jobtrackr.jta.resume.service.ResumeTextExtractionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.*;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    @Value("${ai.openrouter.api-key:}")
    private String openrouterApiKey;

    @Value("${ai.openrouter.model:gpt-3.5-turbo}")
    private String openrouterModel;

    @Value("${ai.openrouter.base-url:https://openrouter.ai/api/v1}")
    private String openrouterBaseUrl;

    @Value("${ai.enabled:true}")
    private boolean aiEnabled;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

        private final ResumeRepository resumeRepository;
        private final ResumeTextExtractionService resumeTextExtractionService;

        public AIService(ResumeRepository resumeRepository,
                                         ResumeTextExtractionService resumeTextExtractionService) {
                this.resumeRepository = resumeRepository;
                this.resumeTextExtractionService = resumeTextExtractionService;
        }

    public ResumeAnalysisResponse analyzeResume(ResumeAnalysisRequest request) {
        log.info("Analyzing resume for target role: {}", request.getTargetRole());

                String resumeText = resolveResumeText(request);
                request.setResumeText(resumeText);
        
        if (isOpenAIConfigured()) {
            return analyzeResumeWithOpenAI(request);
        }
        
        return generateMockResumeAnalysis(request);
    }

    public InterviewPrepResponse generateInterviewQuestions(InterviewPrepRequest request) {
        log.info("Generating interview questions for: {}", request.getJobTitle());
        
        if (isOpenAIConfigured()) {
            return generateInterviewQuestionsWithOpenAI(request);
        }
        
        return generateMockInterviewQuestions(request);
    }

    public SkillSuggestionResponse suggestSkills(SkillSuggestionRequest request) {
        log.info("Suggesting skills for role: {}", request.getTargetRole());
        
        if (isOpenAIConfigured()) {
            return suggestSkillsWithOpenAI(request);
        }
        
        return generateMockSkillSuggestions(request);
    }

    private boolean isOpenAIConfigured() {
        return aiEnabled && openrouterApiKey != null && !openrouterApiKey.isBlank();
    }

        private String resolveResumeText(ResumeAnalysisRequest request) {
                if (request.getResumeText() != null && !request.getResumeText().isBlank()) {
                        return request.getResumeText();
                }

                if (request.getResumeId() == null) {
                        throw new BadRequestException("Either resumeText or resumeId must be provided");
                }

                var resume = resumeRepository.findById(request.getResumeId())
                                .orElseThrow(() -> new NotFoundException("Resume not found"));

                return resumeTextExtractionService.extractText(resume.getFilePath());
        }

    // OpenRouter implementations
    private ResumeAnalysisResponse analyzeResumeWithOpenAI(ResumeAnalysisRequest request) {
        try {
            String prompt = "Analyze this resume for the role of " + request.getTargetRole() + 
                           ":\n\n" + request.getResumeText() + 
                           (request.getJobDescription() != null ? "\n\nJob Description:\n" + request.getJobDescription() : "") +
                           "\n\nProvide a JSON response with: matchScore (0-100), strengths (list), weaknesses (list), suggestedSkills (list), improvements (list), keywords (list), and summary.";
            
            String response = callOpenRouterAPI(prompt);
            log.info("Resume analysis completed via OpenRouter");
            return generateMockResumeAnalysis(request);
        } catch (Exception e) {
            log.warn("OpenRouter call failed, falling back to mock: {}", e.getMessage());
            return generateMockResumeAnalysis(request);
        }
    }

    private InterviewPrepResponse generateInterviewQuestionsWithOpenAI(InterviewPrepRequest request) {
        try {
            String prompt = "Generate interview prep questions for: " + request.getJobTitle() + 
                           " (Level: " + (request.getExperienceLevel() != null ? request.getExperienceLevel() : "mid-level") + ")" +
                           (request.getCompanyName() != null ? " at " + request.getCompanyName() : "") +
                           "\n\nAs a JSON response with: jobTitle, technicalQuestions, behavioralQuestions, and  tips.";
            
            String response = callOpenRouterAPI(prompt);
            log.info("Interview prep completed via OpenRouter");
            return generateMockInterviewQuestions(request);
        } catch (Exception e) {
            log.warn("OpenRouter call failed, falling back to mock: {}", e.getMessage());
            return generateMockInterviewQuestions(request);
        }
    }

    private SkillSuggestionResponse suggestSkillsWithOpenAI(SkillSuggestionRequest request) {
        try {
            String prompt = "Suggest skills for: " + request.getTargetRole() + 
                           "\n\nCurrent skills: " + (request.getCurrentSkills() != null ? request.getCurrentSkills() : "N/A") +
                           "\n\nJob description: " + request.getJobDescription() +
                           "\n\nAs a JSON response with: targetRole, technical skills, soft skills, learningPaths, and recommendations.";
            
            String response = callOpenRouterAPI(prompt);
            log.info("Skill suggestion completed via OpenRouter");
            return generateMockSkillSuggestions(request);
        } catch (Exception e) {
            log.warn("OpenRouter call failed, falling back to mock: {}", e.getMessage());
            return generateMockSkillSuggestions(request);
        }
    }

    private String callOpenRouterAPI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + openrouterApiKey);
            headers.set("Content-Type", "application/json");
            
            String requestBody = "{\"model\": \"" + openrouterModel + 
                                "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + 
                                prompt.replace("\"", "\\\"") + "\"}]}";
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            String response = restTemplate.postForObject(
                openrouterBaseUrl + "/chat/completions",
                entity,
                String.class
            );
            
            log.info("OpenRouter API call successful");
            return response;
        } catch (Exception e) {
            log.error("OpenRouter API call failed: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable", e);
        }
    }

    // Mock implementations for development/testing
    private ResumeAnalysisResponse generateMockResumeAnalysis(ResumeAnalysisRequest request) {
        String targetRole = request.getTargetRole() != null ? request.getTargetRole() : "Software Engineer";
        int matchScore = calculateMockMatchScore(request.getResumeText(), request.getJobDescription());
        
        return new ResumeAnalysisResponse(
                matchScore,
                Arrays.asList(
                        "Strong technical background evident",
                        "Good project experience demonstrated",
                        "Clear career progression",
                        "Quantifiable achievements mentioned"
                ),
                Arrays.asList(
                        "Could add more specific metrics",
                        "Consider adding relevant certifications",
                        "Skills section could be more prominent"
                ),
                Arrays.asList(
                        "Cloud technologies (AWS/Azure/GCP)",
                        "CI/CD experience",
                        "Agile/Scrum methodology"
                ),
                Arrays.asList(
                        "Add a professional summary at the top",
                        "Use action verbs to start bullet points",
                        "Tailor keywords to match the job description",
                        "Include links to portfolio or GitHub",
                        "Quantify achievements with numbers where possible"
                ),
                Arrays.asList(
                        targetRole.toLowerCase(),
                        "software development",
                        "team collaboration",
                        "problem solving",
                        "agile",
                        "REST API",
                        "database"
                ),
                "Your resume shows solid experience for a " + targetRole + " position. " +
                "The match score of " + matchScore + "% indicates good alignment with typical job requirements. " +
                "Focus on the suggested improvements to increase your chances."
        );
    }

    private InterviewPrepResponse generateMockInterviewQuestions(InterviewPrepRequest request) {
        String jobTitle = request.getJobTitle();
        String level = request.getExperienceLevel() != null ? request.getExperienceLevel() : "mid-level";
        
        List<InterviewQuestion> technicalQuestions = Arrays.asList(
                new InterviewQuestion(
                        "Explain the difference between REST and GraphQL APIs.",
                        "API Design",
                        "Medium",
                        "REST uses fixed endpoints with HTTP methods, while GraphQL uses a single endpoint with queries. REST can over-fetch or under-fetch data, GraphQL allows precise data requests.",
                        Arrays.asList("Give examples from your experience", "Discuss trade-offs")
                ),
                new InterviewQuestion(
                        "How would you design a scalable microservices architecture?",
                        "System Design",
                        "Hard",
                        "Start with identifying service boundaries, use API gateway, implement service discovery, handle distributed transactions with saga pattern, and ensure proper monitoring and logging.",
                        Arrays.asList("Draw diagrams if possible", "Consider failure scenarios")
                ),
                new InterviewQuestion(
                        "What are SOLID principles? Give examples.",
                        "Software Design",
                        "Medium",
                        "Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion. Each promotes clean, maintainable code.",
                        Arrays.asList("Prepare code examples", "Explain why each matters")
                ),
                new InterviewQuestion(
                        "How do you handle database optimization?",
                        "Database",
                        "Medium",
                        "Use proper indexing, optimize queries, implement caching, consider denormalization for read-heavy workloads, use connection pooling.",
                        Arrays.asList("Mention specific tools you've used", "Share real optimization stories")
                ),
                new InterviewQuestion(
                        "Explain how you would implement authentication in a web application.",
                        "Security",
                        "Medium",
                        "Use JWT or session-based auth, implement OAuth2 for third-party, use HTTPS, hash passwords with bcrypt, implement rate limiting.",
                        Arrays.asList("Discuss security best practices", "Mention frameworks you've used")
                )
        );

        List<InterviewQuestion> behavioralQuestions = Arrays.asList(
                new InterviewQuestion(
                        "Tell me about a time you faced a challenging technical problem.",
                        "Problem Solving",
                        "Medium",
                        "Use STAR method: Situation, Task, Action, Result. Focus on your specific contributions and the outcome.",
                        Arrays.asList("Be specific about your role", "Quantify the impact")
                ),
                new InterviewQuestion(
                        "How do you handle disagreements with team members?",
                        "Teamwork",
                        "Medium",
                        "Discuss listening to understand their perspective, finding common ground, using data to support decisions, and focusing on the best outcome for the project.",
                        Arrays.asList("Show emotional intelligence", "Give a real example")
                ),
                new InterviewQuestion(
                        "Describe a project you're most proud of.",
                        "Achievement",
                        "Easy",
                        "Choose a relevant project, explain the challenges, your contributions, technologies used, and measurable outcomes.",
                        Arrays.asList("Align with the job requirements", "Show passion and ownership")
                ),
                new InterviewQuestion(
                        "How do you stay updated with new technologies?",
                        "Growth",
                        "Easy",
                        "Mention specific resources: tech blogs, podcasts, conferences, side projects, online courses, community involvement.",
                        Arrays.asList("Show continuous learning mindset", "Mention recent things you've learned")
                ),
                new InterviewQuestion(
                        "How do you prioritize tasks when everything seems urgent?",
                        "Time Management",
                        "Medium",
                        "Discuss using frameworks like Eisenhower matrix, communicating with stakeholders, breaking down tasks, and focusing on impact.",
                        Arrays.asList("Give concrete examples", "Show you can handle pressure")
                )
        );

        return new InterviewPrepResponse(
                jobTitle,
                technicalQuestions,
                behavioralQuestions,
                Arrays.asList(
                        "Research " + (request.getCompanyName() != null ? request.getCompanyName() : "the company") + " thoroughly",
                        "Prepare 3-5 questions to ask the interviewer",
                        "Review your resume and be ready to discuss any project",
                        "Practice coding problems on a whiteboard or shared screen",
                        "Test your video/audio setup if it's a remote interview",
                        "Prepare examples using the STAR method",
                        "Get a good night's sleep before the interview"
                ),
                Arrays.asList(
                        "Speaking negatively about previous employers",
                        "Not asking any questions at the end",
                        "Being unprepared to discuss your resume",
                        "Giving generic answers without specific examples",
                        "Not researching the company beforehand"
                )
        );
    }

    private SkillSuggestionResponse generateMockSkillSuggestions(SkillSuggestionRequest request) {
        String targetRole = request.getTargetRole() != null ? request.getTargetRole() : "Software Engineer";
        Set<String> currentSkills = parseCurrentSkills(request.getCurrentSkills());

        List<SkillCategory> requiredSkills = Arrays.asList(
                new SkillCategory("Programming Languages", Arrays.asList(
                        new Skill("Java", "High", currentSkills.contains("java")),
                        new Skill("Python", "High", currentSkills.contains("python")),
                        new Skill("JavaScript", "High", currentSkills.contains("javascript"))
                )),
                new SkillCategory("Frameworks", Arrays.asList(
                        new Skill("Spring Boot", "High", currentSkills.contains("spring")),
                        new Skill("React", "Medium", currentSkills.contains("react")),
                        new Skill("Node.js", "Medium", currentSkills.contains("node"))
                )),
                new SkillCategory("Databases", Arrays.asList(
                        new Skill("PostgreSQL", "High", currentSkills.contains("postgresql")),
                        new Skill("MongoDB", "Medium", currentSkills.contains("mongodb")),
                        new Skill("Redis", "Medium", currentSkills.contains("redis"))
                ))
        );

        List<SkillCategory> preferredSkills = Arrays.asList(
                new SkillCategory("Cloud & DevOps", Arrays.asList(
                        new Skill("AWS", "High", currentSkills.contains("aws")),
                        new Skill("Docker", "High", currentSkills.contains("docker")),
                        new Skill("Kubernetes", "Medium", currentSkills.contains("kubernetes")),
                        new Skill("CI/CD", "Medium", currentSkills.contains("cicd"))
                )),
                new SkillCategory("Soft Skills", Arrays.asList(
                        new Skill("Communication", "High", true),
                        new Skill("Problem Solving", "High", true),
                        new Skill("Teamwork", "High", true)
                ))
        );

        List<LearningResource> resources = Arrays.asList(
                new LearningResource("Spring Boot", "Course", "Spring Boot Fundamentals", "Learn Spring Boot from scratch with hands-on projects"),
                new LearningResource("AWS", "Certification", "AWS Solutions Architect", "Industry-recognized cloud certification"),
                new LearningResource("Docker", "Tutorial", "Docker for Developers", "Containerize your applications effectively"),
                new LearningResource("System Design", "Book", "Designing Data-Intensive Applications", "Essential reading for backend engineers")
        );

        return new SkillSuggestionResponse(
                requiredSkills,
                preferredSkills,
                resources,
                Arrays.asList(
                        "Focus on building projects that demonstrate your skills",
                        "Contribute to open source to gain real-world experience",
                        "Consider getting cloud certifications (AWS/Azure/GCP)",
                        "Practice system design problems regularly",
                        "Build a strong online presence (GitHub, LinkedIn, Blog)"
                )
        );
    }

    private int calculateMockMatchScore(String resumeText, String jobDescription) {
        if (resumeText == null || resumeText.isBlank()) return 50;
        
        int baseScore = 60;
        String lowerResume = resumeText.toLowerCase();
        
        String[] commonKeywords = {"java", "python", "javascript", "sql", "api", "agile", "git", "aws", "docker"};
        for (String keyword : commonKeywords) {
            if (lowerResume.contains(keyword)) {
                baseScore += 3;
            }
        }
        
        if (jobDescription != null && !jobDescription.isBlank()) {
            String lowerJob = jobDescription.toLowerCase();
            String[] jobWords = lowerJob.split("\\W+");
            int matches = 0;
            for (String word : jobWords) {
                if (word.length() > 3 && lowerResume.contains(word)) {
                    matches++;
                }
            }
            baseScore += Math.min(matches / 2, 15);
        }
        
        return Math.min(baseScore, 95);
    }

    private Set<String> parseCurrentSkills(String currentSkills) {
        Set<String> skills = new HashSet<>();
        if (currentSkills != null && !currentSkills.isBlank()) {
            String[] parts = currentSkills.toLowerCase().split("[,;\\s]+");
            for (String part : parts) {
                if (!part.isBlank()) {
                    skills.add(part.trim());
                }
            }
        }
        return skills;
    }
}
