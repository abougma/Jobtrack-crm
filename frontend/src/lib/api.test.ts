import { describe, expect, it } from "vitest";
import { ApiError, buildQuery, errorMessage } from "./api";

describe("api helpers", () => {
  it("builds a query string and skips empty values", () => {
    expect(buildQuery({ page: 0, size: 20, status: "", source: undefined, search: null })).toBe(
      "?page=0&size=20",
    );
    expect(buildQuery({})).toBe("");
  });

  it("exposes field errors from a problem detail", () => {
    const error = new ApiError(400, "Requête invalide", {
      detail: "Requête invalide",
      errors: [{ field: "company", message: "ne doit pas être vide" }],
    });
    expect(error.fieldError("company")).toBe("ne doit pas être vide");
    expect(error.fieldError("position")).toBeUndefined();
  });

  it("prefers the api error message and falls back otherwise", () => {
    expect(errorMessage(new ApiError(500, "Serveur indisponible"), "fallback")).toBe(
      "Serveur indisponible",
    );
    expect(errorMessage(new Error("boom"), "fallback")).toBe("boom");
    expect(errorMessage("unknown", "fallback")).toBe("fallback");
  });
});
