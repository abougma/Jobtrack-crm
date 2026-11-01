import { apiDownload, apiRequest, buildQuery, type DownloadedFile } from "../../lib/api";
import type {
  CvAnalysis,
  CvAnalyzePayload,
  CvExportFormat,
  CvGeneratePayload,
  CvProfileGeneratePayload,
  CvSummary,
  GeneratedCv,
} from "./types";

export function analyzeCv(payload: CvAnalyzePayload): Promise<CvAnalysis> {
  return apiRequest<CvAnalysis>("/cv/analyze", { method: "POST", body: payload });
}

export function generateCv(payload: CvGeneratePayload): Promise<GeneratedCv> {
  return apiRequest<GeneratedCv>("/cv/generate", { method: "POST", body: payload });
}

export function generateCvFromProfile(payload: CvProfileGeneratePayload): Promise<GeneratedCv> {
  return apiRequest<GeneratedCv>("/cv/generate-from-profile", { method: "POST", body: payload });
}

export function fetchCvs(): Promise<CvSummary[]> {
  return apiRequest<CvSummary[]>("/cv");
}

export function fetchCv(id: string): Promise<GeneratedCv> {
  return apiRequest<GeneratedCv>(`/cv/${id}`);
}

export function deleteCv(id: string): Promise<void> {
  return apiRequest<void>(`/cv/${id}`, { method: "DELETE" });
}

export function exportCv(id: string, format: CvExportFormat): Promise<DownloadedFile> {
  return apiDownload(`/cv/${id}/export${buildQuery({ format })}`, `cv-${id}.${format}`);
}
