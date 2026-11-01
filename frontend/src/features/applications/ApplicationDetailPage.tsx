import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, ExternalLink, Pencil, Trash2 } from "lucide-react";
import { errorMessage } from "../../lib/api";
import { useI18n } from "../../lib/i18n";
import { formatDate, formatDateTime, formatSalary } from "../../lib/format";
import {
  CONTRACT_LABEL_KEYS,
  STATUS_LABEL_KEYS,
  WORK_MODE_LABEL_KEYS,
  type ApplicationStatus,
} from "../../lib/domain";
import { Button } from "../../components/ui/Button";
import { Badge } from "../../components/ui/Badge";
import { Card, CardHeader } from "../../components/ui/Card";
import { Dialog } from "../../components/ui/Dialog";
import { EmptyState } from "../../components/ui/EmptyState";
import { Spinner } from "../../components/ui/Spinner";
import { useToast } from "../../components/ui/toast-context";
import { STATUS_BADGE_VARIANTS } from "../../lib/domain";
import { useApplicationQuery, useDeleteApplicationMutation, useUpdateStatusMutation } from "./hooks";
import { ApplicationStatusSelect } from "./ApplicationStatusSelect";
import { ApplicationFormDialog } from "./ApplicationFormDialog";
import { InteractionTimeline } from "./InteractionTimeline";
import { RemindersPanel } from "./RemindersPanel";
import { HistoryPanel } from "./HistoryPanel";

export function ApplicationDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { t, language } = useI18n();
  const { push } = useToast();
  const navigate = useNavigate();
  const query = useApplicationQuery(id);
  const statusMutation = useUpdateStatusMutation();
  const deleteMutation = useDeleteApplicationMutation();
  const [editOpen, setEditOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);

  if (query.isLoading) return <Spinner fullPage />;

  if (query.isError || !query.data) {
    return (
      <Card className="p-6">
        <EmptyState
          title={t("applications.detail.notFound")}
          description={errorMessage(query.error, t("errors.notFound"))}
          action={
            <Link to="/applications">
              <Button variant="outline" leftIcon={<ArrowLeft className="h-4 w-4" />}>
                {t("common.back")}
              </Button>
            </Link>
          }
        />
      </Card>
    );
  }

  const application = query.data;

  const handleStatus = (status: ApplicationStatus) => {
    statusMutation.mutate(
      { id: application.id, status },
      {
        onSuccess: () => push(t("applications.detail.statusUpdated"), "success"),
        onError: (error) => push(errorMessage(error, t("errors.saveFailed")), "error"),
      },
    );
  };

  const handleDelete = async () => {
    try {
      await deleteMutation.mutateAsync(application.id);
      push(t("applications.deleted"), "success");
      navigate("/applications");
    } catch (error) {
      push(errorMessage(error, t("errors.deleteFailed")), "error");
    }
  };

  const meta = [
    { label: t("applications.column.location"), value: application.location ?? t("common.notProvided") },
    {
      label: t("applications.workMode"),
      value: application.workMode ? t(WORK_MODE_LABEL_KEYS[application.workMode]) : t("common.notProvided"),
    },
    {
      label: t("applications.contract"),
      value: application.contractType
        ? t(CONTRACT_LABEL_KEYS[application.contractType])
        : t("common.notProvided"),
    },
    {
      label: t("applications.detail.salary"),
      value: formatSalary(application.salaryMin, application.salaryMax, language),
    },
    { label: t("applications.source"), value: application.source ?? t("common.notProvided") },
    { label: t("applications.detail.contact"), value: application.contactName ?? t("common.notProvided") },
  ];

  return (
    <div className="space-y-4">
      <Link
        to="/applications"
        className="inline-flex items-center gap-1.5 text-sm font-medium text-muted hover:text-fg"
      >
        <ArrowLeft className="h-4 w-4" aria-hidden="true" />
        {t("common.back")}
      </Link>

      <Card className="p-5">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="flex min-w-0 items-start gap-3">
            <span className="grid h-12 w-12 shrink-0 place-items-center rounded-xl bg-primary-50 font-display text-lg font-bold text-primary-700 dark:bg-primary-900/40 dark:text-primary-100">
              {application.company.slice(0, 1).toUpperCase()}
            </span>
            <div className="min-w-0">
              <h2 className="truncate font-display text-xl font-semibold text-fg">
                {application.position}
              </h2>
              <p className="truncate text-sm text-muted">{application.company}</p>
              <div className="mt-2 flex flex-wrap items-center gap-2">
                <Badge variant={STATUS_BADGE_VARIANTS[application.status]}>
                  {t(STATUS_LABEL_KEYS[application.status])}
                </Badge>
                {application.appliedAt && (
                  <span className="text-xs text-muted">
                    {t("applications.detail.appliedOn", {
                      date: formatDate(application.appliedAt, language),
                    })}
                  </span>
                )}
                {application.generatedCvId && (
                  <Link
                    to={`/cv/${application.generatedCvId}`}
                    className="text-xs font-semibold text-primary-600 hover:underline dark:text-primary-300"
                  >
                    {t("applications.detail.cv")}
                  </Link>
                )}
              </div>
            </div>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <ApplicationStatusSelect
              value={application.status}
              onChange={handleStatus}
              disabled={statusMutation.isPending}
              label={t("applications.form.status")}
              className="h-9"
            />
            <Button
              variant="outline"
              onClick={() => setEditOpen(true)}
              leftIcon={<Pencil className="h-4 w-4" aria-hidden="true" />}
            >
              {t("common.edit")}
            </Button>
            <Button
              variant="ghost"
              onClick={() => setDeleteOpen(true)}
              className="text-rose-600 hover:bg-rose-50 hover:text-rose-700 dark:text-rose-400 dark:hover:bg-rose-950/40"
              leftIcon={<Trash2 className="h-4 w-4" aria-hidden="true" />}
            >
              {t("common.delete")}
            </Button>
          </div>
        </div>

        <dl className="mt-5 grid grid-cols-2 gap-3 border-t border-border pt-4 sm:grid-cols-3 lg:grid-cols-6">
          {meta.map((item) => (
            <div key={item.label}>
              <dt className="text-[0.65rem] font-bold uppercase tracking-wide text-muted">
                {item.label}
              </dt>
              <dd className="mt-0.5 truncate text-sm font-medium text-fg">{item.value}</dd>
            </div>
          ))}
        </dl>

        <div className="mt-3 flex flex-wrap items-center gap-4 text-xs text-muted">
          {application.offerUrl && (
            <a
              href={application.offerUrl}
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-1 font-semibold text-primary-600 hover:underline dark:text-primary-300"
            >
              <ExternalLink className="h-3.5 w-3.5" aria-hidden="true" />
              {t("applications.detail.offerLink")}
            </a>
          )}
          <span>{t("applications.detail.createdAt", { date: formatDateTime(application.createdAt, language) })}</span>
          <span>{t("applications.detail.updatedAt", { date: formatDateTime(application.updatedAt, language) })}</span>
        </div>
      </Card>

      <Card className="p-5">
        <CardHeader title={t("applications.detail.notes")} />
        <p className="mt-2 whitespace-pre-wrap text-sm text-fg">
          {application.notes?.trim() ? application.notes : t("applications.detail.noNotes")}
        </p>
      </Card>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card className="p-5">
          <CardHeader title={t("applications.detail.interactions")} />
          <div className="mt-3">
            <InteractionTimeline applicationId={application.id} interactions={application.interactions} />
          </div>
        </Card>
        <Card className="p-5">
          <CardHeader title={t("applications.detail.reminders")} />
          <div className="mt-3">
            <RemindersPanel applicationId={application.id} reminders={application.reminders} />
          </div>
        </Card>
      </div>

      <Card className="p-5">
        <CardHeader title={t("applications.detail.history")} />
        <div className="mt-3">
          <HistoryPanel history={application.history} />
        </div>
      </Card>

      <ApplicationFormDialog
        open={editOpen}
        application={application}
        onClose={() => setEditOpen(false)}
        onSaved={() => void query.refetch()}
      />

      <Dialog
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
        title={t("applications.deleteConfirmTitle")}
        description={t("applications.deleteConfirmBody")}
        footer={
          <>
            <Button variant="ghost" onClick={() => setDeleteOpen(false)}>
              {t("common.cancel")}
            </Button>
            <Button variant="danger" onClick={() => void handleDelete()} loading={deleteMutation.isPending}>
              {t("applications.confirmDelete")}
            </Button>
          </>
        }
      >
        <p className="text-sm text-muted">
          {application.company} · {application.position}
        </p>
      </Dialog>
    </div>
  );
}
