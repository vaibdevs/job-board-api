package com.jobboard.company.dto;

import com.jobboard.company.CompanyEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CompanyResponse {

    private UUID id;
    private UUID ownerId;
    private String name;
    private String description;
    private String website;
    private LocalDateTime createdAt;

    public static CompanyResponse from(CompanyEntity company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .ownerId(company.getOwner().getId())
                .name(company.getName())
                .description(company.getDescription())
                .website(company.getWebsite())
                .createdAt(company.getCreatedAt())
                .build();
    }
}
