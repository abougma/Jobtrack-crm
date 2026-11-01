import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { useI18n } from "../../lib/i18n";
import { formatShortDate } from "../../lib/format";
import type { WeeklyPoint } from "../../features/dashboard/types";

const tooltipStyle = {
  backgroundColor: "rgb(var(--surface))",
  border: "1px solid rgb(var(--border))",
  borderRadius: 12,
  color: "rgb(var(--fg))",
  fontSize: 12,
};

export function WeeklyChart({ data }: { data: WeeklyPoint[] }) {
  const { t, language } = useI18n();

  if (data.length === 0) {
    return <p className="py-12 text-center text-sm text-muted">{t("dashboard.noData")}</p>;
  }

  const rows = data.map((point) => ({
    week: formatShortDate(point.weekStart, language),
    applications: point.applications,
    responses: point.responses,
    interviews: point.interviews,
  }));

  return (
    <div className="h-[260px] w-full">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={rows} margin={{ top: 8, right: 8, bottom: 0, left: -18 }}>
          <CartesianGrid stroke="var(--chart-grid)" strokeDasharray="4 4" vertical={false} />
          <XAxis
            dataKey="week"
            tick={{ fill: "rgb(var(--muted))", fontSize: 11 }}
            tickLine={false}
            axisLine={{ stroke: "var(--chart-grid)" }}
          />
          <YAxis
            allowDecimals={false}
            tick={{ fill: "rgb(var(--muted))", fontSize: 11 }}
            tickLine={false}
            axisLine={false}
          />
          <Tooltip contentStyle={tooltipStyle} />
          <Legend
            wrapperStyle={{ fontSize: 12, color: "rgb(var(--muted))" }}
            iconType="circle"
            iconSize={8}
          />
          <Line
            type="monotone"
            dataKey="applications"
            name={t("dashboard.chart.applications")}
            stroke="var(--chart-1)"
            strokeWidth={2}
            dot={false}
          />
          <Line
            type="monotone"
            dataKey="responses"
            name={t("dashboard.chart.responses")}
            stroke="var(--chart-3)"
            strokeWidth={2}
            dot={false}
          />
          <Line
            type="monotone"
            dataKey="interviews"
            name={t("dashboard.chart.interviews")}
            stroke="var(--chart-2)"
            strokeWidth={2}
            dot={false}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
