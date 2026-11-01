import { useMemo } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Check, Trash2 } from "lucide-react";
import { errorMessage } from "../../lib/api";
import { useI18n } from "../../lib/i18n";
import { formatDateTime, isPast } from "../../lib/format";
import { cn } from "../../lib/utils";
import { Button } from "../../components/ui/Button";
import { Badge } from "../../components/ui/Badge";
import { Input } from "../../components/ui/Input";
import { EmptyState } from "../../components/ui/EmptyState";
import { useToast } from "../../components/ui/toast-context";
import { createReminderSchema, type ReminderFormValues } from "./schemas";
import {
  useCompleteReminderMutation,
  useCreateReminderMutation,
  useDeleteReminderMutation,
} from "./hooks";
import type { Reminder } from "./types";

export function RemindersPanel({
  applicationId,
  reminders,
}: {
  applicationId: string;
  reminders: Reminder[];
}) {
  const { t, language } = useI18n();
  const { push } = useToast();
  const schema = useMemo(() => createReminderSchema(t), [t]);
  const createMutation = useCreateReminderMutation(applicationId);
  const completeMutation = useCompleteReminderMutation(applicationId);
  const deleteMutation = useDeleteReminderMutation(applicationId);

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<ReminderFormValues>({
    resolver: zodResolver(schema),
    defaultValues: { title: "", dueAt: "" },
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await createMutation.mutateAsync({
        title: values.title.trim(),
        dueAt: new Date(values.dueAt).toISOString(),
      });
      reset({ title: "", dueAt: "" });
    } catch (error) {
      setError("root", { message: errorMessage(error, t("errors.saveFailed")) });
    }
  });

  const handleComplete = async (reminderId: string) => {
    try {
      await completeMutation.mutateAsync(reminderId);
    } catch (error) {
      push(errorMessage(error, t("errors.saveFailed")), "error");
    }
  };

  const handleDelete = async (reminderId: string) => {
    try {
      await deleteMutation.mutateAsync(reminderId);
    } catch (error) {
      push(errorMessage(error, t("errors.deleteFailed")), "error");
    }
  };

  const sorted = [...reminders].sort(
    (a, b) => new Date(a.dueAt).getTime() - new Date(b.dueAt).getTime(),
  );

  return (
    <div className="space-y-4">
      {sorted.length === 0 ? (
        <EmptyState
          title={t("applications.detail.remindersEmpty")}
          className="border-0 bg-transparent py-6"
        />
      ) : (
        <ul className="space-y-2">
          {sorted.map((reminder) => (
            <li
              key={reminder.id}
              className={cn(
                "flex items-start gap-2 rounded-lg border border-border px-3 py-2",
                reminder.completed && "opacity-60",
              )}
            >
              <span
                className={cn(
                  "mt-1.5 h-2 w-2 shrink-0 rounded-full",
                  reminder.completed
                    ? "bg-emerald-500"
                    : isPast(reminder.dueAt)
                      ? "bg-rose-500"
                      : "bg-accent-400",
                )}
                aria-hidden="true"
              />
              <div className="min-w-0 flex-1">
                <p
                  className={cn(
                    "text-sm font-semibold text-fg",
                    reminder.completed && "line-through",
                  )}
                >
                  {reminder.title}
                </p>
                <p className="text-xs text-muted">{formatDateTime(reminder.dueAt, language)}</p>
              </div>
              {reminder.completed ? (
                <Badge variant="success">{t("applications.detail.reminderCompleted")}</Badge>
              ) : (
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => void handleComplete(reminder.id)}
                  aria-label={t("applications.detail.markComplete")}
                  className="text-emerald-600 hover:bg-emerald-50 dark:text-emerald-400 dark:hover:bg-emerald-950/40"
                >
                  <Check className="h-4 w-4" aria-hidden="true" />
                </Button>
              )}
              <Button
                variant="ghost"
                size="icon"
                onClick={() => void handleDelete(reminder.id)}
                aria-label={t("applications.detail.deleteReminder")}
                className="text-muted hover:text-rose-600"
              >
                <Trash2 className="h-3.5 w-3.5" aria-hidden="true" />
              </Button>
            </li>
          ))}
        </ul>
      )}

      <form onSubmit={onSubmit} noValidate className="space-y-3 rounded-xl border border-border bg-elevated/60 p-3">
        <p className="text-xs font-semibold uppercase tracking-wide text-muted">
          {t("applications.detail.addReminder")}
        </p>
        <Input
          label={t("applications.detail.reminderTitle")}
          placeholder={t("applications.detail.reminderTitle")}
          error={errors.title?.message}
          {...register("title")}
        />
        <Input
          label={t("applications.detail.reminderDate")}
          type="datetime-local"
          error={errors.dueAt?.message}
          {...register("dueAt")}
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
