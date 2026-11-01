import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { EmptyState } from "./EmptyState";

describe("EmptyState", () => {
  it("renders a title and a description", () => {
    render(<EmptyState title="Aucun résultat" description="Essayez d'élargir vos filtres." />);
    expect(screen.getByText("Aucun résultat")).toBeInTheDocument();
    expect(screen.getByText("Essayez d'élargir vos filtres.")).toBeInTheDocument();
  });
});
