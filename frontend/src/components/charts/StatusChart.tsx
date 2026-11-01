import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";
import { useI18n } from "../../lib/i18n";
import { STATUS_LABEL_KEYS } from "../../lib/domain";
import type { StatusCount } from "../../features/dashboard/types";

const COLORS = [
  "var(--chart-1)",
  "var(--chart-2)",
  "var(--chart-3)",
  "var(--chart-4)",
  "var(--chart-5)",
  "#7c8aa5",
  "#c9a227",
  "#5b8def",
  "#9aa5b8",
];

const tooltipStyle = {
  backgroundColor: "rgb(var(--surface))",
  border: "1px solid rgb(var(--border))",
  borderRadius: 12,
  color: "rgb(var(--fg))",
  fontSize: 12,
};

export function StatusChart({ data }: { data: StatusCount[] }) {
  const { t } = useI18n();
  const rows = data
    .filter((item) => item.count > 0)
    .map((item) => ({ name: t(STATUS_LABEL_KEYS[item.status]), value: item.count }));

  if (rows.length === 0) {
    return <p className="py-12 text-center text-sm text-muted">{t("dashboard.noData")}</p>;
  }

  return (
    <div className="h-[260px] w-full">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie
            data={rows}
            dataKey="value"
            nameKey="name"
            innerRadius={55}
            outerRadius={90}
            paddingAngle={2}
            stroke="none"
          >
            {rows.map((row, index) => (
              <Cell key={row.name} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip contentStyle={tooltipStyle} />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
