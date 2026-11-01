import { apiRequest } from "../../lib/api";
import type { DashboardData } from "./types";

export function fetchDashboard(): Promise<DashboardData> {
  return apiRequest<DashboardData>("/dashboard");
}
