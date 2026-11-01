import { useCallback, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { KanbanSquare, Plus, Table2, Upload } from "lucide-react";
import { errorMessage } from "../../lib/api";
import { useI18n } from "../../lib/i18n";
import { DEFAULT_FILTERS, filterApplications } from "./filters";
import { useApplicationsQuery, useDeleteApplicationMutation, useUpdateStatusMutation } from "./hooks";
import type { ApplicationFilters, ApplicationSummary } from "./types";
import { Button } from "../../components/ui/Button";
import { Card } from "../../components/ui/Card";
import { Dialog } from "../../components/ui/Dialog";
import { EmptyState } from "../../components/ui/EmptyState";
import { Spinner } from "../../components/ui/Spinner";
import { Tabs } from "../../components/ui/Tabs";
import { useToast } from "../../components/ui/toast-context";
import { ApplicationFiltersBar } from "./ApplicationFiltersBar";
import { ApplicationTable } from "./ApplicationTable";
import { ApplicationFormDialog } from "./ApplicationFormDialog";
import { KanbanBoard } from "./KanbanBoard";
import { CsvExportButton, CsvImportDialog } from "./CsvTools";

type ViewMode = "table" | "kanban";

export function ApplicationsPage() {
  const { t } = useI18n();
  const { push } = useToast();
  const [searchParams, setSearchParams] = useSearchParams();
  const [filters, setFilters] = useState<ApplicationFilters>(DEFAULT_FILTERS);
  const [view, setView] = useState<ViewMode>("table");
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<ApplicationSummary | null>(null);
  const [deleting, setDeleting] = useState<ApplicationSummary | null>(null);
  const [importOpen, setImportOpen] = useState(false);

  useEffect(() => {
    if (searchParams.get("new") === "1") {
      setEditing(null);
      setFormOpen(true);
      const next = new URLSearchParams(searchParams);
      next.delete("new");
      setSearchParams(next, { replace: true });
    }
  }, [searchParams, setSearchParams]);

  const tableQuery = useApplicationsQuery(filters, view === "table");
  const kanbanQuery = useApplicationsQuery({ ...filters, page: 0, size: 200 }, view === "kanban");
  const statusMutation = useUpdateStatusMutation();
  const deleteMutation = useDeleteApplicationMutation();

  const handleFilterChange = useCallback((patch: Partial<ApplicationFilters>) => {
    setFilters((current) => ({ ...current, ...patch }));
  }, []);

  const handleStatusChange = (id: string, status: ApplicationSummary["status"]) => {
    statusMutation.mutate(
      { id, status },
      {
        onSuccess: () => push(t("applications.detail.statusUpdated"), "success"),
        onError: (error) => push(errorMessage(error, t("errors.saveFailed")), "error"),
      },
    );
  };

  const handleDelete = async () => {
    if (!deleting) return;
    try {
      await deleteMutation.mutateAsync(deleting.id);
      push(t("applications.deleted"), "success");
      setDeleting(null);
    } catch (error) {
      push(errorMessage(error, t("errors.deleteFailed")), "error");
    }
  };

  const activeQuery = view === "table" ? tableQuery : kanbanQuery;
  const kanbanItems = filterApplications(kanbanQuery.data?.content ?? [], filters);
  const isFiltered = Boolean(
    filters.search || filters.status || filters.source || filters.contractType || filters.workMode || filters.from || filters.to,
  );

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h2 className="font-display text-xl font-semibold text-fg">{t("applications.title")}</h2>
          <p className="mt-1 text-sm text-muted">{t("applications.subtitle")}</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <CsvExportButton filters={filters} />
          <Button
            variant="outline"
            onClick={() => setImportOpen(true)}
            leftIcon={<Upload className="h-4 w-4" aria-hidden="true" />}
          >
            {t("applications.csv.import")}
          </Button>
          <Button
            onClick={() => {
              setEditing(null);
              setFormOpen(true);
            }}
            leftIcon={<Plus className="h-4 w-4" aria-hidden="true" />}
          >
            {t("applications.new")}
          </Button>
        </div>
      </div>

      <ApplicationFiltersBar
        filters={filters}
        onChange={handleFilterChange}
        onReset={() => setFilters(DEFAULT_FILTERS)}
      />

      <div className="flex flex-wrap items-center justify-between gap-3">
        <Tabs
          ariaLabel={t("applications.title")}
          value={view}
          onChange={(value) => setView(value as ViewMode)}
          items={[
            { value: "table", label: t("applications.tab.table"), icon: <Table2 className="h-4 w-4" /> },
            { value: "kanban", label: t("applications.tab.kanban"), icon: <KanbanSquare className="h-4 w-4" /> },
          ]}
        />
        <p className="text-xs text-muted">
          {t("common.results", { count: activeQuery.data?.totalElements ?? 0 })}
        </p>
      </div>

      {activeQuery.isLoading ? (
        <Spinner fullPage />
      ) : activeQuery.isError ? (
        <Card className="p-6">
          <EmptyState
            title={t("errors.loadFailed")}
            description={errorMessage(activeQuery.error, t("errors.generic"))}
            action={
              <Button variant="outline" onClick={() => void activeQuery.refetch()}>
                {t("common.retry")}
              </Button>
            }
          />
        </Card>
      ) : view === "table" ? (
        tableQuery.data && tableQuery.data.content.length > 0 ? (
          <ApplicationTable
            items={tableQuery.data.content}
            totalElements={tableQuery.data.totalElements}
            totalPages={tableQuery.data.totalPages}
            page={tableQuery.data.number}
            sort={filters.sort}
            onSort={(sort) => handleFilterChange({ sort, page: 0 })}
            onPage={(page) => handleFilterChange({ page })}
            onEdit={(application) => {
              setEditing(application);
              setFormOpen(true);
            }}
            onDelete={setDeleting}
            onStatusChange={handleStatusChange}
            updatingId={statusMutation.isPending ? statusMutation.variables?.id : undefined}
          />
        ) : (
          <Card className="p-6">
            <EmptyState
              title={isFiltered ? t("applications.emptyFiltered.title") : t("applications.empty.title")}
              description={
                isFiltered
                  ? t("applications.emptyFiltered.description")
                  : t("applications.empty.description")
              }
              action={
                isFiltered ? (
                  <Button variant="outline" onClick={() => setFilters(DEFAULT_FILTERS)}>
                    {t("common.reset")}
                  </Button>
                ) : (
                  <Button onClick={() => setFormOpen(true)} leftIcon={<Plus className="h-4 w-4" />}>
                    {t("applications.empty.action")}
                  </Button>
                )
              }
            />
          </Card>
        )
      ) : kanbanItems.length > 0 ? (
        <KanbanBoard
          applications={kanbanItems}
          onStatusChange={handleStatusChange}
          updatingId={statusMutation.isPending ? statusMutation.variables?.id : undefined}
        />
      ) : (
        <Card className="p-6">
          <EmptyState
            title={isFiltered ? t("applications.emptyFiltered.title") : t("applications.empty.title")}
            description={
              isFiltered
                ? t("applications.emptyFiltered.description")
                : t("applications.empty.description")
            }
            action={
              <Button onClick={() => setFormOpen(true)} leftIcon={<Plus className="h-4 w-4" />}>
                {t("applications.empty.action")}
              </Button>
            }
          />
        </Card>
      )}

      <ApplicationFormDialog
        open={formOpen}
        application={editing}
        onClose={() => setFormOpen(false)}
        onSaved={() => void activeQuery.refetch()}
      />
      <CsvImportDialog open={importOpen} onClose={() => setImportOpen(false)} />

      <Dialog
        open={Boolean(deleting)}
        onClose={() => setDeleting(null)}
        title={t("applications.deleteConfirmTitle")}
        description={t("applications.deleteConfirmBody")}
        footer={
          <>
            <Button variant="ghost" onClick={() => setDeleting(null)}>
              {t("common.cancel")}
            </Button>
            <Button
              variant="danger"
              onClick={() => void handleDelete()}
              loading={deleteMutation.isPending}
            >
              {t("applications.confirmDelete")}
            </Button>
          </>
        }
      >
        <p className="text-sm text-muted">
          {deleting?.company} · {deleting?.position}
        </p>
      </Dialog>
    </div>
  );
}
