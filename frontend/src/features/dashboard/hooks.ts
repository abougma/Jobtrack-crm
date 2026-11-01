import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { fetchDashboard } from "./api";

export function useDashboardQuery() {
  return useQuery({
    queryKey: queryKeys.dashboard,
    queryFn: fetchDashboard,
    staleTime: 30_000,
  });
}
