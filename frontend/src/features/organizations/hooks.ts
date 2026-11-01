import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import type { ConsentScope, OrganizationRole } from "../../lib/domain";
import {
  createOrganization,
  createOrganizationGroup,
  fetchConsent,
  fetchOrganizationDashboard,
  fetchOrganizationGroups,
  fetchOrganizationMembers,
  fetchOrganizations,
  inviteMember,
  joinOrganization,
  leaveOrganization,
  revokeConsent,
  updateConsent,
} from "./api";

function invalidateOrganization(queryClient: ReturnType<typeof useQueryClient>, id?: string) {
  void queryClient.invalidateQueries({ queryKey: queryKeys.organizations.all });
  if (id) void queryClient.invalidateQueries({ queryKey: queryKeys.organizations.detail(id) });
}

export function useOrganizationsQuery() {
  return useQuery({ queryKey: queryKeys.organizations.all, queryFn: fetchOrganizations });
}

export function useOrganizationGroupsQuery(id: string | undefined) {
  return useQuery({
    queryKey: queryKeys.organizations.groups(id ?? ""),
    queryFn: () => fetchOrganizationGroups(id!),
    enabled: Boolean(id),
  });
}

export function useOrganizationMembersQuery(id: string | undefined, enabled = true) {
  return useQuery({
    queryKey: queryKeys.organizations.members(id ?? ""),
    queryFn: () => fetchOrganizationMembers(id!),
    enabled: Boolean(id) && enabled,
  });
}

export function useOrganizationDashboardQuery(id: string | undefined, groupId?: string | null) {
  return useQuery({
    queryKey: queryKeys.organizations.dashboard(id ?? "", groupId ?? undefined),
    queryFn: () => fetchOrganizationDashboard(id!, groupId),
    enabled: Boolean(id),
  });
}

export function useConsentQuery(id: string | undefined) {
  return useQuery({
    queryKey: [...queryKeys.organizations.detail(id ?? ""), "consent"] as const,
    queryFn: () => fetchConsent(id!),
    enabled: Boolean(id),
  });
}

export function useCreateOrganizationMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (name: string) => createOrganization(name),
    onSuccess: () => invalidateOrganization(queryClient),
  });
}

export function useJoinOrganizationMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (token: string) => joinOrganization(token),
    onSuccess: () => invalidateOrganization(queryClient),
  });
}

export function useCreateGroupMutation(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (name: string) => createOrganizationGroup(id, name),
    onSuccess: () => invalidateOrganization(queryClient, id),
  });
}

export function useLeaveOrganizationMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => leaveOrganization(id),
    onSuccess: () => invalidateOrganization(queryClient),
  });
}

export function useUpdateConsentMutation(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (scope: ConsentScope) => updateConsent(id, scope),
    onSuccess: () => invalidateOrganization(queryClient, id),
  });
}

export function useRevokeConsentMutation(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => revokeConsent(id),
    onSuccess: () => invalidateOrganization(queryClient, id),
  });
}

export function useInviteMemberMutation(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ email, role }: { email: string; role: OrganizationRole }) =>
      inviteMember(id, email, role),
    onSuccess: () => invalidateOrganization(queryClient, id),
  });
}
