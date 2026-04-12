package com.jobboard.application.dto;

import com.jobboard.application.ApplicationEntity;
import com.jobboard.application.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ApplicationResponse {

    private UUID id;
    private UUID jobId;
    private String jobTitle;
    private UUID applicantId;
    private ApplicationStatus status;
    private String resumeUrl;
    private LocalDateTime appliedAt;

    public static ApplicationResponse from(ApplicationEntity application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .applicantId(application.getApplicant().getId())
                .status(application.getStatus())
                .resumeUrl("/files/" + application.getResumeUrl())
                .appliedAt(application.getAppliedAt())
                .build();
    }
}
