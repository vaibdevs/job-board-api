package com.jobboard.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, UUID> {

    boolean existsByJobIdAndApplicantId(UUID jobId, UUID applicantId);

    List<ApplicationEntity> findAllByJobId(UUID jobId);

    List<ApplicationEntity> findAllByApplicantId(UUID applicantId);
}
