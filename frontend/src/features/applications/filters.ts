import type { ApplicationFilters, ApplicationSummary } from "./types";

export const DEFAULT_FILTERS: ApplicationFilters = {
  search: "",
  status: "",
  source: "",
  contractType: "",
  workMode: "",
  from: "",
  to: "",
  sort: "updatedAt,desc",
  page: 0,
  size: 20,
};

export function filterApplications(
  items: ApplicationSummary[],
  filters: Pick<ApplicationFilters, "search" | "status" | "source" | "contractType" | "workMode">,
): ApplicationSummary[] {
  const search = filters.search.trim().toLowerCase();

  return items.filter((item) => {
    if (filters.status && item.status !== filters.status) return false;
    if (filters.source && item.source !== filters.source) return false;
    if (filters.contractType && item.contractType !== filters.contractType) return false;
    if (filters.workMode && item.workMode !== filters.workMode) return false;
    if (!search) return true;
    return [item.company, item.position, item.location, item.source]
      .filter(Boolean)
      .some((value) => value!.toLowerCase().includes(search));
  });
}

export function countActiveFilters(filters: ApplicationFilters): number {
  return [
    filters.search,
    filters.status,
    filters.source,
    filters.contractType,
    filters.workMode,
    filters.from,
    filters.to,
  ].filter(Boolean).length;
}
