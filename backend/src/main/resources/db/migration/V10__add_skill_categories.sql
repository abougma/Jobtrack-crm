ALTER TABLE profile_skills ADD COLUMN category VARCHAR(100);
UPDATE profile_skills SET category = 'Autres' WHERE category IS NULL;
