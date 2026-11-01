import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
  type QueryClient,
} from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import type { ApplicationStatus } from "../../lib/domain";
import {
  completeReminder,
  createApplication,
  createInteraction,
  createReminder,
  deleteApplication,
  deleteInteraction,
  deleteReminder,
  fetchApplication,
  fetchApplications,
  fetchHistory,
  fetchInteractions,
  fetchReminders,
  importApplicationsCsv,
  updateApplication,
  updateApplicationStatus,
} from "./api";
import type {
  ApplicationDetail,
  ApplicationFilters,
  ApplicationPayload,
  ApplicationSummary,
  InteractionPayload,
  PageResponse,
  ReminderPayload,
} from "./types";

function invalidateApplicationData(queryClient: QueryClient, id?: string) {
  void queryClient.invalidateQueries({ queryKey: queryKeys.applications.all });
  void queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
  if (id) void queryClient.invalidateQueries({ queryKey: queryKeys.applications.detail(id) });
}

export function useApplicationsQuery(filters: ApplicationFilters, enabled = true) {
  return useQuery({
    queryKey: queryKeys.applications.list(filters as unknown as Record<string, unknown>),
    queryFn: () => fetchApplications(filters),
    placeholderData: keepPreviousData,
    enabled,
  });
}

export function useApplicationQuery(id: string | undefined) {
  return useQuery({
    queryKey: queryKeys.applications.detail(id ?? ""),
    queryFn: () => fetchApplication(id!),
    enabled: Boolean(id),
  });
}

export function useHistoryQuery(id: string | undefined) {
  return useQuery({
    queryKey: queryKeys.applications.history(id ?? ""),
    queryFn: () => fetchHistory(id!),
    enabled: Boolean(id),
  });
}

export function useInteractionsQuery(id: string | undefined) {
  return useQuery({
    queryKey: queryKeys.applications.interactions(id ?? ""),
    queryFn: () => fetchInteractions(id!),
    enabled: Boolean(id),
  });
}

export function useRemindersQuery(id: string | undefined) {
  return useQuery({
    queryKey: queryKeys.applications.reminders(id ?? ""),
    queryFn: () => fetchReminders(id!),
    enabled: Boolean(id),
  });
}

export function useCreateApplicationMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: ApplicationPayload) => createApplication(payload),
    onSuccess: () => invalidateApplicationData(queryClient),
  });
}

export function useUpdateApplicationMutation(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: ApplicationPayload) => updateApplication(id, payload),
    onSuccess: (detail) => {
      queryClient.setQueryData(queryKeys.applications.detail(id), detail);
      invalidateApplicationData(queryClient, id);
    },
  });
}

interface StatusVariables {
  id: string;
  status: ApplicationStatus;
}

interface StatusContext {
  previousLists: [readonly unknown[], PageResponse<ApplicationSummary> | undefined][];
  previousDetail: ApplicationDetail | undefined;
}

export function useUpdateStatusMutation() {
  const queryClient = useQueryClient();

  return useMutation<ApplicationDetail, unknown, StatusVariables, StatusContext>({
    mutationFn: ({ id, status }) => updateApplicationStatus(id, status),
    onMutate: async ({ id, status }) => {
      await queryClient.cancelQueries({ queryKey: queryKeys.applications.all });

      const previousLists = queryClient.getQueriesData<PageResponse<ApplicationSummary>>({
        queryKey: ["applications", "list"],
      });
      queryClient.setQueriesData<PageResponse<ApplicationSummary>>(
        { queryKey: ["applications", "list"] },
        (old) =>
          old
            ? {
                ...old,
                content: old.content.map((item) =>
                  item.id === id ? { ...item, status } : item,
                ),
              }
            : old,
      );

      const previousDetail = queryClient.getQueryData<ApplicationDetail>(
        queryKeys.applications.detail(id),
      );
      if (previousDetail) {
        queryClient.setQueryData<ApplicationDetail>(queryKeys.applications.detail(id), {
          ...previousDetail,
          status,
        });
      }

      return { previousLists, previousDetail };
    },
    onError: (_error, variables, context) => {
      context?.previousLists.forEach(([key, data]) => {
        queryClient.setQueryData(key, data);
      });
      if (context?.previousDetail) {
        queryClient.setQueryData(
          queryKeys.applications.detail(variables.id),
          context.previousDetail,
        );
      }
    },
    onSettled: (_data, _error, variables) => {
      invalidateApplicationData(queryClient, variables.id);
    },
  });
}

export function useDeleteApplicationMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteApplication(id),
    onSuccess: () => invalidateApplicationData(queryClient),
  });
}

export function useCreateInteractionMutation(applicationId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: InteractionPayload) => createInteraction(applicationId, payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: queryKeys.applications.interactions(applicationId),
      });
      invalidateApplicationData(queryClient, applicationId);
    },
  });
}

export function useDeleteInteractionMutation(applicationId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (interactionId: string) => deleteInteraction(applicationId, interactionId),
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: queryKeys.applications.interactions(applicationId),
      });
      invalidateApplicationData(queryClient, applicationId);
    },
  });
}

export function useCreateReminderMutation(applicationId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: ReminderPayload) => createReminder(applicationId, payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: queryKeys.applications.reminders(applicationId),
      });
      invalidateApplicationData(queryClient, applicationId);
    },
  });
}

export function useCompleteReminderMutation(applicationId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (reminderId: string) => completeReminder(applicationId, reminderId),
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: queryKeys.applications.reminders(applicationId),
      });
      invalidateApplicationData(queryClient, applicationId);
    },
  });
}

export function useDeleteReminderMutation(applicationId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (reminderId: string) => deleteReminder(applicationId, reminderId),
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: queryKeys.applications.reminders(applicationId),
      });
      invalidateApplicationData(queryClient, applicationId);
    },
  });
}

export function useImportApplicationsMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (file: File) => importApplicationsCsv(file),
    onSuccess: () => invalidateApplicationData(queryClient),
  });
}
