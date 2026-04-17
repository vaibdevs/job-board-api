package com.jobboard.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobboard.application.dto.ApplicationResponse;
import com.jobboard.application.dto.UpdateStatusRequest;
import com.jobboard.auth.JwtTokenProvider;
import com.jobboard.exception.ConflictException;
import com.jobboard.file.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private JwtTokenProvider jwtUtil;

    private final UUID jobId = UUID.randomUUID();
    private final UUID applicationId = UUID.randomUUID();
    private final UUID applicantId = UUID.randomUUID();

    private ApplicationResponse sampleApplication() {
        return new ApplicationResponse(applicationId, jobId, "Java Developer",
                applicantId, ApplicationStatus.PENDING, "/files/resume.pdf", LocalDateTime.now());
    }

    @Test
    void apply_success_returns201() throws Exception {
        MockMultipartFile resume = new MockMultipartFile("resume", "resume.pdf",
                "application/pdf", "fake pdf".getBytes());

        when(applicationService.apply(eq(jobId), any(), any())).thenReturn(sampleApplication());

        mockMvc.perform(multipart("/jobs/{jobId}/apply", jobId)
                        .file(resume))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobTitle").value("Java Developer"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void apply_duplicate_returns409() throws Exception {
        MockMultipartFile resume = new MockMultipartFile("resume", "resume.pdf",
                "application/pdf", "fake pdf".getBytes());

        when(applicationService.apply(eq(jobId), any(), any()))
                .thenThrow(new ConflictException("You have already applied to this job"));

        mockMvc.perform(multipart("/jobs/{jobId}/apply", jobId)
                        .file(resume))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You have already applied to this job"));
    }

    @Test
    void getMyApplications_returns200() throws Exception {
        when(applicationService.getMyApplications(any())).thenReturn(List.of(sampleApplication()));

        mockMvc.perform(get("/applications/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void updateStatus_returns200() throws Exception {
        ApplicationResponse updated = new ApplicationResponse(applicationId, jobId, "Java Developer",
                applicantId, ApplicationStatus.ACCEPTED, "/files/resume.pdf", LocalDateTime.now());

        when(applicationService.updateStatus(eq(applicationId), eq(ApplicationStatus.ACCEPTED), any()))
                .thenReturn(updated);

        UpdateStatusRequest request = new UpdateStatusRequest("ACCEPTED");

        mockMvc.perform(patch("/applications/{id}/status", applicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void withdrawApplication_returns204() throws Exception {
        mockMvc.perform(delete("/applications/{id}", applicationId))
                .andExpect(status().isNoContent());
    }
}
