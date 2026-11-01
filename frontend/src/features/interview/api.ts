import { apiRequest } from "../../lib/api";
import type { InterviewPayload, InterviewPreparation } from "./types";

export function prepareInterview(payload: InterviewPayload): Promise<InterviewPreparation> {
  return apiRequest<InterviewPreparation>("/interview/prepare", { method: "POST", body: payload });
}
