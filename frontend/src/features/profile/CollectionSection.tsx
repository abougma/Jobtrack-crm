import { useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Trash2, Pencil } from "lucide-react";
import { z } from "zod";
import { errorMessage } from "../../lib/api";
import { useI18n } from "../../lib/i18n";
import { formatDate } from "../../lib/format";
import { Button } from "../../components/ui/Button";
import { Dialog } from "../../components/ui/Dialog";
import { EmptyState } from "../../components/ui/EmptyState";
import { Input } from "../../components/ui/Input";
import { Select } from "../../components/ui/Select";
import { Textarea } from "../../components/ui/Textarea";
import { useToast } from "../../components/ui/toast-context";
import { cn } from "../../lib/utils";
import type { ProfileField, ProfileSectionConfig } from "./profileFields";
import {
  useCreateProfileItemMutation,
  useDeleteProfileItemMutation,
  useUpdateProfileItemMutation,
} from "./hooks";
import type { ProfileItem } from "./types";

function buildSchema(fields: ProfileField[], t: ReturnType<typeof useI18n>["t"]) {
  const shape: Record<string, z.ZodString> = {};
  fields.forEach((field) => {
    shape[field.name] = field.required
      ? z.string().min(1, { message: t("validation.required") })
      : z.string();
  });
  return z
    .object(shape)
    .refine(
      (values) => {
        if (!values.startedOn || !values.endedOn) return true;
        return values.endedOn >= values.startedOn;
      },
      { path: ["endedOn"], message: t("validation.dateRange") },
    );
}

type FormValues = Record<string, string>;

interface CollectionSectionProps {
  config: ProfileSectionConfig;
  items: ProfileItem[];
}

export function CollectionSection({ config, items }: CollectionSectionProps) {
  const { t, language } = useI18n();
  const { push } = useToast();
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<ProfileItem | null>(null);
  const [deleting, setDeleting] = useState<ProfileItem | null>(null);

  const createMutation = useCreateProfileItemMutation(config.kind);
  const updateMutation = useUpdateProfileItemMutation(config.kind);
  const deleteMutation = useDeleteProfileItemMutation(config.kind);

  const openCreate = () => {
    setEditing(null);
    setOpen(true);
  };

  const openEdit = (item: ProfileItem) => {
    setEditing(item);
    setOpen(true);
  };

  const handleDelete = async () => {
    if (!deleting) return;
    try {
      await deleteMutation.mutateAsync(deleting.id);
      push(t("profile.deleted"), "success");
      setDeleting(null);
    } catch (error) {
      push(errorMessage(error, t("errors.deleteFailed")), "error");
    }
  };

  const descriptionField = config.fields.find((field) => field.type === "textarea");

  return (
    <section className="card-base p-5">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div>
          <h2 className="text-base font-semibold text-fg">{t(config.titleKey)}</h2>
          <p className="mt-0.5 text-xs text-muted">{t("profile.skillCount", { count: items.length })}</p>
        </div>
        <Button variant="secondary" size="sm" onClick={openCreate} leftIcon={<Plus className="h-4 w-4" />}>
          {t("profile.add")}
        </Button>
      </div>

      <div className="mt-4 space-y-2">
        {items.length === 0 ? (
          <EmptyState title={t(config.emptyKey)} className="border-0 bg-transparent py-6" />
        ) : (
          items.map((item) => {
            const record = item as unknown as Record<string, string | null>;
            const primary = record[config.fields[0].name] ?? "";
            const secondary = config.fields
              .slice(1)
              .filter((field) => field.type !== "textarea")
              .map((field) => {
                const value = record[field.name];
                if (!value) return null;
                if (field.type === "date") return formatDate(value, language, "MMM yyyy");
                if (field.type === "select") {
                  const option = field.options?.find((candidate) => candidate.value === value);
                  return option ? t(option.labelKey) : value;
                }
                return value;
              })
              .filter(Boolean)
              .join(" · ");

            return (
              <article
                key={item.id}
                className="flex items-start justify-between gap-3 rounded-lg border border-border px-3 py-2.5"
              >
                <div className="min-w-0">
                  <p className="truncate text-sm font-semibold text-fg">{primary}</p>
                  {secondary && <p className="truncate text-xs text-muted">{secondary}</p>}
                  {descriptionField && record[descriptionField.name] && (
                    <p className="mt-1 line-clamp-2 text-xs text-muted">
                      {record[descriptionField.name]}
                    </p>
                  )}
                </div>
                <div className="flex shrink-0 gap-1">
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => openEdit(item)}
                    aria-label={`${t("common.edit")} ${primary}`}
                  >
                    <Pencil className="h-3.5 w-3.5" aria-hidden="true" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => setDeleting(item)}
                    aria-label={`${t("common.delete")} ${primary}`}
                    className="text-muted hover:text-rose-600"
                  >
                    <Trash2 className="h-3.5 w-3.5" aria-hidden="true" />
                  </Button>
                </div>
              </article>
            );
          })
        )}
      </div>

      <Dialog
        open={open}
        onClose={() => setOpen(false)}
        title={t(editing ? "profile.form.editTitle" : "profile.form.addTitle", {
          section: t(config.titleKey),
        })}
      >
        <ProfileItemForm
          key={editing?.id ?? "new"}
          config={config}
          item={editing}
          onClose={() => setOpen(false)}
          onSaved={() => setOpen(false)}
          createMutation={createMutation}
          updateMutation={updateMutation}
        />
      </Dialog>

      <Dialog
        open={Boolean(deleting)}
        onClose={() => setDeleting(null)}
        title={t("common.delete")}
        description={t("profile.deleteConfirm")}
        footer={
          <>
            <Button variant="ghost" onClick={() => setDeleting(null)}>
              {t("common.cancel")}
            </Button>
            <Button variant="danger" onClick={() => void handleDelete()} loading={deleteMutation.isPending}>
              {t("common.delete")}
            </Button>
          </>
        }
      >
        <p className="text-sm text-muted">
          {deleting ? String((deleting as unknown as Record<string, unknown>)[config.fields[0].name]) : ""}
        </p>
      </Dialog>
    </section>
  );
}

function ProfileItemForm({
  config,
  item,
  onClose,
  onSaved,
  createMutation,
  updateMutation,
}: {
  config: ProfileSectionConfig;
  item: ProfileItem | null;
  onClose: () => void;
  onSaved: () => void;
  createMutation: ReturnType<typeof useCreateProfileItemMutation>;
  updateMutation: ReturnType<typeof useUpdateProfileItemMutation>;
}) {
  const { t } = useI18n();
  const { push } = useToast();
  const schema = useMemo(() => buildSchema(config.fields, t), [config.fields, t]);

  const defaults = useMemo<FormValues>(() => {
    const record = (item ?? {}) as unknown as Record<string, unknown>;
    return Object.fromEntries(
      config.fields.map((field) => [field.name, record[field.name] != null ? String(record[field.name]) : ""]),
    );
  }, [config.fields, item]);

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: defaults });

  const onSubmit = handleSubmit(async (values) => {
    const payload: Record<string, unknown> = {};
    config.fields.forEach((field) => {
      const raw = values[field.name]?.trim() ?? "";
      payload[field.name] = raw === "" ? (field.type === "date" || !field.required ? null : "") : raw;
    });

    try {
      if (item) {
        await updateMutation.mutateAsync({ id: item.id, body: payload });
        push(t("common.updated"), "success");
      } else {
        await createMutation.mutateAsync(payload);
        push(t("common.created"), "success");
      }
      onSaved();
      onClose();
    } catch (error) {
      setError("root", { message: errorMessage(error, t("errors.saveFailed")) });
    }
  });

  return (
    <form onSubmit={onSubmit} noValidate className="space-y-3">
      <div className="grid gap-3 sm:grid-cols-2">
        {config.fields.map((field) => {
          const label = t(field.labelKey);
          const error = errors[field.name]?.message;
          if (field.type === "textarea") {
            return (
              <Textarea
                key={field.name}
                label={label}
                required={field.required}
                error={error}
                containerClassName={cn(field.colSpan === 2 && "sm:col-span-2")}
                {...register(field.name)}
              />
            );
          }
          if (field.type === "select") {
            return (
              <Select
                key={field.name}
                label={label}
                required={field.required}
                placeholder={t("common.select")}
                error={error}
                options={(field.options ?? []).map((option) => ({
                  value: option.value,
                  label: t(option.labelKey),
                }))}
                {...register(field.name)}
              />
            );
          }
          return (
            <Input
              key={field.name}
              label={label}
              required={field.required}
              type={field.type === "date" ? "date" : "text"}
              error={error}
              {...register(field.name)}
            />
          );
        })}
      </div>
      {errors.root?.message && (
        <p role="alert" className="text-xs text-rose-600 dark:text-rose-400">
          {errors.root.message}
        </p>
      )}
      <div className="flex justify-end gap-2 border-t border-border pt-3">
        <Button variant="ghost" onClick={onClose}>
          {t("common.cancel")}
        </Button>
        <Button type="submit" loading={isSubmitting || createMutation.isPending || updateMutation.isPending}>
          {t("common.save")}
        </Button>
      </div>
    </form>
  );
}
