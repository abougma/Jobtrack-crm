import type { LanguageLevel, SkillContext, SkillLevel } from "../../lib/domain";

export interface Experience {
  id: string;
  company: string;
  jobTitle: string;
  startedOn: string | null;
  endedOn: string | null;
  description: string | null;
}

export interface Education {
  id: string;
  institution: string;
  degree: string;
  startedOn: string | null;
  endedOn: string | null;
  description: string | null;
}

export interface Project {
  id: string;
  name: string;
  description: string | null;
  technologies: string | null;
}

export interface Skill {
  id: string;
  name: string;
  level: SkillLevel;
  context: SkillContext;
  category: string;
}

export interface Language {
  id: string;
  name: string;
  level: LanguageLevel;
}

export interface ProfileSnapshot {
  experiences: Experience[];
  educations: Education[];
  projects: Project[];
  skills: Skill[];
  languages: Language[];
}

export type ProfileCollectionKind = "experiences" | "educations" | "projects" | "skills" | "languages";

export type ProfileItem = Experience | Education | Project | Skill | Language;
