CREATE TABLE user_plans (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    plan VARCHAR(32) NOT NULL,
    cv_analysis_used INTEGER NOT NULL DEFAULT 0,
    cv_analysis_reset_at DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
