package com.Jobtrackr.jta.resume.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ResumeResponse {
    private UUID id;
    private String fileName;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private Integer version;
    private boolean isDefault;
    private String label;
    private LocalDateTime uploadedAt;

    public ResumeResponse() {}

    public ResumeResponse(UUID id, String fileName, String originalFileName, String contentType,
                          Long fileSize, Integer version, boolean isDefault, String label,
                          LocalDateTime uploadedAt) {
        this.id = id;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.version = version;
        this.isDefault = isDefault;
        this.label = label;
        this.uploadedAt = uploadedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
