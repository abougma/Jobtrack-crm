import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { createPlanCheckout, deleteAccount, fetchPlan } from "./api";
import type { Plan } from "../../lib/auth";
import { useAuth } from "../auth/AuthProvider";

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

export function useChangePlanMutation() {
  const { refreshUser } = useAuth();
  return useMutation({
    mutationFn: async (plan: Plan) => {
      // Refresh the access token before opening Checkout. This avoids sending
      // a stale token when the user has left the profile open for a while.
      const user = await refreshUser();
      if (!user) {
        throw new Error("Session expirée. Veuillez vous reconnecter.");
      }
      const { checkoutUrl } = await createPlanCheckout(plan);
      window.location.assign(checkoutUrl);
    },
  });
}
