CREATE TABLE applications (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id       UUID         NOT NULL,
    applicant_id UUID         NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    resume_url   VARCHAR(500) NOT NULL,
    applied_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT applications_job_fk       FOREIGN KEY (job_id)       REFERENCES jobs(id)  ON DELETE CASCADE,
    CONSTRAINT applications_user_fk      FOREIGN KEY (applicant_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT applications_status_check CHECK (status IN ('PENDING', 'REVIEWED', 'ACCEPTED', 'REJECTED')),
    CONSTRAINT applications_no_duplicate UNIQUE (job_id, applicant_id)
);

CREATE INDEX idx_applications_job_id       ON applications(job_id);
CREATE INDEX idx_applications_applicant_id ON applications(applicant_id);
CREATE INDEX idx_applications_status       ON applications(status);
