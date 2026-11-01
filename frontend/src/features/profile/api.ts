import { apiRequest } from "../../lib/api";
import type {
  Education,
  Experience,
  Language,
  ProfileCollectionKind,
  ProfileSnapshot,
  Project,
  Skill,
} from "./types";

export function fetchProfile(): Promise<ProfileSnapshot> {
  return apiRequest<ProfileSnapshot>("/profile");
}

interface KindMap {
  experiences: Experience;
  educations: Education;
  projects: Project;
  skills: Skill;
  languages: Language;
}

export function createProfileItem<K extends ProfileCollectionKind>(
  kind: K,
  body: Record<string, unknown>,
): Promise<KindMap[K]> {
  return apiRequest<KindMap[K]>(`/profile/${kind}`, { method: "POST", body });
}

export function updateProfileItem<K extends ProfileCollectionKind>(
  kind: K,
  id: string,
  body: Record<string, unknown>,
): Promise<KindMap[K]> {
  return apiRequest<KindMap[K]>(`/profile/${kind}/${id}`, { method: "PUT", body });
}

export function deleteProfileItem(kind: ProfileCollectionKind, id: string): Promise<void> {
  return apiRequest<void>(`/profile/${kind}/${id}`, { method: "DELETE" });
}
