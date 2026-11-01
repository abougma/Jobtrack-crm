import { useEffect, useState } from "react";
import { ChevronDown, ChevronUp, RotateCcw, Search } from "lucide-react";
import { useI18n } from "../../lib/i18n";
import type { TranslationKey } from "../../lib/i18n";
import {
  APPLICATION_STATUSES,
  CONTRACT_LABEL_KEYS,
  CONTRACT_TYPES,
  STATUS_LABEL_KEYS,
  WORK_MODE_LABEL_KEYS,
  WORK_MODES,
} from "../../lib/domain";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { Select } from "../../components/ui/Select";
import { countActiveFilters } from "./filters";
import type { ApplicationFilters } from "./types";

interface ApplicationFiltersBarProps {
  filters: ApplicationFilters;
  onChange: (patch: Partial<ApplicationFilters>) => void;
  onReset: () => void;
}

const SORT_OPTIONS: { value: string; labelKey: TranslationKey }[] = [
  { value: "updatedAt,desc", labelKey: "applications.sort.updatedAt" },
  { value: "createdAt,desc", labelKey: "applications.sort.createdAt" },
  { value: "appliedAt,desc", labelKey: "applications.sort.appliedAt" },
  { value: "company,asc", labelKey: "applications.sort.company" },
  { value: "position,asc", labelKey: "applications.sort.position" },
  { value: "status,asc", labelKey: "applications.sort.status" },
];

export function ApplicationFiltersBar({ filters, onChange, onReset }: ApplicationFiltersBarProps) {
  const { t } = useI18n();
  const [expanded, setExpanded] = useState(false);
  const [search, setSearch] = useState(filters.search);

  useEffect(() => {
    setSearch(filters.search);
  }, [filters.search]);

  useEffect(() => {
    if (search === filters.search) return;
    const timer = window.setTimeout(() => {
      onChange({ search, page: 0 });
    }, 350);
    return () => window.clearTimeout(timer);
  }, [search, filters.search, onChange]);

  const activeCount = countActiveFilters(filters);

  return (
    <div className="space-y-3">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
        <div className="relative flex-1">
          <Search
            className="pointer-events-none absolute left-3 top-[2.35rem] h-4 w-4 text-muted"
            aria-hidden="true"
          />
          <Input
            label={t("common.search")}
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder={t("applications.searchPlaceholder")}
            className="pl-9"
            type="search"
          />
        </div>
        <Select
          label={t("applications.column.status")}
          value={filters.status}
          onChange={(event) => onChange({ status: event.target.value, page: 0 })}
          placeholder={t("applications.allStatuses")}
          options={APPLICATION_STATUSES.map((status) => ({
            value: status,
            label: t(STATUS_LABEL_KEYS[status]),
          }))}
          containerClassName="sm:w-48"
        />
        <Button
          variant="outline"
          onClick={() => setExpanded((current) => !current)}
          aria-expanded={expanded}
          aria-controls="advanced-filters"
          leftIcon={expanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
        >
          {expanded ? t("applications.filtersLess") : t("applications.filtersMore")}
          {activeCount > 0 && (
            <span className="ml-1 rounded-full bg-primary-600 px-1.5 text-[0.65rem] text-white">
              {activeCount}
            </span>
          )}
        </Button>
      </div>

      {expanded && (
        <div
          id="advanced-filters"
          className="grid grid-cols-1 gap-3 rounded-xl border border-border bg-surface p-3 sm:grid-cols-2 lg:grid-cols-4"
        >
          <Input
            label={t("applications.source")}
            value={filters.source}
            onChange={(event) => onChange({ source: event.target.value, page: 0 })}
            placeholder={t("applications.sourcePlaceholder")}
          />
          <Select
            label={t("applications.contract")}
            value={filters.contractType}
            onChange={(event) => onChange({ contractType: event.target.value, page: 0 })}
            placeholder={t("applications.allContracts")}
            options={CONTRACT_TYPES.map((contract) => ({
              value: contract,
              label: t(CONTRACT_LABEL_KEYS[contract]),
            }))}
          />
          <Select
            label={t("applications.workMode")}
            value={filters.workMode}
            onChange={(event) => onChange({ workMode: event.target.value, page: 0 })}
            placeholder={t("applications.allWorkModes")}
            options={WORK_MODES.map((mode) => ({
              value: mode,
              label: t(WORK_MODE_LABEL_KEYS[mode]),
            }))}
          />
          <Select
            label={t("applications.sortBy")}
            value={filters.sort}
            onChange={(event) => onChange({ sort: event.target.value, page: 0 })}
            options={SORT_OPTIONS.map((sort) => ({
              value: sort.value,
              label: t(sort.labelKey),
            }))}
          />
          <Input
            label={t("applications.dateFrom")}
            type="date"
            value={filters.from}
            onChange={(event) => onChange({ from: event.target.value, page: 0 })}
          />
          <Input
            label={t("applications.dateTo")}
            type="date"
            value={filters.to}
            onChange={(event) => onChange({ to: event.target.value, page: 0 })}
          />
          <div className="flex items-end">
            <Button
              variant="ghost"
              onClick={onReset}
              leftIcon={<RotateCcw className="h-4 w-4" aria-hidden="true" />}
            >
              {t("common.reset")}
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
