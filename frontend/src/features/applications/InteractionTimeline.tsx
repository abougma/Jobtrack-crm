import { useMemo } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Trash2 } from "lucide-react";
import { errorMessage } from "../../lib/api";
import { useI18n } from "../../lib/i18n";
import { formatDateTime } from "../../lib/format";
import {
  INTERACTION_LABEL_KEYS,
  INTERACTION_TYPES,
  type InteractionType,
} from "../../lib/domain";
import { Button } from "../../components/ui/Button";
import { Badge } from "../../components/ui/Badge";
import { Select } from "../../components/ui/Select";
import { Textarea } from "../../components/ui/Textarea";
import { Input } from "../../components/ui/Input";
import { EmptyState } from "../../components/ui/EmptyState";
import { useToast } from "../../components/ui/toast-context";
import { createInteractionSchema, type InteractionFormValues } from "./schemas";
import { useCreateInteractionMutation, useDeleteInteractionMutation } from "./hooks";
import type { Interaction } from "./types";

interface InteractionTimelineProps {
  applicationId: string;
  interactions: Interaction[];
}

export function InteractionTimeline({ applicationId, interactions }: InteractionTimelineProps) {
  const { t, language } = useI18n();
  const { push } = useToast();
  const schema = useMemo(() => createInteractionSchema(t), [t]);
  const createMutation = useCreateInteractionMutation(applicationId);
  const deleteMutation = useDeleteInteractionMutation(applicationId);

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<InteractionFormValues>({
    resolver: zodResolver(schema),
    defaultValues: { type: "MESSAGE", message: "", occurredAt: "" },
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await createMutation.mutateAsync({
        type: values.type,
        message: values.message.trim(),
        occurredAt: values.occurredAt ? new Date(values.occurredAt).toISOString() : undefined,
      });
      reset({ type: "MESSAGE", message: "", occurredAt: "" });
    } catch (error) {
      setError("root", { message: errorMessage(error, t("errors.saveFailed")) });
    }
  });

  const handleDelete = async (interactionId: string) => {
    try {
      await deleteMutation.mutateAsync(interactionId);
    } catch (error) {
      push(errorMessage(error, t("errors.deleteFailed")), "error");
    }
  };

  const sorted = [...interactions].sort(
    (a, b) => new Date(b.occurredAt).getTime() - new Date(a.occurredAt).getTime(),
  );

  return (
    <div className="space-y-4">
      {sorted.length === 0 ? (
        <EmptyState
          title={t("applications.detail.interactionsEmpty")}
          className="border-0 bg-transparent py-6"
        />
      ) : (
        <ol className="relative space-y-4 border-l border-border pl-5">
          {sorted.map((interaction) => (
            <li key={interaction.id} className="relative">
              <span
                className="absolute -left-[1.4rem] top-1.5 h-2.5 w-2.5 rounded-full border-2 border-surface bg-primary-500"
                aria-hidden="true"
              />
              <div className="flex flex-wrap items-start justify-between gap-2">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <Badge variant="info">{t(INTERACTION_LABEL_KEYS[interaction.type])}</Badge>
                    <span className="text-xs text-muted">
                      {formatDateTime(interaction.occurredAt, language)}
                    </span>
                  </div>
                  <p className="mt-1 whitespace-pre-wrap text-sm text-fg">{interaction.message}</p>
                </div>
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => void handleDelete(interaction.id)}
                  aria-label={t("applications.detail.deleteInteraction")}
                  className="text-muted hover:text-rose-600"
                >
                  <Trash2 className="h-3.5 w-3.5" aria-hidden="true" />
                </Button>
              </div>
            </li>
          ))}
        </ol>
      )}

      <form onSubmit={onSubmit} noValidate className="space-y-3 rounded-xl border border-border bg-elevated/60 p-3">
        <p className="text-xs font-semibold uppercase tracking-wide text-muted">
          {t("applications.detail.addInteraction")}
        </p>
        <div className="grid gap-3 sm:grid-cols-2">
          <Select
            label={t("applications.detail.interactionType")}
            options={INTERACTION_TYPES.map((type) => ({
              value: type,
              label: t(INTERACTION_LABEL_KEYS[type as InteractionType]),
            }))}
            error={errors.type?.message}
            {...register("type")}
          />
          <Input
            label={t("applications.detail.interactionDate")}
            type="datetime-local"
            error={errors.occurredAt?.message}
            {...register("occurredAt")}
          />
        </div>
        <Textarea
          label={t("applications.detail.interactionMessage")}
          placeholder={t("applications.detail.interactionPlaceholder")}
          error={errors.message?.message}
          {...register("message")}
        />
        {errors.root?.message && (
          <p role="alert" className="text-xs text-rose-600 dark:text-rose-400">
            {errors.root.message}
          </p>
        )}
        <div className="flex justify-end">
          <Button type="submit" size="sm" loading={isSubmitting || createMutation.isPending}>
            {t("common.add")}
          </Button>
        </div>
      </form>
    </div>
  );
}
