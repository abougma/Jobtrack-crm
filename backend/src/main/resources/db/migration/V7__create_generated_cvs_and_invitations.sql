CREATE TABLE generated_cvs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id UUID REFERENCES applications(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    content JSONB NOT NULL,
    score INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_generated_cvs_user ON generated_cvs(user_id, created_at DESC);
CREATE INDEX idx_generated_cvs_application ON generated_cvs(application_id);

CREATE TABLE organization_invitations (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    email VARCHAR(320) NOT NULL,
    role VARCHAR(32) NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    accepted_at TIMESTAMPTZ,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_organization_invitations_organization ON organization_invitations(organization_id);

CREATE INDEX idx_applications_user_status ON applications(user_id, status);
CREATE INDEX idx_applications_user_source ON applications(user_id, source);
CREATE INDEX idx_reminders_application_completed ON reminders(application_id, completed, due_at);
