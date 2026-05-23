package com.Jobtrackr.jta.verification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {
    
    private boolean success;
    private String message;
    private Integer remainingAttempts;
    private Boolean canResend;
}