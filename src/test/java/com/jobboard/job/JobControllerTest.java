package com.jobboard.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobboard.auth.JwtTokenProvider;
import com.jobboard.job.dto.CreateJobRequest;
import com.jobboard.job.dto.JobResponse;
import com.jobboard.job.dto.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobService jobService;

    @MockBean
    private JwtTokenProvider jwtUtil;

    private final UUID companyId = UUID.randomUUID();
    private final UUID jobId = UUID.randomUUID();

    private JobResponse sampleJob() {
        return new JobResponse(jobId, companyId, "Acme Corp", "Java Developer",
                "Build REST APIs", "Bengaluru", JobStatus.OPEN, LocalDateTime.now());
    }

    @Test
    void searchJobs_returns200() throws Exception {
        PageResponse<JobResponse> page = PageResponse.<JobResponse>builder()
                .content(List.of(sampleJob()))
                .page(0).size(10).totalElements(1).totalPages(1).last(true)
                .build();
        when(jobService.searchJobs(any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/jobs")
                        .param("title", "Java")
                        .param("location", "Bengaluru"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Java Developer"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getJob_returns200() throws Exception {
        when(jobService.getJob(jobId)).thenReturn(sampleJob());

        mockMvc.perform(get("/jobs/{id}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java Developer"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void createJob_returns201() throws Exception {
        CreateJobRequest request = new CreateJobRequest(companyId, "Java Developer", "Build REST APIs", "Bengaluru");
        when(jobService.createJob(any(), any())).thenReturn(sampleJob());

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Java Developer"));
    }

    @Test
    void deleteJob_returns204() throws Exception {
        mockMvc.perform(delete("/jobs/{id}", jobId))
                .andExpect(status().isNoContent());
    }
}
