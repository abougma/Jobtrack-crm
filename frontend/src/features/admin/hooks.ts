import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import {
  createAdminUser,
  fetchAdminOrganizations,
  fetchAdminOverview,
  fetchAdminUser,
  fetchAdminUsers,
  updateAdminUserPlan,
  updateAdminUserRole,
} from "./api";
import type { AdminOrganizationFilters, AdminUserFilters } from "./types";

export function useAdminOverviewQuery() {
  return useQuery({
    queryKey: queryKeys.admin.overview,
    queryFn: fetchAdminOverview,
    staleTime: 30_000,
  });
}

export function useAdminUsersQuery(filters: AdminUserFilters) {
  return useQuery({
    queryKey: queryKeys.admin.users(filters),
    queryFn: () => fetchAdminUsers(filters),
    placeholderData: keepPreviousData,
    staleTime: 15_000,
  });
}

export function useAdminUserQuery(id: string | null) {
  return useQuery({
    queryKey: queryKeys.admin.user(id ?? "none"),
    queryFn: () => fetchAdminUser(id as string),
    enabled: Boolean(id),
    staleTime: 15_000,
  });
}

export function useAdminOrganizationsQuery(filters: AdminOrganizationFilters) {
  return useQuery({
    queryKey: queryKeys.admin.organizations(filters),
    queryFn: () => fetchAdminOrganizations(filters),
    placeholderData: keepPreviousData,
    staleTime: 30_000,
  });
}

function useAdminInvalidation() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: queryKeys.admin.all });
}

export function useCreateAdminUserMutation() {
  const invalidate = useAdminInvalidation();
  return useMutation({
    mutationFn: (payload: { email: string; password: string; role: string }) =>
      createAdminUser(payload),
    onSuccess: () => void invalidate(),
  });
}

export function useUpdateUserPlanMutation() {
  const invalidate = useAdminInvalidation();
  return useMutation({
    mutationFn: ({ id, plan }: { id: string; plan: string }) => updateAdminUserPlan(id, plan),
    onSuccess: () => void invalidate(),
  });
}

export function useUpdateUserRoleMutation() {
  const invalidate = useAdminInvalidation();
  return useMutation({
    mutationFn: ({ id, role }: { id: string; role: string }) => updateAdminUserRole(id, role),
    onSuccess: () => void invalidate(),
  });
}
