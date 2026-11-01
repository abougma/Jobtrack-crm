import { z } from "zod";
import {
  APPLICATION_STATUSES,
  CONTRACT_TYPES,
  INTERACTION_TYPES,
  WORK_MODES,
} from "../../lib/domain";
import type { TranslateFn } from "../../lib/i18n";

export function createApplicationSchema(t: TranslateFn) {
  return z
    .object({
      company: z
        .string()
        .trim()
        .min(1, { message: t("validation.required") })
        .max(255, { message: t("validation.required") }),
      position: z
        .string()
        .trim()
        .min(1, { message: t("validation.required") })
        .max(255, { message: t("validation.required") }),
      location: z.string().max(255).optional(),
      workMode: z.union([z.enum(WORK_MODES), z.literal("")]).optional(),
      contractType: z.union([z.enum(CONTRACT_TYPES), z.literal("")]).optional(),
      salaryMin: z.string().regex(/^\d*$/, { message: t("validation.required") }).optional(),
      salaryMax: z.string().regex(/^\d*$/, { message: t("validation.required") }).optional(),
      source: z.string().max(100).optional(),
      offerUrl: z
        .string()
        .trim()
        .refine((value) => !value || /^https?:\/\/.+/i.test(value), {
          message: t("validation.url"),
        })
        .optional(),
      appliedAt: z.string().optional(),
      notes: z.string().max(10000).optional(),
      status: z.enum(APPLICATION_STATUSES).optional(),
    })
    .refine(
      (values) => {
        if (!values.salaryMin || !values.salaryMax) return true;
        return Number(values.salaryMax) >= Number(values.salaryMin);
      },
      { path: ["salaryMax"], message: t("validation.salaryRange") },
    );
}

export type ApplicationFormValues = z.infer<ReturnType<typeof createApplicationSchema>>;

export function createInteractionSchema(t: TranslateFn) {
  return z.object({
    type: z.enum(INTERACTION_TYPES),
    message: z.string().trim().min(1, { message: t("validation.required") }).max(5000),
    occurredAt: z.string().optional(),
  });
}

export type InteractionFormValues = z.infer<ReturnType<typeof createInteractionSchema>>;

export function createReminderSchema(t: TranslateFn) {
  return z.object({
    title: z.string().trim().min(1, { message: t("validation.required") }).max(255),
    dueAt: z.string().min(1, { message: t("validation.required") }),
  });
}

export type ReminderFormValues = z.infer<ReturnType<typeof createReminderSchema>>;
