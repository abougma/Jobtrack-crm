CREATE TABLE profile_experiences (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company VARCHAR(255) NOT NULL,
    job_title VARCHAR(255) NOT NULL,
    started_on DATE NOT NULL,
    ended_on DATE,
    description TEXT NOT NULL
);

CREATE TABLE profile_educations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    institution VARCHAR(255) NOT NULL,
    degree VARCHAR(255) NOT NULL,
    started_on DATE,
    ended_on DATE,
    description TEXT
);

CREATE TABLE profile_projects (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    technologies TEXT
);

CREATE TABLE profile_skills (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    level VARCHAR(32) NOT NULL,
    context VARCHAR(32) NOT NULL
);

CREATE TABLE profile_languages (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    level VARCHAR(32) NOT NULL
);

CREATE INDEX idx_profile_experiences_user ON profile_experiences(user_id);
CREATE INDEX idx_profile_skills_user ON profile_skills(user_id);
