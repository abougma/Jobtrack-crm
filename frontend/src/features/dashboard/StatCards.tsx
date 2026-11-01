import {
  ArrowUpRight,
  BriefcaseBusiness,
  CalendarCheck2,
  CheckCircle2,
  MessageSquareReply,
  Send,
  XCircle,
} from "lucide-react";
import { useI18n } from "../../lib/i18n";
import { formatNumber, formatPercent } from "../../lib/format";
import { Card } from "../../components/ui/Card";
import type { DashboardData } from "./types";

interface StatCardsProps {
  data: DashboardData;
}

export function StatCards({ data }: StatCardsProps) {
  const { t, language } = useI18n();

  const cards = [
    {
      label: t("dashboard.stats.total"),
      value: formatNumber(data.totalApplications, language),
      hint: t("dashboard.stats.inTracking"),
      icon: BriefcaseBusiness,
    },
    {
      label: t("dashboard.stats.applied"),
      value: formatNumber(data.applied, language),
      hint: t("dashboard.stats.toDate"),
      icon: Send,
    },
    {
      label: t("dashboard.stats.responses"),
      value: formatNumber(data.responses, language),
      hint: formatPercent(data.responseRate, language),
      icon: MessageSquareReply,
    },
    {
      label: t("dashboard.stats.interviews"),
      value: formatNumber(data.interviews, language),
      hint: formatPercent(data.interviewRate, language),
      icon: CalendarCheck2,
    },
    {
      label: t("dashboard.stats.offers"),
      value: formatNumber(data.offers, language),
      hint: formatPercent(data.offerRate, language),
      icon: CheckCircle2,
    },
    {
      label: t("dashboard.stats.rejected"),
      value: formatNumber(data.rejected, language),
      hint: t("dashboard.stats.noResponse") + " · " + formatNumber(data.noResponse, language),
      icon: XCircle,
    },
  ];

  return (
    <section className="grid grid-cols-2 gap-3 sm:grid-cols-3 xl:grid-cols-6" aria-label={t("dashboard.title")}>
      {cards.map(({ label, value, hint, icon: Icon }) => (
        <Card key={label} className="p-4">
          <div className="flex items-center justify-between gap-2">
            <span className="text-xs font-medium text-muted">{label}</span>
            <Icon className="h-4 w-4 text-primary-500 dark:text-primary-300" aria-hidden="true" />
          </div>
          <p className="mt-2 font-display text-2xl font-semibold text-fg">{value}</p>
          <p className="mt-0.5 flex items-center gap-1 truncate text-xs text-muted">
            {hint}
            <ArrowUpRight className="h-3 w-3 shrink-0" aria-hidden="true" />
          </p>
        </Card>
      ))}
    </section>
  );
}
