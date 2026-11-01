import { describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen } from "@testing-library/react";
import { I18nProvider } from "../../lib/i18n";
import { AdminPage } from "./AdminPage";

const refetch = vi.fn();
const mutateAsync = vi.fn().mockResolvedValue(undefined);

vi.mock("./hooks", () => ({
  useAdminOverviewQuery: () => ({
    data: {
      totalUsers: 42,
      platformAdmins: 2,
      newUsersLast30Days: 7,
      totalOrganizations: 3,
      totalMemberships: 15,
      totalApplications: 120,
      usersByPlan: [
        { plan: "FREE", users: 30 },
        { plan: "PRO", users: 10 },
        { plan: "ORGANIZATION", users: 2 },
      ],
    },
    isLoading: false,
    isError: false,
    refetch,
  }),
  useAdminUsersQuery: () => ({
    data: {
      content: [
        {
          id: "user-1",
          email: "ada@example.com",
          role: "JOB_SEEKER",
          plan: "FREE",
          firstName: "Ada",
          lastName: "Lovelace",
          createdAt: "2026-01-15T10:00:00Z",
        },
      ],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 10,
    },
    isLoading: false,
  }),
  useAdminOrganizationsQuery: () => ({
    data: {
      content: [
        { id: "org-1", name: "École 42", members: 12, createdAt: "2026-02-01T10:00:00Z" },
      ],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 10,
    },
    isLoading: false,
  }),
  useAdminUserQuery: () => ({ data: undefined, isLoading: false, isError: false }),
  useCreateAdminUserMutation: () => ({ mutateAsync, isPending: false }),
  useUpdateUserPlanMutation: () => ({ mutateAsync, isPending: false }),
  useUpdateUserRoleMutation: () => ({ mutateAsync, isPending: false }),
}));

vi.mock("../auth/AuthProvider", () => ({
  useAuth: () => ({ user: { id: "admin-1", role: "PLATFORM_ADMIN" } }),
}));

vi.mock("../../components/ui/toast-context", () => ({
  useToast: () => ({ push: vi.fn() }),
}));

function renderPage() {
  return render(
    <I18nProvider>
      <AdminPage />
    </I18nProvider>,
  );
}

describe("AdminPage", () => {
  it("affiche les indicateurs, les utilisateurs et les organisations", () => {
    renderPage();

    expect(screen.getByRole("heading", { name: "Administration JobTrack" })).toBeInTheDocument();
    expect(screen.getByText("42")).toBeInTheDocument();
    expect(screen.getByText("ada@example.com")).toBeInTheDocument();
    expect(screen.getByText("Ada Lovelace")).toBeInTheDocument();
    expect(screen.getByText("École 42")).toBeInTheDocument();
    expect(screen.getByText("30 compte(s)")).toBeInTheDocument();
  });

  it("propose le détail d'un compte", () => {
    renderPage();

    expect(screen.getByRole("button", { name: "Détail" })).toBeInTheDocument();
  });

  it("ouvre le formulaire de création de compte", () => {
    renderPage();

    fireEvent.click(screen.getByRole("button", { name: "Créer un compte" }));

    expect(screen.getByLabelText(/E-mail/)).toBeInTheDocument();
    expect(screen.getByLabelText(/Mot de passe/)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Créer le compte" })).toBeInTheDocument();
  });
});
