package com.Jobtrackr.jta.resume.dto;

import jakarta.validation.constraints.Size;

public class ResumeUploadRequest {
    
    @Size(max = 200, message = "Label must not exceed 200 characters")
    private String label;
    
    private boolean setAsDefault = false;

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public boolean isSetAsDefault() { return setAsDefault; }
    public void setSetAsDefault(boolean setAsDefault) { this.setAsDefault = setAsDefault; }
}
