import { useI18n } from "../../lib/i18n";
import { formatDateTime } from "../../lib/format";
import { STATUS_LABEL_KEYS } from "../../lib/domain";
import { Badge } from "../../components/ui/Badge";
import { EmptyState } from "../../components/ui/EmptyState";
import type { StatusChange } from "./types";

export function HistoryPanel({ history }: { history: StatusChange[] }) {
  const { t, language } = useI18n();

  if (history.length === 0) {
    return (
      <EmptyState
        title={t("applications.detail.historyEmpty")}
        className="border-0 bg-transparent py-6"
      />
    );
  }

  const sorted = [...history].sort(
    (a, b) => new Date(b.changedAt).getTime() - new Date(a.changedAt).getTime(),
  );

  return (
    <ul className="space-y-2">
      {sorted.map((change) => (
        <li key={change.id} className="flex flex-wrap items-center gap-2 text-sm">
          {change.previousStatus ? (
            <Badge variant="neutral">{t(STATUS_LABEL_KEYS[change.previousStatus])}</Badge>
          ) : (
            <span className="text-xs text-muted">—</span>
          )}
          <span className="text-muted" aria-hidden="true">
            →
          </span>
          <Badge variant="info">{t(STATUS_LABEL_KEYS[change.newStatus])}</Badge>
          <span className="text-xs text-muted">{formatDateTime(change.changedAt, language)}</span>
        </li>
      ))}
    </ul>
  );
}
