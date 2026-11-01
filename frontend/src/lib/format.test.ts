import { describe, expect, it } from "vitest";
import { formatDate, formatPercent, formatSalary, toDateInputValue } from "./format";

describe("format helpers", () => {
  it("formats a salary range in euros", () => {
    const value = formatSalary(45000, 55000, "fr");
    expect(value).toContain("45");
    expect(value).toContain("55");
    expect(value).toContain("€");
  });

  it("returns an em dash when no salary is provided", () => {
    expect(formatSalary(null, null, "fr")).toBe("—");
  });

  it("formats percentages with the requested precision", () => {
    expect(formatPercent(42.5, "fr")).toContain("42");
    expect(formatPercent(null, "fr")).toBe("—");
  });

  it("converts ISO dates to date input values", () => {
    expect(toDateInputValue("2026-01-10")).toBe("2026-01-10");
    expect(toDateInputValue(null)).toBe("");
  });

  it("formats dates with the requested language", () => {
    expect(formatDate("2026-01-10", "fr", "yyyy")).toBe("2026");
    expect(formatDate(null, "fr")).toBe("—");
  });
});
