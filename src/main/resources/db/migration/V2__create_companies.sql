CREATE TABLE companies (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id    UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    website     VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT companies_owner_fk   FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT companies_name_check CHECK (CHAR_LENGTH(TRIM(name)) > 0)
);

CREATE INDEX idx_companies_owner_id ON companies(owner_id);
