import { describe, expect, it } from "vitest";
import { createApplicationSchema } from "./schemas";
import type { TranslateFn } from "../../lib/i18n";

const t: TranslateFn = (key) => key;

describe("application schema", () => {
  const schema = createApplicationSchema(t);

  it("accepts a minimal application", () => {
    const result = schema.safeParse({ company: "Acme", position: "Développeur Java" });
    expect(result.success).toBe(true);
  });

  it("rejects an invalid salary range", () => {
    const result = schema.safeParse({
      company: "Acme",
      position: "Développeur Java",
      salaryMin: "55000",
      salaryMax: "45000",
    });
    expect(result.success).toBe(false);
  });

  it("rejects an invalid offer URL", () => {
    const result = schema.safeParse({
      company: "Acme",
      position: "Développeur Java",
      offerUrl: "not-a-url",
    });
    expect(result.success).toBe(false);
  });

  it("requires a company and a position", () => {
    expect(schema.safeParse({ company: "", position: "" }).success).toBe(false);
  });
});
