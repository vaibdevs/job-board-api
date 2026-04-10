package com.jobboard.job.dto;

import com.jobboard.job.JobEntity;
import com.jobboard.job.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class JobResponse {

    private UUID id;
    private UUID companyId;
    private String companyName;
    private String title;
    private String description;
    private String location;
    private JobStatus status;
    private LocalDateTime postedAt;

    public static JobResponse from(JobEntity job) {
        return JobResponse.builder()
                .id(job.getId())
                .companyId(job.getCompany().getId())
                .companyName(job.getCompany().getName())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .status(job.getStatus())
                .postedAt(job.getPostedAt())
                .build();
    }
}
