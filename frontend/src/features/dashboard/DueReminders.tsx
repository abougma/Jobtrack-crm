import { Link } from "react-router-dom";
import { BellRing } from "lucide-react";
import { useI18n } from "../../lib/i18n";
import { formatDate, isPast } from "../../lib/format";
import { Badge } from "../../components/ui/Badge";
import { EmptyState } from "../../components/ui/EmptyState";
import type { DashboardReminder } from "./types";

export function DueReminders({ reminders }: { reminders: DashboardReminder[] }) {
  const { t, language } = useI18n();

  if (reminders.length === 0) {
    return (
      <EmptyState
        title={t("dashboard.reminders.empty")}
        icon={<BellRing className="h-5 w-5" aria-hidden="true" />}
        className="border-0 bg-transparent py-8"
      />
    );
  }

  return (
    <ul className="divide-y divide-border">
      {reminders.map((reminder) => {
        const content = (
          <>
            <span
              className={`mt-1.5 h-2 w-2 shrink-0 rounded-full ${
                isPast(reminder.dueAt) ? "bg-rose-500" : "bg-accent-400"
              }`}
              aria-hidden="true"
            />
            <span className="min-w-0 flex-1">
              <span className="block truncate text-sm font-semibold text-fg">{reminder.title}</span>
              <span className="block truncate text-xs text-muted">
                {[reminder.applicationCompany, reminder.applicationPosition]
                  .filter(Boolean)
                  .join(" · ") || t("applications.detail.title")}
              </span>
              <span className="mt-0.5 block text-xs text-muted">
                {t("dashboard.reminders.due", { date: formatDate(reminder.dueAt, language) })}
              </span>
            </span>
            {isPast(reminder.dueAt) && <Badge variant="danger">{t("common.today")}</Badge>}
          </>
        );

        return (
          <li key={reminder.id}>
            {reminder.applicationId ? (
              <Link
                to={`/applications/${reminder.applicationId}`}
                className="flex items-start gap-3 py-3 hover:bg-elevated/60 focus-visible:ring-2 focus-visible:ring-primary-500"
              >
                {content}
              </Link>
            ) : (
              <div className="flex items-start gap-3 py-3">{content}</div>
            )}
          </li>
        );
      })}
    </ul>
  );
}
