package com.jobboard.job;

import org.springframework.data.jpa.domain.Specification;

public class JobSpecification {

    private JobSpecification() {
    }

    public static Specification<JobEntity> hasStatus(JobStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<JobEntity> titleContains(String title) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<JobEntity> locationContains(String location) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }
}
