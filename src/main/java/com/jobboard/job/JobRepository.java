package com.jobboard.job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface JobRepository extends JpaRepository<JobEntity, UUID>, JpaSpecificationExecutor<JobEntity> {
}
