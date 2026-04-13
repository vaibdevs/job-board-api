package com.jobboard.application;

import com.jobboard.application.dto.ApplicationResponse;
import com.jobboard.application.dto.UpdateStatusRequest;
import com.jobboard.exception.BadRequestException;
import com.jobboard.file.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final FileStorageService fileStorageService;

    @PostMapping("/jobs/{jobId}/apply")
    public ResponseEntity<ApplicationResponse> apply(@PathVariable UUID jobId,
                                                      @RequestParam("resume") MultipartFile resume,
                                                      @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.apply(jobId, userId, resume));
    }

    @GetMapping("/applications/mine")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(applicationService.getMyApplications(userId));
    }

    @DeleteMapping("/applications/{id}")
    public ResponseEntity<Void> withdrawApplication(@PathVariable UUID id,
                                                     @AuthenticationPrincipal UUID userId) {
        applicationService.withdrawApplication(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/jobs/{jobId}/applications")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsForJob(@PathVariable UUID jobId,
                                                                            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(applicationService.getApplicationsForJob(jobId, userId));
    }

    @PatchMapping("/applications/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(@PathVariable UUID id,
                                                             @Valid @RequestBody UpdateStatusRequest request,
                                                             @AuthenticationPrincipal UUID userId) {
        ApplicationStatus status;
        try {
            status = ApplicationStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status. Must be PENDING, REVIEWED, ACCEPTED, or REJECTED");
        }
        return ResponseEntity.ok(applicationService.updateStatus(id, status, userId));
    }

    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = fileStorageService.load(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
