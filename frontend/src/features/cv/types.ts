export interface CvAnalysis {
  score: number;
  coveredSkills: string[];
  missingSkills: string[];
  missingInformation: string[];
  alerts: string[];
  experienceGap: string;
  suggestions: string[];
}

export interface CvExperience {
  jobTitle: string;
  company: string;
  startedOn: string | null;
  endedOn: string | null;
  description: string | null;
}

export interface CvProject {
  name: string;
  description: string | null;
  technologies: string | null;
}

export interface CvEducation {
  degree: string;
  institution: string;
  startedOn: string | null;
  endedOn: string | null;
}

export interface GeneratedCv {
  id: string;
  userId?: string;
  applicationId: string | null;
  title: string;
  fullName: string | null;
  location: string | null;
  email?: string | null;
  phone?: string | null;
  linkedinUrl?: string | null;
  portfolioUrl?: string | null;
  headline: string | null;
  summary: string | null;
  skills: string[];
  skillGroups?: Record<string, string[]>;
  experiences: CvExperience[];
  projects: CvProject[];
  educations: CvEducation[];
  missingInformation: string[];
  score: number;
  createdAt: string;
}

export interface CvSummary {
  id: string;
  title: string;
  applicationId: string | null;
  score: number;
  createdAt: string;
}

export interface CvAnalyzePayload {
  offerText: string;
  applicationId?: string | null;
}

export interface CvGeneratePayload extends CvAnalyzePayload {
  title?: string | null;
}

export interface CvProfileGeneratePayload {
  title: string;
  fullName: string;
  location?: string | null;
  summary: string;
}

export type CvExportFormat = "docx" | "pdf" | "txt";
