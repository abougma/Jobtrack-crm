import type { TranslationKey } from "../../lib/i18n";
import {
  LANGUAGE_LEVEL_LABEL_KEYS,
  LANGUAGE_LEVELS,
  SKILL_CONTEXT_LABEL_KEYS,
  SKILL_CONTEXTS,
  SKILL_LEVEL_LABEL_KEYS,
  SKILL_LEVELS,
} from "../../lib/domain";
import type { ProfileCollectionKind } from "./types";

export interface ProfileField {
  name: string;
  labelKey: TranslationKey;
  type: "text" | "date" | "textarea" | "select";
  required?: boolean;
  options?: { value: string; labelKey: TranslationKey }[];
  colSpan?: 1 | 2;
}

export interface ProfileSectionConfig {
  kind: ProfileCollectionKind;
  titleKey: TranslationKey;
  emptyKey: TranslationKey;
  fields: ProfileField[];
}

export const PROFILE_SECTIONS: ProfileSectionConfig[] = [
  {
    kind: "experiences",
    titleKey: "profile.experiences",
    emptyKey: "profile.empty.experiences",
    fields: [
      { name: "jobTitle", labelKey: "profile.experience.jobTitle", type: "text", required: true },
      { name: "company", labelKey: "profile.experience.company", type: "text", required: true },
      { name: "startedOn", labelKey: "profile.experience.startedOn", type: "date", required: true },
      { name: "endedOn", labelKey: "profile.experience.endedOn", type: "date" },
      {
        name: "description",
        labelKey: "profile.experience.description",
        type: "textarea",
        required: true,
        colSpan: 2,
      },
    ],
  },
  {
    kind: "educations",
    titleKey: "profile.educations",
    emptyKey: "profile.empty.educations",
    fields: [
      { name: "degree", labelKey: "profile.education.degree", type: "text", required: true },
      {
        name: "institution",
        labelKey: "profile.education.institution",
        type: "text",
        required: true,
      },
      { name: "startedOn", labelKey: "profile.education.startedOn", type: "date", required: true },
      { name: "endedOn", labelKey: "profile.education.endedOn", type: "date" },
      {
        name: "description",
        labelKey: "profile.education.description",
        type: "textarea",
        colSpan: 2,
      },
    ],
  },
  {
    kind: "projects",
    titleKey: "profile.projects",
    emptyKey: "profile.empty.projects",
    fields: [
      { name: "name", labelKey: "profile.project.name", type: "text", required: true },
      {
        name: "technologies",
        labelKey: "profile.project.technologies",
        type: "text",
      },
      {
        name: "description",
        labelKey: "profile.project.description",
        type: "textarea",
        required: true,
        colSpan: 2,
      },
    ],
  },
  {
    kind: "skills",
    titleKey: "profile.skills",
    emptyKey: "profile.empty.skills",
    fields: [
      { name: "name", labelKey: "profile.skill.name", type: "text", required: true },
      {
        name: "level",
        labelKey: "profile.skill.level",
        type: "select",
        required: true,
        options: SKILL_LEVELS.map((level) => ({
          value: level,
          labelKey: SKILL_LEVEL_LABEL_KEYS[level],
        })),
      },
      {
        name: "context",
        labelKey: "profile.skill.context",
        type: "select",
        required: true,
        options: SKILL_CONTEXTS.map((context) => ({
          value: context,
          labelKey: SKILL_CONTEXT_LABEL_KEYS[context],
        })),
      },
    ],
  },
  {
    kind: "languages",
    titleKey: "profile.languages",
    emptyKey: "profile.empty.languages",
    fields: [
      { name: "name", labelKey: "profile.language.name", type: "text", required: true },
      {
        name: "level",
        labelKey: "profile.language.level",
        type: "select",
        required: true,
        options: LANGUAGE_LEVELS.map((level) => ({
          value: level,
          labelKey: LANGUAGE_LEVEL_LABEL_KEYS[level],
        })),
      },
    ],
  },
];
