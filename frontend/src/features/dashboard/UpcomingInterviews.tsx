import { Link } from "react-router-dom";
import { CalendarClock } from "lucide-react";
import { useI18n } from "../../lib/i18n";
import { formatDateTime } from "../../lib/format";
import { EmptyState } from "../../components/ui/EmptyState";
import type { UpcomingInterview } from "./types";

export function UpcomingInterviews({ interviews }: { interviews: UpcomingInterview[] }) {
  const { t, language } = useI18n();

  if (interviews.length === 0) {
    return (
      <EmptyState
        title={t("dashboard.interviews.empty")}
        icon={<CalendarClock className="h-5 w-5" aria-hidden="true" />}
        className="border-0 bg-transparent py-8"
      />
    );
  }

  return (
    <ul className="divide-y divide-border">
      {interviews.map((interview) => (
        <li key={interview.id}>
          <Link
            to={`/applications/${interview.applicationId}`}
            className="flex items-start gap-3 py-3 hover:bg-elevated/60 focus-visible:ring-2 focus-visible:ring-primary-500"
          >
            <span className="mt-0.5 grid h-8 w-8 shrink-0 place-items-center rounded-lg bg-primary-50 text-primary-600 dark:bg-primary-900/40 dark:text-primary-200">
              <CalendarClock className="h-4 w-4" aria-hidden="true" />
            </span>
            <span className="min-w-0 flex-1">
              <span className="block truncate text-sm font-semibold text-fg">{interview.company}</span>
              <span className="block truncate text-xs text-muted">{interview.position}</span>
              <span className="mt-0.5 block text-xs font-medium text-primary-600 dark:text-primary-300">
                {t("dashboard.interviews.on", {
                  date: formatDateTime(interview.occurredAt, language),
                })}
              </span>
            </span>
          </Link>
        </li>
      ))}
    </ul>
  );
}
