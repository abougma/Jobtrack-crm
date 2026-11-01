import { apiRequest } from "../../lib/api";
import type { PlanSnapshot } from "./types";

export function fetchPlan(): Promise<PlanSnapshot> {
  return apiRequest<PlanSnapshot>("/plan");
}

export function exportUserData(): Promise<Record<string, unknown>> {
  return apiRequest<Record<string, unknown>>("/privacy/export");
}

export function deleteAccount(): Promise<void> {
  return apiRequest<void>("/privacy/account", { method: "DELETE", body: { confirm: true } });
}
