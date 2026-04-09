package com.jobboard.company;

import com.jobboard.company.dto.CompanyResponse;
import com.jobboard.company.dto.CreateCompanyRequest;
import com.jobboard.exception.ResourceNotFoundException;
import com.jobboard.user.UserEntity;
import com.jobboard.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyResponse createCompany(CreateCompanyRequest request, UUID ownerId) {
        UserEntity owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CompanyEntity company = CompanyEntity.builder()
                .owner(owner)
                .name(request.getName())
                .description(request.getDescription())
                .website(request.getWebsite())
                .build();

        company = companyRepository.save(company);
        return CompanyResponse.from(company);
    }

    public CompanyResponse getCompany(UUID companyId) {
        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        return CompanyResponse.from(company);
    }
}
