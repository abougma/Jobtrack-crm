import { describe, expect, it } from "vitest";
import { countActiveFilters, DEFAULT_FILTERS, filterApplications } from "./filters";
import type { ApplicationSummary } from "./types";

function application(overrides: Partial<ApplicationSummary>): ApplicationSummary {
  return {
    id: "id",
    company: "Acme",
    position: "Développeur Java",
    location: "Paris",
    workMode: "HYBRID",
    contractType: "PERMANENT",
    salaryMin: null,
    salaryMax: null,
    source: "LinkedIn",
    offerUrl: null,
    status: "APPLIED",
    appliedAt: "2026-01-10",
    contactId: null,
    contactName: null,
    nextReminderAt: null,
    interactionCount: 0,
    createdAt: "2026-01-10T10:00:00Z",
    updatedAt: "2026-01-10T10:00:00Z",
    ...overrides,
  };
}

describe("application filters", () => {
  const items = [
    application({ id: "1", company: "Acme", status: "APPLIED", source: "LinkedIn" }),
    application({ id: "2", company: "Globex", status: "INTERVIEW", source: "Réseau" }),
    application({ id: "3", company: "Initech", status: "REJECTED", workMode: "REMOTE" }),
  ];

  it("filters by status", () => {
    expect(filterApplications(items, { ...DEFAULT_FILTERS, status: "INTERVIEW" })).toHaveLength(1);
  });

  it("matches the search across company, position and location", () => {
    expect(filterApplications(items, { ...DEFAULT_FILTERS, search: "glob" })).toHaveLength(1);
    expect(filterApplications(items, { ...DEFAULT_FILTERS, search: "paris" })).toHaveLength(3);
  });

  it("returns every item when no filter is active", () => {
    expect(filterApplications(items, DEFAULT_FILTERS)).toHaveLength(3);
  });

  it("counts active filters", () => {
    expect(countActiveFilters(DEFAULT_FILTERS)).toBe(0);
    expect(countActiveFilters({ ...DEFAULT_FILTERS, status: "APPLIED", search: "acme" })).toBe(2);
  });
});
