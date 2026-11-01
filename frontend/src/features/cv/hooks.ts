import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { analyzeCv, deleteCv, fetchCv, fetchCvs, generateCv, generateCvFromProfile } from "./api";
import type { CvAnalyzePayload, CvGeneratePayload, CvProfileGeneratePayload } from "./types";

export function useCvListQuery() {
  return useQuery({ queryKey: queryKeys.cv.list, queryFn: fetchCvs });
}

export function useCvQuery(id: string | undefined) {
  return useQuery({
    queryKey: queryKeys.cv.detail(id ?? ""),
    queryFn: () => fetchCv(id!),
    enabled: Boolean(id),
  });
}

export function useAnalyzeCvMutation() {
  return useMutation({ mutationFn: (payload: CvAnalyzePayload) => analyzeCv(payload) });
}

export function useGenerateCvMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CvGeneratePayload) => generateCv(payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.cv.all });
      void queryClient.invalidateQueries({ queryKey: queryKeys.plan });
    },
  });
}

export function useGenerateCvFromProfileMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CvProfileGeneratePayload) => generateCvFromProfile(payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.cv.all });
      void queryClient.invalidateQueries({ queryKey: queryKeys.plan });
    },
  });
}

export function useDeleteCvMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteCv(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.cv.all });
    },
  });
}
