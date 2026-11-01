import { apiRequest } from "../../lib/api";
import type {
  AdminOrganization,
  AdminOrganizationFilters,
  AdminOverview,
  AdminUser,
  AdminUserDetail,
  AdminUserFilters,
  Page,
} from "./types";

function queryString(params: Record<string, unknown>): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      search.set(key, String(value));
    }
  });
  const query = search.toString();
  return query ? `?${query}` : "";
}

export function fetchAdminOverview(): Promise<AdminOverview> {
  return apiRequest<AdminOverview>("/admin/overview");
}

export function fetchAdminUsers(filters: AdminUserFilters): Promise<Page<AdminUser>> {
  return apiRequest<Page<AdminUser>>(`/admin/users${queryString({ ...filters })}`);
}

export function fetchAdminUser(id: string): Promise<AdminUserDetail> {
  return apiRequest<AdminUserDetail>(`/admin/users/${id}`);
}

export function createAdminUser(payload: {
  email: string;
  password: string;
  role: string;
}): Promise<AdminUser> {
  return apiRequest<AdminUser>("/admin/users", { method: "POST", body: payload });
}

export function updateAdminUserPlan(id: string, plan: string): Promise<AdminUser> {
  return apiRequest<AdminUser>(`/admin/users/${id}/plan`, { method: "PATCH", body: { plan } });
}

export function updateAdminUserRole(id: string, role: string): Promise<AdminUser> {
  return apiRequest<AdminUser>(`/admin/users/${id}/role`, { method: "PATCH", body: { role } });
}

export function fetchAdminOrganizations(
  filters: AdminOrganizationFilters,
): Promise<Page<AdminOrganization>> {
  return apiRequest<Page<AdminOrganization>>(`/admin/organizations${queryString({ ...filters })}`);
}
