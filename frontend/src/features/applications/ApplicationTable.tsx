import { Link } from "react-router-dom";
import { ArrowUpDown, ChevronLeft, ChevronRight, Pencil, Trash2 } from "lucide-react";
import { useI18n, type TranslationKey } from "../../lib/i18n";
import { formatDate, formatNumber } from "../../lib/format";
import { Button } from "../../components/ui/Button";
import { Badge } from "../../components/ui/Badge";
import type { ApplicationStatus } from "../../lib/domain";
import { ApplicationStatusSelect } from "./ApplicationStatusSelect";
import type { ApplicationSummary } from "./types";

interface ApplicationTableProps {
  items: ApplicationSummary[];
  totalElements: number;
  totalPages: number;
  page: number;
  sort: string;
  onSort: (sort: string) => void;
  onPage: (page: number) => void;
  onEdit: (application: ApplicationSummary) => void;
  onDelete: (application: ApplicationSummary) => void;
  onStatusChange: (id: string, status: ApplicationStatus) => void;
  updatingId?: string;
}

const SORTABLE: { field: string; labelKey: TranslationKey }[] = [
  { field: "position", labelKey: "applications.column.position" },
  { field: "company", labelKey: "applications.column.company" },
  { field: "status", labelKey: "applications.column.status" },
  { field: "appliedAt", labelKey: "applications.column.appliedAt" },
  { field: "updatedAt", labelKey: "applications.column.updatedAt" },
];

export function ApplicationTable({
  items,
  totalElements,
  totalPages,
  page,
  sort,
  onSort,
  onPage,
  onEdit,
  onDelete,
  onStatusChange,
  updatingId,
}: ApplicationTableProps) {
  const { t, language } = useI18n();
  const [sortField, sortDirection] = sort.split(",");

  const toggleSort = (field: string) => {
    if (sortField === field) {
      onSort(`${field},${sortDirection === "asc" ? "desc" : "asc"}`);
    } else {
      onSort(`${field},asc`);
    }
  };

  return (
    <div>
      <div className="overflow-x-auto rounded-xl border border-border">
        <table className="w-full min-w-[900px] border-collapse text-left text-sm">
          <thead className="bg-elevated">
            <tr>
              {SORTABLE.map(({ field, labelKey }) => {
                const active = sortField === field;
                const ariaSort = active ? (sortDirection === "asc" ? "ascending" : "descending") : "none";
                return (
                  <th
                    key={field}
                    scope="col"
                    aria-sort={ariaSort}
                    className="px-3 py-2.5 text-xs font-semibold uppercase tracking-wide text-muted"
                  >
                    <button
                      type="button"
                      onClick={() => toggleSort(field)}
                      className="inline-flex items-center gap-1 rounded hover:text-fg focus-visible:ring-2 focus-visible:ring-primary-500"
                      aria-label={`${t(labelKey)} — ${
                        active && sortDirection === "asc" ? t("aria.sortDesc") : t("aria.sortAsc")
                      }`}
                    >
                      {t(labelKey)}
                      <ArrowUpDown
                        className={`h-3 w-3 ${active ? "text-primary-600 dark:text-primary-300" : "opacity-40"}`}
                        aria-hidden="true"
                      />
                    </button>
                  </th>
                );
              })}
              <th scope="col" className="px-3 py-2.5 text-xs font-semibold uppercase tracking-wide text-muted">
                {t("applications.column.source")}
              </th>
              <th scope="col" className="px-3 py-2.5 text-xs font-semibold uppercase tracking-wide text-muted">
                {t("applications.column.nextReminder")}
              </th>
              <th scope="col" className="px-3 py-2.5 text-right text-xs font-semibold uppercase tracking-wide text-muted">
                {t("common.actions")}
              </th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className="border-t border-border hover:bg-elevated/60">
                <td className="px-3 py-3">
                  <Link
                    to={`/applications/${item.id}`}
                    className="font-semibold text-fg hover:text-primary-600 hover:underline dark:hover:text-primary-300"
                  >
                    {item.position}
                  </Link>
                  {item.location && <p className="text-xs text-muted">{item.location}</p>}
                </td>
                <td className="px-3 py-3 text-muted">{item.company}</td>
                <td className="px-3 py-3">
                  <ApplicationStatusSelect
                    value={item.status}
                    onChange={(status) => onStatusChange(item.id, status)}
                    disabled={updatingId === item.id}
                  />
                </td>
                <td className="px-3 py-3 text-muted">
                  {formatDate(item.appliedAt, language)}
                </td>
                <td className="px-3 py-3 text-muted">{formatDate(item.updatedAt, language)}</td>
                <td className="px-3 py-3">
                  {item.source ? <Badge variant="neutral">{item.source}</Badge> : <span className="text-muted">—</span>}
                </td>
                <td className="px-3 py-3 text-xs text-muted">
                  {item.nextReminderAt ? (
                    <Badge variant="accent">{formatDate(item.nextReminderAt, language)}</Badge>
                  ) : (
                    "—"
                  )}
                </td>
                <td className="px-3 py-3">
                  <div className="flex items-center justify-end gap-1">
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => onEdit(item)}
                      aria-label={`${t("common.edit")} ${item.position}`}
                    >
                      <Pencil className="h-4 w-4" aria-hidden="true" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => onDelete(item)}
                      aria-label={`${t("common.delete")} ${item.position}`}
                      className="text-rose-600 hover:bg-rose-50 hover:text-rose-700 dark:text-rose-400 dark:hover:bg-rose-950/40"
                    >
                      <Trash2 className="h-4 w-4" aria-hidden="true" />
                    </Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="mt-3 flex flex-wrap items-center justify-between gap-3">
        <p className="text-xs text-muted">
          {t("common.results", { count: formatNumber(totalElements, language) })}
        </p>
        <div className="flex items-center gap-2">
          <span className="text-xs text-muted">
            {t("common.page", { page: page + 1, pages: Math.max(totalPages, 1) })}
          </span>
          <Button
            variant="outline"
            size="icon"
            onClick={() => onPage(page - 1)}
            disabled={page <= 0}
            aria-label={t("applications.pagination.previous")}
          >
            <ChevronLeft className="h-4 w-4" aria-hidden="true" />
          </Button>
          <Button
            variant="outline"
            size="icon"
            onClick={() => onPage(page + 1)}
            disabled={page + 1 >= totalPages}
            aria-label={t("applications.pagination.next")}
          >
            <ChevronRight className="h-4 w-4" aria-hidden="true" />
          </Button>
        </div>
      </div>
    </div>
  );
}
