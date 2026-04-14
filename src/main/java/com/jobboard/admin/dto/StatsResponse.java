package com.jobboard.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StatsResponse {

    private long totalUsers;
    private long totalCompanies;
    private long totalJobs;
    private long totalApplications;
}
