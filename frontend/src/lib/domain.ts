import type { TranslationKey } from "./translations/fr";

export const APPLICATION_STATUSES = [
  "TO_APPLY",
  "CV_READY",
  "APPLIED",
  "FOLLOWED_UP",
  "INTERVIEW",
  "OFFER",
  "REJECTED",
  "NO_RESPONSE",
  "WITHDRAWN",
] as const;
export type ApplicationStatus = (typeof APPLICATION_STATUSES)[number];

export const WORK_MODES = ["ON_SITE", "REMOTE", "HYBRID"] as const;
export type WorkMode = (typeof WORK_MODES)[number];

export const CONTRACT_TYPES = [
  "INTERNSHIP",
  "APPRENTICESHIP",
  "FIXED_TERM",
  "PERMANENT",
  "FREELANCE",
  "OTHER",
] as const;
export type ContractType = (typeof CONTRACT_TYPES)[number];

export const INTERACTION_TYPES = [
  "MESSAGE",
  "FOLLOW_UP",
  "INTERVIEW",
  "CALL",
  "EMAIL",
  "NOTE",
] as const;
export type InteractionType = (typeof INTERACTION_TYPES)[number];

export const SKILL_LEVELS = ["BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"] as const;
export type SkillLevel = (typeof SKILL_LEVELS)[number];

export const SKILL_CONTEXTS = ["PROFESSIONAL", "PERSONAL", "ACADEMIC"] as const;
export type SkillContext = (typeof SKILL_CONTEXTS)[number];

export const LANGUAGE_LEVELS = ["A1", "A2", "B1", "B2", "C1", "C2", "NATIVE"] as const;
export type LanguageLevel = (typeof LANGUAGE_LEVELS)[number];

export const CONSENT_SCOPES = ["AGGREGATED", "APPLICATION_DETAILS"] as const;
export type ConsentScope = (typeof CONSENT_SCOPES)[number];

export const ORGANIZATION_ROLES = ["ORG_ADMIN", "ORG_COACH", "MEMBER"] as const;
export type OrganizationRole = (typeof ORGANIZATION_ROLES)[number];

export const STATUS_LABEL_KEYS: Record<ApplicationStatus, TranslationKey> = {
  TO_APPLY: "status.TO_APPLY",
  CV_READY: "status.CV_READY",
  APPLIED: "status.APPLIED",
  FOLLOWED_UP: "status.FOLLOWED_UP",
  INTERVIEW: "status.INTERVIEW",
  OFFER: "status.OFFER",
  REJECTED: "status.REJECTED",
  NO_RESPONSE: "status.NO_RESPONSE",
  WITHDRAWN: "status.WITHDRAWN",
};

export const WORK_MODE_LABEL_KEYS: Record<WorkMode, TranslationKey> = {
  ON_SITE: "workMode.ON_SITE",
  REMOTE: "workMode.REMOTE",
  HYBRID: "workMode.HYBRID",
};

export const CONTRACT_LABEL_KEYS: Record<ContractType, TranslationKey> = {
  INTERNSHIP: "contract.INTERNSHIP",
  APPRENTICESHIP: "contract.APPRENTICESHIP",
  FIXED_TERM: "contract.FIXED_TERM",
  PERMANENT: "contract.PERMANENT",
  FREELANCE: "contract.FREELANCE",
  OTHER: "contract.OTHER",
};

export const INTERACTION_LABEL_KEYS: Record<InteractionType, TranslationKey> = {
  MESSAGE: "interaction.MESSAGE",
  FOLLOW_UP: "interaction.FOLLOW_UP",
  INTERVIEW: "interaction.INTERVIEW",
  CALL: "interaction.CALL",
  EMAIL: "interaction.EMAIL",
  NOTE: "interaction.NOTE",
};

export const SKILL_LEVEL_LABEL_KEYS: Record<SkillLevel, TranslationKey> = {
  BEGINNER: "level.BEGINNER",
  INTERMEDIATE: "level.INTERMEDIATE",
  ADVANCED: "level.ADVANCED",
  EXPERT: "level.EXPERT",
};

export const SKILL_CONTEXT_LABEL_KEYS: Record<SkillContext, TranslationKey> = {
  PROFESSIONAL: "context.PROFESSIONAL",
  PERSONAL: "context.PERSONAL",
  ACADEMIC: "context.ACADEMIC",
};

export const LANGUAGE_LEVEL_LABEL_KEYS: Record<LanguageLevel, TranslationKey> = {
  A1: "languageLevel.A1",
  A2: "languageLevel.A2",
  B1: "languageLevel.B1",
  B2: "languageLevel.B2",
  C1: "languageLevel.C1",
  C2: "languageLevel.C2",
  NATIVE: "languageLevel.NATIVE",
};

export const ORGANIZATION_ROLE_LABEL_KEYS: Record<OrganizationRole, TranslationKey> = {
  ORG_ADMIN: "organizations.role.ORG_ADMIN",
  ORG_COACH: "organizations.role.ORG_COACH",
  MEMBER: "organizations.role.MEMBER",
};

export const CONSENT_SCOPE_LABEL_KEYS: Record<ConsentScope, TranslationKey> = {
  AGGREGATED: "organizations.scope.AGGREGATED",
  APPLICATION_DETAILS: "organizations.scope.APPLICATION_DETAILS",
};

export const STATUS_BADGE_VARIANTS: Record<
  ApplicationStatus,
  "neutral" | "info" | "success" | "warning" | "danger" | "accent"
> = {
  TO_APPLY: "neutral",
  CV_READY: "info",
  APPLIED: "info",
  FOLLOWED_UP: "accent",
  INTERVIEW: "warning",
  OFFER: "success",
  REJECTED: "danger",
  NO_RESPONSE: "neutral",
  WITHDRAWN: "danger",
};
