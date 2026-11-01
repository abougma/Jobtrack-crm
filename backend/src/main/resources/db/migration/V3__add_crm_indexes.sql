CREATE INDEX idx_reminders_due_at ON reminders(due_at);
CREATE INDEX idx_interactions_application ON interactions(application_id, occurred_at DESC);
