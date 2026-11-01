import { apiDownload, apiRequest, buildQuery } from "../../lib/api";
import type {
  ApplicationDetail,
  ApplicationFilters,
  ApplicationPayload,
  ApplicationSummary,
  ImportResult,
  Interaction,
  InteractionPayload,
  PageResponse,
  Reminder,
  ReminderPayload,
  StatusChange,
} from "./types";

function filterQuery(filters: Partial<ApplicationFilters>): string {
  return buildQuery({
    page: filters.page,
    size: filters.size,
    sort: filters.sort,
    status: filters.status || undefined,
    source: filters.source || undefined,
    contractType: filters.contractType || undefined,
    workMode: filters.workMode || undefined,
    search: filters.search || undefined,
    from: filters.from || undefined,
    to: filters.to || undefined,
  });
}

export function fetchApplications(filters: ApplicationFilters): Promise<PageResponse<ApplicationSummary>> {
  return apiRequest<PageResponse<ApplicationSummary>>(`/applications${filterQuery(filters)}`);
}

export function fetchApplication(id: string): Promise<ApplicationDetail> {
  return apiRequest<ApplicationDetail>(`/applications/${id}`);
}

export function createApplication(payload: ApplicationPayload): Promise<ApplicationDetail> {
  return apiRequest<ApplicationDetail>("/applications", { method: "POST", body: payload });
}

export function updateApplication(
  id: string,
  payload: ApplicationPayload,
): Promise<ApplicationDetail> {
  return apiRequest<ApplicationDetail>(`/applications/${id}`, { method: "PUT", body: payload });
}

export function updateApplicationStatus(
  id: string,
  status: ApplicationSummary["status"],
): Promise<ApplicationDetail> {
  return apiRequest<ApplicationDetail>(`/applications/${id}/status`, {
    method: "PATCH",
    body: { status },
  });
}

export function deleteApplication(id: string): Promise<void> {
  return apiRequest<void>(`/applications/${id}`, { method: "DELETE" });
}

export function fetchHistory(id: string): Promise<StatusChange[]> {
  return apiRequest<StatusChange[]>(`/applications/${id}/history`);
}

export function fetchInteractions(id: string): Promise<Interaction[]> {
  return apiRequest<Interaction[]>(`/applications/${id}/interactions`);
}

export function createInteraction(id: string, payload: InteractionPayload): Promise<Interaction> {
  return apiRequest<Interaction>(`/applications/${id}/interactions`, {
    method: "POST",
    body: payload,
  });
}

export function deleteInteraction(applicationId: string, interactionId: string): Promise<void> {
  return apiRequest<void>(`/applications/${applicationId}/interactions/${interactionId}`, {
    method: "DELETE",
  });
}

export function fetchReminders(id: string): Promise<Reminder[]> {
  return apiRequest<Reminder[]>(`/applications/${id}/reminders`);
}

export function createReminder(id: string, payload: ReminderPayload): Promise<Reminder> {
  return apiRequest<Reminder>(`/applications/${id}/reminders`, { method: "POST", body: payload });
}

export function completeReminder(applicationId: string, reminderId: string): Promise<Reminder> {
  return apiRequest<Reminder>(`/applications/${applicationId}/reminders/${reminderId}/complete`, {
    method: "PATCH",
  });
}

export function deleteReminder(applicationId: string, reminderId: string): Promise<void> {
  return apiRequest<void>(`/applications/${applicationId}/reminders/${reminderId}`, {
    method: "DELETE",
  });
}

export function exportApplicationsCsv(filters: Partial<ApplicationFilters>) {
  return apiDownload(`/applications/export.csv${filterQuery(filters)}`, "jobtrack-candidatures.csv");
}

export function importApplicationsCsv(file: File): Promise<ImportResult> {
  const formData = new FormData();
  formData.append("file", file);
  return apiRequest<ImportResult>("/applications/import", { method: "POST", body: formData });
}
