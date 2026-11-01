import { useNavigate } from "react-router-dom";
import { ArrowRight, Sparkles } from "lucide-react";
import { useI18n } from "../../lib/i18n";
import { errorMessage } from "../../lib/api";
import { formatNumber } from "../../lib/format";
import { Button } from "../../components/ui/Button";
import { Card, CardHeader } from "../../components/ui/Card";
import { EmptyState } from "../../components/ui/EmptyState";
import { Spinner } from "../../components/ui/Spinner";
import { StatusChart } from "../../components/charts/StatusChart";
import { WeeklyChart } from "../../components/charts/WeeklyChart";
import { FunnelChart } from "../../components/charts/FunnelChart";
import { useDashboardQuery } from "./hooks";
import { StatCards } from "./StatCards";
import { SourceTable } from "./SourceTable";
import { DueReminders } from "./DueReminders";
import { UpcomingInterviews } from "./UpcomingInterviews";

export function DashboardPage() {
  const { t, language } = useI18n();
  const navigate = useNavigate();
  const { data, isLoading, isError, error, refetch } = useDashboardQuery();

  if (isLoading) return <Spinner fullPage />;

  if (isError || !data) {
    return (
      <Card className="p-6">
        <EmptyState
          title={t("errors.loadFailed")}
          description={errorMessage(error, t("errors.generic"))}
          action={
            <Button variant="outline" onClick={() => void refetch()}>
              {t("common.retry")}
            </Button>
          }
        />
      </Card>
    );
  }

  const isEmpty = data.totalApplications === 0;

  return (
    <div className="space-y-5">
      <section className="relative overflow-hidden rounded-2xl bg-slate-950 px-6 py-7 text-white sm:px-8 sm:py-8">
        <div
          className="pointer-events-none absolute -right-10 -top-16 h-56 w-56 rounded-full bg-primary-600/40 blur-3xl"
          aria-hidden="true"
        />
        <div
          className="pointer-events-none absolute -bottom-20 left-1/3 h-56 w-56 rounded-full bg-accent-500/20 blur-3xl"
          aria-hidden="true"
        />
        <div className="relative flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="text-[0.65rem] font-bold uppercase tracking-[0.2em] text-accent-300">
              {t("dashboard.heroEyebrow")}
            </p>
            <h2 className="mt-2 font-display text-2xl font-semibold leading-tight sm:text-3xl">
              {t("dashboard.heroTitle")}{" "}
              <span className="text-accent-300">{t("dashboard.heroHighlight")}</span>
            </h2>
            <p className="mt-2 max-w-lg text-sm text-slate-300">{t("dashboard.heroSubtitle")}</p>
          </div>
          <div className="flex items-center gap-4 rounded-xl border border-white/10 bg-white/5 px-4 py-3">
            <div>
              <p className="text-[0.65rem] uppercase tracking-wide text-slate-400">
                {t("dashboard.stats.averageResponse")}
              </p>
              <p className="font-display text-xl font-semibold">
                {t("dashboard.stats.averageResponseUnit", {
                  days: formatNumber(data.averageResponseDays, language),
                })}
              </p>
            </div>
            <Sparkles className="h-6 w-6 text-accent-300" aria-hidden="true" />
          </div>
        </div>
      </section>

      {isEmpty ? (
        <Card className="p-6">
          <EmptyState
            title={t("dashboard.empty.title")}
            description={t("dashboard.empty.description")}
            action={
              <Button onClick={() => navigate("/applications?new=1")} leftIcon={<ArrowRight className="h-4 w-4" />}>
                {t("dashboard.empty.action")}
              </Button>
            }
          />
        </Card>
      ) : (
        <>
          <StatCards data={data} />

          <div className="grid gap-4 lg:grid-cols-3">
            <Card className="p-5 lg:col-span-1">
              <CardHeader title={t("dashboard.funnel.title")} description={t("dashboard.funnel.subtitle")} />
              <div className="mt-3">
                <FunnelChart data={data.funnel} />
              </div>
            </Card>
            <Card className="p-5 lg:col-span-2">
              <CardHeader title={t("dashboard.weekly.title")} description={t("dashboard.weekly.subtitle")} />
              <div className="mt-3">
                <WeeklyChart data={data.weekly} />
              </div>
            </Card>
          </div>

          <div className="grid gap-4 lg:grid-cols-2">
            <Card className="p-5">
              <CardHeader title={t("dashboard.status.title")} description={t("dashboard.status.subtitle")} />
              <div className="mt-3">
                <StatusChart data={data.applicationsByStatus} />
              </div>
            </Card>
            <Card className="p-5">
              <CardHeader
                title={t("dashboard.source.title")}
                description={t("dashboard.source.subtitle")}
                action={
                  <Button variant="ghost" size="sm" onClick={() => navigate("/applications")}>
                    {t("dashboard.viewAll")}
                  </Button>
                }
              />
              <div className="mt-3">
                <SourceTable data={data.topSources.length ? data.topSources : data.applicationsBySource} />
              </div>
            </Card>
          </div>

          <div className="grid gap-4 lg:grid-cols-2">
            <Card className="p-5">
              <CardHeader title={t("dashboard.reminders.title")} />
              <div className="mt-2">
                <DueReminders reminders={data.dueReminders} />
              </div>
            </Card>
            <Card className="p-5">
              <CardHeader title={t("dashboard.interviews.title")} />
              <div className="mt-2">
                <UpcomingInterviews interviews={data.upcomingInterviews} />
              </div>
            </Card>
          </div>
        </>
      )}
    </div>
  );
}
