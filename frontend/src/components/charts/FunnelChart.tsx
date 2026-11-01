import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  LabelList,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { useI18n } from "../../lib/i18n";
import type { FunnelStage } from "../../features/dashboard/types";

const colors = ["var(--chart-1)", "var(--chart-3)", "var(--chart-2)", "var(--chart-4)"];

const tooltipStyle = {
  backgroundColor: "rgb(var(--surface))",
  border: "1px solid rgb(var(--border))",
  borderRadius: 12,
  color: "rgb(var(--fg))",
  fontSize: 12,
};

export function FunnelChart({ data }: { data: FunnelStage[] }) {
  const { t } = useI18n();

  if (data.length === 0) {
    return <p className="py-12 text-center text-sm text-muted">{t("dashboard.noData")}</p>;
  }

  return (
    <div className="h-[260px] w-full">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart
          data={data}
          layout="vertical"
          margin={{ top: 4, right: 28, bottom: 4, left: 8 }}
          barCategoryGap={14}
        >
          <CartesianGrid stroke="var(--chart-grid)" strokeDasharray="4 4" horizontal={false} />
          <XAxis type="number" hide />
          <YAxis
            type="category"
            dataKey="stage"
            width={96}
            tick={{ fill: "rgb(var(--muted))", fontSize: 11 }}
            tickLine={false}
            axisLine={false}
          />
          <Tooltip contentStyle={tooltipStyle} />
          <Bar dataKey="count" radius={[0, 6, 6, 0]} maxBarSize={26}>
            {data.map((stage, index) => (
              <Cell key={stage.stage} fill={colors[index % colors.length]} />
            ))}
            <LabelList dataKey="count" position="right" fill="rgb(var(--fg))" fontSize={12} />
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
