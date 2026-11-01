import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { deleteAccount, fetchPlan } from "./api";

export function usePlanQuery() {
  return useQuery({ queryKey: queryKeys.plan, queryFn: fetchPlan });
}

export function useDeleteAccountMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteAccount,
    onSuccess: () => queryClient.clear(),
  });
}
