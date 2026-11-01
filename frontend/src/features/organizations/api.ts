import { apiRequest, buildQuery } from "../../lib/api";
import type { ConsentScope, OrganizationRole } from "../../lib/domain";
import type {
  ConsentView,
  InvitationResult,
  Organization,
  OrganizationDashboard,
  OrganizationGroup,
  OrganizationMember,
} from "./types";

export function fetchOrganizations(): Promise<Organization[]> {
  return apiRequest<Organization[]>("/organizations");
}

export function createOrganization(name: string): Promise<Organization> {
  return apiRequest<Organization>("/organizations", { method: "POST", body: { name } });
}

export function fetchOrganization(id: string): Promise<Organization> {
  return apiRequest<Organization>(`/organizations/${id}`);
}

export function fetchOrganizationGroups(id: string): Promise<OrganizationGroup[]> {
  return apiRequest<OrganizationGroup[]>(`/organizations/${id}/groups`);
}

export function createOrganizationGroup(id: string, name: string): Promise<OrganizationGroup> {
  return apiRequest<OrganizationGroup>(`/organizations/${id}/groups`, {
    method: "POST",
    body: { name },
  });
}

export function fetchOrganizationMembers(id: string): Promise<OrganizationMember[]> {
  return apiRequest<OrganizationMember[]>(`/organizations/${id}/members`);
}

export function leaveOrganization(id: string): Promise<void> {
  return apiRequest<void>(`/organizations/${id}/members/me`, { method: "DELETE" });
}

export function fetchConsent(id: string): Promise<ConsentView> {
  return apiRequest<ConsentView>(`/organizations/${id}/consent`);
}

export function updateConsent(id: string, scope: ConsentScope): Promise<ConsentView> {
  return apiRequest<ConsentView>(`/organizations/${id}/consent`, {
    method: "PUT",
    body: { scope },
  });
}

export function revokeConsent(id: string): Promise<void> {
  return apiRequest<void>(`/organizations/${id}/consent`, { method: "DELETE" });
}

export function inviteMember(
  id: string,
  email: string,
  role: OrganizationRole,
): Promise<InvitationResult> {
  return apiRequest<InvitationResult>(`/organizations/${id}/invitations`, {
    method: "POST",
    body: { email, role },
  });
}

export function joinOrganization(token: string): Promise<Organization> {
  return apiRequest<Organization>("/organizations/join", { method: "POST", body: { token } });
}

export function fetchOrganizationDashboard(
  id: string,
  groupId?: string | null,
): Promise<OrganizationDashboard> {
  return apiRequest<OrganizationDashboard>(
    `/organizations/${id}/dashboard${buildQuery({ groupId })}`,
  );
}
