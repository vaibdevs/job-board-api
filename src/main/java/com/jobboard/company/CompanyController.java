package com.jobboard.company;

import com.jobboard.company.dto.CompanyResponse;
import com.jobboard.company.dto.CreateCompanyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/companies")
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request,
                                                         @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companyService.createCompany(request, userId));
    }

    @GetMapping("/companies/{id}")
    public ResponseEntity<CompanyResponse> getCompany(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.getCompany(id));
    }
}
