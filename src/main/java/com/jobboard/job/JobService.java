package com.jobboard.job;

import com.jobboard.company.CompanyEntity;
import com.jobboard.company.CompanyRepository;
import com.jobboard.exception.ResourceNotFoundException;
import com.jobboard.job.dto.CreateJobRequest;
import com.jobboard.job.dto.JobResponse;
import com.jobboard.job.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    public JobResponse createJob(CreateJobRequest request, UUID userId) {
        CompanyEntity company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        checkOwnership(company, userId);

        JobEntity job = JobEntity.builder()
                .company(company)
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .status(JobStatus.OPEN)
                .build();

        job = jobRepository.save(job);
        return JobResponse.from(job);
    }

    public PageResponse<JobResponse> searchJobs(String title, String location, Pageable pageable) {
        Specification<JobEntity> spec = Specification.where(JobSpecification.hasStatus(JobStatus.OPEN));

        if (title != null && !title.isBlank()) {
            spec = spec.and(JobSpecification.titleContains(title));
        }
        if (location != null && !location.isBlank()) {
            spec = spec.and(JobSpecification.locationContains(location));
        }

        Page<JobEntity> page = jobRepository.findAll(spec, pageable);
        List<JobResponse> content = page.getContent().stream().map(JobResponse::from).toList();
        return PageResponse.from(page, content);
    }

    public JobResponse getJob(UUID jobId) {
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return JobResponse.from(job);
    }

    public JobResponse updateJob(UUID jobId, CreateJobRequest request, UUID userId) {
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        checkOwnership(job.getCompany(), userId);

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());

        job = jobRepository.save(job);
        return JobResponse.from(job);
    }

    public void deleteJob(UUID jobId, UUID userId) {
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        checkOwnership(job.getCompany(), userId);
        jobRepository.delete(job);
    }

    public JobResponse updateJobStatus(UUID jobId, JobStatus status) {
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        job.setStatus(status);
        job = jobRepository.save(job);
        return JobResponse.from(job);
    }

    private void checkOwnership(CompanyEntity company, UUID userId) {
        if (!company.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You do not own this company");
        }
    }
}
