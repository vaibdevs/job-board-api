package com.jobboard.application;

import com.jobboard.application.dto.ApplicationResponse;
import com.jobboard.exception.BadRequestException;
import com.jobboard.exception.ConflictException;
import com.jobboard.exception.ResourceNotFoundException;
import com.jobboard.file.FileStorageService;
import com.jobboard.job.JobEntity;
import com.jobboard.job.JobRepository;
import com.jobboard.job.JobStatus;
import com.jobboard.user.UserEntity;
import com.jobboard.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public ApplicationResponse apply(UUID jobId, UUID userId, MultipartFile file) {
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getStatus() != JobStatus.OPEN) {
            throw new BadRequestException("Job is not open for applications");
        }

        if (applicationRepository.existsByJobIdAndApplicantId(jobId, userId)) {
            throw new ConflictException("You have already applied to this job");
        }

        UserEntity applicant = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String resumeUrl = fileStorageService.store(file, userId);

        ApplicationEntity application = ApplicationEntity.builder()
                .job(job)
                .applicant(applicant)
                .status(ApplicationStatus.PENDING)
                .resumeUrl(resumeUrl)
                .build();

        application = applicationRepository.save(application);
        return ApplicationResponse.from(application);
    }

    public List<ApplicationResponse> getMyApplications(UUID userId) {
        return applicationRepository.findAllByApplicantId(userId).stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    public void withdrawApplication(UUID applicationId, UUID userId) {
        ApplicationEntity application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getApplicant().getId().equals(userId)) {
            throw new AccessDeniedException("You do not own this application");
        }

        applicationRepository.delete(application);
    }

    public List<ApplicationResponse> getApplicationsForJob(UUID jobId, UUID userId) {
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getCompany().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You do not own this job");
        }

        return applicationRepository.findAllByJobId(jobId).stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    public ApplicationResponse updateStatus(UUID applicationId, ApplicationStatus newStatus, UUID userId) {
        ApplicationEntity application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getJob().getCompany().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You do not own this job");
        }

        application.setStatus(newStatus);
        application = applicationRepository.save(application);
        return ApplicationResponse.from(application);
    }
}
