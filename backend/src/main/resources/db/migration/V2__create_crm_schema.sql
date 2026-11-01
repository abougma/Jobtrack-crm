CREATE TABLE contacts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(255),
    linkedin_url VARCHAR(500),
    email VARCHAR(320),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE applications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    contact_id UUID REFERENCES contacts(id) ON DELETE SET NULL,
    company VARCHAR(255) NOT NULL,
    position VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    work_mode VARCHAR(32),
    contract_type VARCHAR(32),
    salary_min INTEGER,
    salary_max INTEGER,
    source VARCHAR(100),
    offer_url VARCHAR(1000),
    status VARCHAR(32) NOT NULL,
    applied_at DATE,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE application_status_history (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    previous_status VARCHAR(32),
    new_status VARCHAR(32) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE interactions (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    type VARCHAR(32) NOT NULL,
    message TEXT,
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE reminders (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    due_at TIMESTAMPTZ NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_applications_user_updated ON applications(user_id, updated_at DESC);
CREATE INDEX idx_application_status_history_application ON application_status_history(application_id);
