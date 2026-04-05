CREATE TABLE jobs (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id  UUID         NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    location    VARCHAR(255) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    posted_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT jobs_company_fk     FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT jobs_status_check   CHECK (status IN ('OPEN', 'CLOSED', 'DRAFT')),
    CONSTRAINT jobs_title_check    CHECK (CHAR_LENGTH(TRIM(title)) > 0),
    CONSTRAINT jobs_location_check CHECK (CHAR_LENGTH(TRIM(location)) > 0)
);

CREATE INDEX idx_jobs_company_id ON jobs(company_id);
CREATE INDEX idx_jobs_status     ON jobs(status);
CREATE INDEX idx_jobs_title      ON jobs(title);
CREATE INDEX idx_jobs_location   ON jobs(location);
CREATE INDEX idx_jobs_posted_at  ON jobs(posted_at DESC);
