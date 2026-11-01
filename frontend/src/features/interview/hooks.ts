import { useMutation } from "@tanstack/react-query";
import { prepareInterview } from "./api";
import type { InterviewPayload } from "./types";

export function usePrepareInterviewMutation() {
  return useMutation({
    mutationFn: (payload: InterviewPayload) => prepareInterview(payload),
  });
}
