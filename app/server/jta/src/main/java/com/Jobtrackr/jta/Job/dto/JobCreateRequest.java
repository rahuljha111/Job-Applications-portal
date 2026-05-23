package com.Jobtrackr.jta.Job.dto;

import com.Jobtrackr.jta.Job.entity.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JobCreateRequest {

    @NotBlank(message = "Job title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    @NotBlank(message = "Job description is required")
    @Size(min = 50, max = 5000, message = "Description must be between 50 and 5000 characters")
    private String description;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @Positive(message = "Salary must be positive")
    private Double salary;
    
    @NotNull(message = "Job type is required")
    private JobType type;
}
