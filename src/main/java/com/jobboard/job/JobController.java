package com.jobboard.job;

import com.jobboard.job.dto.CreateJobRequest;
import com.jobboard.job.dto.JobResponse;
import com.jobboard.job.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping("/jobs")
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody CreateJobRequest request,
                                                  @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(request, userId));
    }

    @GetMapping("/jobs")
    public ResponseEntity<PageResponse<JobResponse>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postedAt") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        return ResponseEntity.ok(jobService.searchJobs(title, location, pageable));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable UUID id) {
        return ResponseEntity.ok(jobService.getJob(id));
    }

    @PutMapping("/jobs/{id}")
    public ResponseEntity<JobResponse> updateJob(@PathVariable UUID id,
                                                  @Valid @RequestBody CreateJobRequest request,
                                                  @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(jobService.updateJob(id, request, userId));
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID id,
                                           @AuthenticationPrincipal UUID userId) {
        jobService.deleteJob(id, userId);
        return ResponseEntity.noContent().build();
    }
}
