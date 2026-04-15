package com.jobboard.admin;

import com.jobboard.admin.dto.StatsResponse;
import com.jobboard.admin.dto.UserResponse;
import com.jobboard.application.ApplicationRepository;
import com.jobboard.company.CompanyRepository;
import com.jobboard.exception.BadRequestException;
import com.jobboard.exception.ResourceNotFoundException;
import com.jobboard.job.JobRepository;
import com.jobboard.job.JobService;
import com.jobboard.job.JobStatus;
import com.jobboard.job.dto.JobResponse;
import com.jobboard.user.UserEntity;
import com.jobboard.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final JobService jobService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/jobs/{id}/status")
    public ResponseEntity<JobResponse> updateJobStatus(@PathVariable UUID id,
                                                        @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        if (statusStr == null) {
            throw new BadRequestException("Status is required");
        }

        JobStatus status;
        try {
            status = JobStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status. Must be OPEN, CLOSED, or DRAFT");
        }

        return ResponseEntity.ok(jobService.updateJobStatus(id, status));
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats() {
        StatsResponse stats = StatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalCompanies(companyRepository.count())
                .totalJobs(jobRepository.count())
                .totalApplications(applicationRepository.count())
                .build();
        return ResponseEntity.ok(stats);
    }
}
