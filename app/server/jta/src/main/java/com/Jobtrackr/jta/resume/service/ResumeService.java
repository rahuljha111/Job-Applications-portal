package com.Jobtrackr.jta.resume.service;

import com.Jobtrackr.jta.exception.BadRequestException;
import com.Jobtrackr.jta.exception.NotFoundException;
import com.Jobtrackr.jta.resume.dto.ResumeResponse;
import com.Jobtrackr.jta.resume.entity.Resume;
import com.Jobtrackr.jta.resume.repository.ResumeRepository;
import com.Jobtrackr.jta.user.entity.User;
import com.Jobtrackr.jta.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_RESUMES_PER_USER = 10;

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final Path uploadPath;

    public ResumeService(ResumeRepository resumeRepository,
                         UserRepository userRepository,
                         @Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Transactional
    public ResumeResponse uploadResume(MultipartFile file, String label, boolean setAsDefault) {
        User user = getCurrentUser();
        
        validateFile(file);
        
        long resumeCount = resumeRepository.countByUserId(user.getId());
        if (resumeCount >= MAX_RESUMES_PER_USER) {
            throw new BadRequestException("Maximum number of resumes (" + MAX_RESUMES_PER_USER + ") reached");
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String storedFileName = UUID.randomUUID().toString() + fileExtension;
        
        Path userDir = uploadPath.resolve(user.getId().toString());
        try {
            Files.createDirectories(userDir);
            Path targetPath = userDir.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            if (setAsDefault) {
                resumeRepository.clearDefaultForUser(user.getId());
            }
            
            int nextVersion = resumeRepository.findMaxVersionByUserId(user.getId()) + 1;
            
            Resume resume = new Resume();
            resume.setUser(user);
            resume.setFileName(storedFileName);
            resume.setOriginalFileName(originalFileName);
            resume.setContentType(file.getContentType());
            resume.setFileSize(file.getSize());
            resume.setFilePath(targetPath.toString());
            resume.setVersion(nextVersion);
            resume.setDefault(setAsDefault || resumeCount == 0);
            resume.setLabel(label != null ? label : "Resume v" + nextVersion);
            
            Resume saved = resumeRepository.save(resume);
            log.info("Resume uploaded: {} for user: {}", saved.getId(), user.getEmail());
            
            return mapToResponse(saved);
            
        } catch (IOException e) {
            log.error("Failed to store resume file", e);
            throw new RuntimeException("Failed to store resume file", e);
        }
    }

    public List<ResumeResponse> getUserResumes() {
        User user = getCurrentUser();
        return resumeRepository.findByUserIdOrderByUploadedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ResumeResponse getResumeById(UUID resumeId) {
        User user = getCurrentUser();
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() -> new NotFoundException("Resume not found"));
        return mapToResponse(resume);
    }

    public Resource downloadResume(UUID resumeId) {
        User user = getCurrentUser();
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() -> new NotFoundException("Resume not found"));
        
        try {
            Path filePath = Paths.get(resume.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                resume.setLastAccessedAt(LocalDateTime.now());
                resumeRepository.save(resume);
                return resource;
            } else {
                throw new NotFoundException("Resume file not found on disk");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error reading resume file", e);
        }
    }

    @Transactional
    public ResumeResponse setDefaultResume(UUID resumeId) {
        User user = getCurrentUser();
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() -> new NotFoundException("Resume not found"));
        
        resumeRepository.clearDefaultForUser(user.getId());
        resume.setDefault(true);
        Resume saved = resumeRepository.save(resume);
        
        log.info("Default resume set: {} for user: {}", resumeId, user.getEmail());
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteResume(UUID resumeId) {
        User user = getCurrentUser();
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() -> new NotFoundException("Resume not found"));
        
        try {
            Path filePath = Paths.get(resume.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Could not delete resume file: {}", resume.getFilePath());
        }
        
        resumeRepository.delete(resume);
        log.info("Resume deleted: {} for user: {}", resumeId, user.getEmail());
        
        if (resume.isDefault()) {
            resumeRepository.findByUserIdOrderByUploadedAtDesc(user.getId())
                    .stream()
                    .findFirst()
                    .ifPresent(r -> {
                        r.setDefault(true);
                        resumeRepository.save(r);
                    });
        }
    }

    @Transactional
    public ResumeResponse updateResumeLabel(UUID resumeId, String label) {
        User user = getCurrentUser();
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() -> new NotFoundException("Resume not found"));
        
        resume.setLabel(label);
        Resume saved = resumeRepository.save(resume);
        return mapToResponse(saved);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 10MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Only PDF and Word documents are allowed");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BadRequestException("Not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private ResumeResponse mapToResponse(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getFileName(),
                resume.getOriginalFileName(),
                resume.getContentType(),
                resume.getFileSize(),
                resume.getVersion(),
                resume.isDefault(),
                resume.getLabel(),
                resume.getUploadedAt()
        );
    }
}
