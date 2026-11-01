import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiRequest } from "../../lib/api";
import type { AuthUser } from "../../lib/auth";
import { queryKeys } from "../../lib/queryKeys";
import { useAuth } from "./AuthProvider";

export function useMeQuery() {
  const { isAuthenticated } = useAuth();
  return useQuery({
    queryKey: queryKeys.me,
    queryFn: () => apiRequest<AuthUser>("/users/me"),
    enabled: isAuthenticated,
  });
}

export function useUpdateIdentityMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: {
      firstName?: string;
      lastName?: string;
      headline?: string;
      location?: string;
    }) => apiRequest<AuthUser>("/users/me/profile", { method: "PUT", body }),
    onSuccess: (user) => {
      queryClient.setQueryData(queryKeys.me, user);
    },
  });
}
