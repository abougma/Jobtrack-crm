import { useMemo } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useI18n } from "../../lib/i18n";
import { errorMessage } from "../../lib/api";
import {
  APPLICATION_STATUSES,
  CONTRACT_LABEL_KEYS,
  CONTRACT_TYPES,
  STATUS_LABEL_KEYS,
  WORK_MODE_LABEL_KEYS,
  WORK_MODES,
  type ApplicationStatus,
  type ContractType,
  type WorkMode,
} from "../../lib/domain";
import { Button } from "../../components/ui/Button";
import { Dialog } from "../../components/ui/Dialog";
import { Input } from "../../components/ui/Input";
import { Select } from "../../components/ui/Select";
import { Textarea } from "../../components/ui/Textarea";
import { useToast } from "../../components/ui/toast-context";
import { Spinner } from "../../components/ui/Spinner";
import { createApplicationSchema, type ApplicationFormValues } from "./schemas";
import { useApplicationQuery, useCreateApplicationMutation, useUpdateApplicationMutation } from "./hooks";
import type { ApplicationPayload, ApplicationSummary } from "./types";

interface ApplicationFormDialogProps {
  open: boolean;
  onClose: () => void;
  application?: ApplicationSummary | null;
  onSaved: () => void;
}

function toFormValues(application?: EditableApplication | null): ApplicationFormValues {
  return {
    company: application?.company ?? "",
    position: application?.position ?? "",
    location: application?.location ?? "",
    workMode: (application?.workMode ?? "") as WorkMode | "",
    contractType: (application?.contractType ?? "") as ContractType | "",
    salaryMin: application?.salaryMin?.toString() ?? "",
    salaryMax: application?.salaryMax?.toString() ?? "",
    source: application?.source ?? "",
    offerUrl: application?.offerUrl ?? "",
    appliedAt: application?.appliedAt ?? "",
    notes: application?.notes ?? "",
    status: (application?.status ?? "TO_APPLY") as ApplicationStatus,
  };
}

type EditableApplication = ApplicationSummary & { notes?: string | null };

function toPayload(values: ApplicationFormValues): ApplicationPayload {
  return {
    company: values.company.trim(),
    position: values.position.trim(),
    location: values.location?.trim() || null,
    workMode: values.workMode || null,
    contractType: values.contractType || null,
    salaryMin: values.salaryMin ? Number(values.salaryMin) : null,
    salaryMax: values.salaryMax ? Number(values.salaryMax) : null,
    source: values.source?.trim() || null,
    offerUrl: values.offerUrl?.trim() || null,
    appliedAt: values.appliedAt || null,
    notes: values.notes?.trim() || null,
    status: values.status ?? "TO_APPLY",
  };
}

export function ApplicationFormDialog({ open, onClose, application, onSaved }: ApplicationFormDialogProps) {
  const { t } = useI18n();
  const detailQuery = useApplicationQuery(open ? application?.id : undefined);
  const editable: EditableApplication | null = detailQuery.data ?? application ?? null;

  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={application ? t("applications.form.editTitle") : t("applications.form.createTitle")}
      size="lg"
    >
      {application && detailQuery.isLoading ? (
        <Spinner />
      ) : (
        <ApplicationForm
          key={editable?.id ?? "new"}
          application={editable}
          onClose={onClose}
          onSaved={onSaved}
        />
      )}
    </Dialog>
  );
}

function ApplicationForm({
  application,
  onClose,
  onSaved,
}: {
  application?: EditableApplication | null;
  onClose: () => void;
  onSaved: () => void;
}) {
  const { t } = useI18n();
  const { push } = useToast();
  const createMutation = useCreateApplicationMutation();
  const updateMutation = useUpdateApplicationMutation(application?.id ?? "");
  const schema = useMemo(() => createApplicationSchema(t), [t]);

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<ApplicationFormValues>({
    resolver: zodResolver(schema),
    defaultValues: toFormValues(application),
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      if (application) {
        await updateMutation.mutateAsync(toPayload(values));
        push(t("applications.updated"), "success");
      } else {
        await createMutation.mutateAsync(toPayload(values));
        push(t("applications.created"), "success");
      }
      onSaved();
      onClose();
    } catch (error) {
      setError("root", { message: errorMessage(error, t("errors.saveFailed")) });
    }
  });

  return (
    <form onSubmit={onSubmit} noValidate className="space-y-4">
      <div className="grid gap-4 sm:grid-cols-2">
        <Input
          label={t("applications.form.company")}
          required
          placeholder={t("applications.form.companyPlaceholder")}
          error={errors.company?.message}
          {...register("company")}
        />
        <Input
          label={t("applications.form.position")}
          required
          placeholder={t("applications.form.positionPlaceholder")}
          error={errors.position?.message}
          {...register("position")}
        />
        <Input
          label={t("applications.form.location")}
          placeholder={t("applications.form.locationPlaceholder")}
          error={errors.location?.message}
          {...register("location")}
        />
        <Select
          label={t("applications.form.status")}
          options={APPLICATION_STATUSES.map((status) => ({
            value: status,
            label: t(STATUS_LABEL_KEYS[status]),
          }))}
          error={errors.status?.message}
          {...register("status")}
        />
        <Select
          label={t("applications.form.workMode")}
          placeholder={t("common.none")}
          options={WORK_MODES.map((mode) => ({ value: mode, label: t(WORK_MODE_LABEL_KEYS[mode]) }))}
          error={errors.workMode?.message}
          {...register("workMode")}
        />
        <Select
          label={t("applications.form.contractType")}
          placeholder={t("common.none")}
          options={CONTRACT_TYPES.map((contract) => ({
            value: contract,
            label: t(CONTRACT_LABEL_KEYS[contract]),
          }))}
          error={errors.contractType?.message}
          {...register("contractType")}
        />
        <Input
          label={t("applications.form.salaryMin")}
          inputMode="numeric"
          placeholder="45000"
          hint={t("applications.form.salaryHint")}
          error={errors.salaryMin?.message}
          {...register("salaryMin")}
        />
        <Input
          label={t("applications.form.salaryMax")}
          inputMode="numeric"
          placeholder="55000"
          error={errors.salaryMax?.message}
          {...register("salaryMax")}
        />
        <Input
          label={t("applications.form.source")}
          placeholder={t("applications.form.sourcePlaceholder")}
          error={errors.source?.message}
          {...register("source")}
        />
        <Input
          label={t("applications.form.appliedAt")}
          type="date"
          error={errors.appliedAt?.message}
          {...register("appliedAt")}
        />
        <Input
          label={t("applications.form.offerUrl")}
          type="url"
          placeholder={t("applications.form.offerUrlPlaceholder")}
          error={errors.offerUrl?.message}
          containerClassName="sm:col-span-2"
          {...register("offerUrl")}
        />
        <Textarea
          label={t("applications.form.notes")}
          placeholder={t("applications.form.notesPlaceholder")}
          containerClassName="sm:col-span-2"
          error={errors.notes?.message}
          {...register("notes")}
        />
      </div>

      {errors.root?.message && (
        <p role="alert" className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700 dark:bg-rose-950/40 dark:text-rose-200">
          {errors.root.message}
        </p>
      )}

      <div className="flex flex-wrap justify-end gap-2 border-t border-border pt-4">
        <Button variant="ghost" onClick={onClose}>
          {t("common.cancel")}
        </Button>
        <Button type="submit" loading={isSubmitting || createMutation.isPending || updateMutation.isPending}>
          {application ? t("applications.form.submitUpdate") : t("applications.form.submitCreate")}
        </Button>
      </div>
    </form>
  );
}
