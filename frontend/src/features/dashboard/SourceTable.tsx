import { useI18n } from "../../lib/i18n";
import { formatNumber, formatPercent } from "../../lib/format";
import type { SourceStat } from "./types";

export function SourceTable({ data }: { data: SourceStat[] }) {
  const { t, language } = useI18n();

  if (data.length === 0) {
    return <p className="py-10 text-center text-sm text-muted">{t("dashboard.noData")}</p>;
  }

  return (
    <div className="-mx-1 overflow-x-auto">
      <table className="w-full min-w-[420px] text-left text-sm">
        <thead>
          <tr className="border-b border-border text-xs uppercase tracking-wide text-muted">
            <th scope="col" className="px-2 py-2 font-semibold">
              {t("dashboard.source.source")}
            </th>
            <th scope="col" className="px-2 py-2 text-right font-semibold">
              {t("dashboard.source.count")}
            </th>
            <th scope="col" className="px-2 py-2 text-right font-semibold">
              {t("dashboard.source.responses")}
            </th>
            <th scope="col" className="px-2 py-2 text-right font-semibold">
              {t("dashboard.source.responseRate")}
            </th>
            <th scope="col" className="px-2 py-2 text-right font-semibold">
              {t("dashboard.source.interviewRate")}
            </th>
          </tr>
        </thead>
        <tbody>
          {data.map((row) => (
            <tr key={row.source} className="border-b border-border/60 last:border-0">
              <th scope="row" className="px-2 py-2.5 text-left font-medium text-fg">
                {row.source || "—"}
              </th>
              <td className="px-2 py-2.5 text-right text-muted">{formatNumber(row.count, language)}</td>
              <td className="px-2 py-2.5 text-right text-muted">
                {formatNumber(row.responses, language)}
              </td>
              <td className="px-2 py-2.5 text-right font-semibold text-fg">
                {formatPercent(row.responseRate, language)}
              </td>
              <td className="px-2 py-2.5 text-right text-muted">
                {formatPercent(row.interviewRate, language)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
