import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiRequest } from "../../lib/api";
import { queryKeys } from "../../lib/queryKeys";
import { useAuth } from "../auth/AuthProvider";
import { createProfileItem, deleteProfileItem, fetchProfile, updateProfileItem } from "./api";
import type { ProfileCollectionKind } from "./types";

export function useProfileQuery() {
  return useQuery({ queryKey: queryKeys.profile, queryFn: fetchProfile });
}

export function useCreateProfileItemMutation(kind: ProfileCollectionKind) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: Record<string, unknown>) => createProfileItem(kind, body),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.profile });
      void queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
    },
  });
}

export function useUpdateProfileItemMutation(kind: ProfileCollectionKind) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: Record<string, unknown> }) =>
      updateProfileItem(kind, id, body),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.profile });
    },
  });
}

export function useDeleteProfileItemMutation(kind: ProfileCollectionKind) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteProfileItem(kind, id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.profile });
      void queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
    },
  });
}

export interface IdentityPayload {
  firstName?: string;
  lastName?: string;
  headline?: string;
  location?: string;
}

export function useUpdateIdentityMutation() {
  const queryClient = useQueryClient();
  const { refreshUser } = useAuth();
  return useMutation({
    mutationFn: (body: IdentityPayload) =>
      apiRequest("/users/me/profile", { method: "PUT", body }),
    onSuccess: async () => {
      await refreshUser();
      void queryClient.invalidateQueries({ queryKey: queryKeys.me });
    },
  });
}
