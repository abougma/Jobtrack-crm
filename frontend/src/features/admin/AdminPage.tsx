import { useEffect, useState, type FormEvent, type ReactNode } from "react";
import { Building2, ShieldCheck, UserPlus, Users } from "lucide-react";
import { errorMessage } from "../../lib/api";
import type { Plan, UserRole } from "../../lib/auth";
import { formatDate } from "../../lib/format";
import { useI18n, type TranslationKey } from "../../lib/i18n";
import { fullName } from "../../lib/utils";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Card, CardHeader } from "../../components/ui/Card";
import { Dialog } from "../../components/ui/Dialog";
import { EmptyState } from "../../components/ui/EmptyState";
import { Input } from "../../components/ui/Input";
import { Progress } from "../../components/ui/Progress";
import { Select, type SelectOption } from "../../components/ui/Select";
import { Spinner } from "../../components/ui/Spinner";
import { useToast } from "../../components/ui/toast-context";
import { useAuth } from "../auth/AuthProvider";
import {
  useAdminOrganizationsQuery,
  useCreateAdminUserMutation,
  useAdminOverviewQuery,
  useAdminUserQuery,
  useAdminUsersQuery,
  useUpdateUserPlanMutation,
  useUpdateUserRoleMutation,
} from "./hooks";

const PLATFORM_ROLES: UserRole[] = ["JOB_SEEKER", "PLATFORM_ADMIN"];
const PAGE_SIZE = 10;

function roleKey(role: string): TranslationKey {
  return `profile.role.${role}` as TranslationKey;
}

function planLabelKey(plan: string): TranslationKey {
  if (plan === "PRO") return "settings.plan.pro";
  if (plan === "ORGANIZATION") return "settings.plan.organization";
  return "settings.plan.free";
}

function Kpi({ label, value, icon }: { label: string; value: number; icon: ReactNode }) {
  return (
    <Card className="flex items-center gap-3 p-4">
      <span className="grid h-10 w-10 shrink-0 place-items-center rounded-xl bg-primary-50 text-primary-600 dark:bg-primary-900/40 dark:text-primary-200">
        {icon}
      </span>
      <div className="min-w-0">
        <p className="text-xs font-semibold uppercase tracking-wide text-muted">{label}</p>
        <p className="font-display text-2xl font-bold text-fg">{value}</p>
      </div>
    </Card>
  );
}

export function AdminPage() {
  const { t, language } = useI18n();
  const { user } = useAuth();
  const { push } = useToast();

  const [search, setSearch] = useState("");
  const [query, setQuery] = useState("");
  const [planFilter, setPlanFilter] = useState<Plan | "">("");
  const [roleFilter, setRoleFilter] = useState<UserRole | "">("");
  const [page, setPage] = useState(0);
  const [selectedId, setSelectedId] = useState<string | null>(null);

  const [createOpen, setCreateOpen] = useState(false);
  const [newEmail, setNewEmail] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [newRole, setNewRole] = useState<UserRole>("JOB_SEEKER");

  const [orgSearch, setOrgSearch] = useState("");
  const [orgQuery, setOrgQuery] = useState("");
  const [orgPage, setOrgPage] = useState(0);

  useEffect(() => {
    const timer = setTimeout(() => {
      setQuery(search.trim());
      setPage(0);
    }, 300);
    return () => clearTimeout(timer);
  }, [search]);

  useEffect(() => {
    const timer = setTimeout(() => {
      setOrgQuery(orgSearch.trim());
      setOrgPage(0);
    }, 300);
    return () => clearTimeout(timer);
  }, [orgSearch]);

  const overviewQuery = useAdminOverviewQuery();
  const usersQuery = useAdminUsersQuery({
    query: query || undefined,
    plan: planFilter || undefined,
    role: roleFilter || undefined,
    page,
    size: PAGE_SIZE,
  });
  const organizationsQuery = useAdminOrganizationsQuery({
    query: orgQuery || undefined,
    page: orgPage,
    size: PAGE_SIZE,
  });
  const detailQuery = useAdminUserQuery(selectedId);
  const updatePlan = useUpdateUserPlanMutation();
  const updateRole = useUpdateUserRoleMutation();
  const createUser = useCreateAdminUserMutation();

  const planOptions: SelectOption[] = [
    { value: "FREE", label: t("settings.plan.free") },
    { value: "PRO", label: t("settings.plan.pro") },
    { value: "ORGANIZATION", label: t("settings.plan.organization") },
  ];
  const roleOptions: SelectOption[] = PLATFORM_ROLES.map((role) => ({
    value: role,
    label: t(roleKey(role)),
  }));

  const notifyUpdate = async (action: () => Promise<unknown>) => {
    try {
      await action();
      push(t("admin.updated"), "success");
    } catch (error) {
      push(errorMessage(error, t("admin.updateError")), "error");
    }
  };

  const handlePlanChange = (id: string, plan: string) =>
    notifyUpdate(() => updatePlan.mutateAsync({ id, plan }));

  const handleRoleChange = (id: string, role: string) =>
    notifyUpdate(() => updateRole.mutateAsync({ id, role }));

  const closeCreateDialog = () => {
    setCreateOpen(false);
    setNewEmail("");
    setNewPassword("");
    setNewRole("JOB_SEEKER");
  };

  const handleCreateUser = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await createUser.mutateAsync({
        email: newEmail.trim(),
        password: newPassword,
        role: newRole,
      });
      push(t("admin.create.created"), "success");
      closeCreateDialog();
    } catch (error) {
      push(errorMessage(error, t("admin.create.error")), "error");
    }
  };

  const overview = overviewQuery.data;
  const users = usersQuery.data?.content ?? [];
  const organizations = organizationsQuery.data?.content ?? [];
  const planTotal = overview?.usersByPlan.reduce((sum, metric) => sum + metric.users, 0) ?? 0;
  const detail = detailQuery.data;

  return (
    <div className="flex flex-col gap-6">
      <header>
        <p className="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.16em] text-muted">
          <ShieldCheck className="h-4 w-4 text-primary-600 dark:text-primary-300" aria-hidden="true" />
          {t("admin.eyebrow")}
        </p>
        <h1 className="mt-1 font-display text-2xl font-bold tracking-tight text-fg sm:text-3xl">
          {t("admin.title")}
        </h1>
        <p className="mt-1 max-w-2xl text-sm text-muted">{t("admin.description")}</p>
      </header>

      {overviewQuery.isLoading && (
        <div className="grid place-items-center py-10">
          <Spinner />
        </div>
      )}

      {overviewQuery.isError && (
        <EmptyState
          title={t("admin.loadError")}
          action={
            <Button variant="outline" size="sm" onClick={() => void overviewQuery.refetch()}>
              {t("common.retry")}
            </Button>
          }
        />
      )}

      {overview && (
        <>
          <section aria-label={t("admin.kpi.section")} className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
            <Kpi label={t("admin.kpi.users")} value={overview.totalUsers} icon={<Users className="h-5 w-5" aria-hidden="true" />} />
            <Kpi label={t("admin.kpi.newUsers")} value={overview.newUsersLast30Days} icon={<Users className="h-5 w-5" aria-hidden="true" />} />
            <Kpi label={t("admin.kpi.admins")} value={overview.platformAdmins} icon={<ShieldCheck className="h-5 w-5" aria-hidden="true" />} />
            <Kpi label={t("admin.kpi.organizations")} value={overview.totalOrganizations} icon={<Building2 className="h-5 w-5" aria-hidden="true" />} />
            <Kpi label={t("admin.kpi.memberships")} value={overview.totalMemberships} icon={<Building2 className="h-5 w-5" aria-hidden="true" />} />
            <Kpi label={t("admin.kpi.applications")} value={overview.totalApplications} icon={<Users className="h-5 w-5" aria-hidden="true" />} />
          </section>

          <Card className="p-5">
            <CardHeader title={t("admin.plans.title")} description={t("admin.plans.description")} />
            <ul className="mt-4 grid gap-3 sm:grid-cols-3">
              {overview.usersByPlan.map((metric) => (
                <li key={metric.plan}>
                  <div className="flex items-baseline justify-between gap-2">
                    <span className="text-sm font-medium text-fg">{t(planLabelKey(metric.plan))}</span>
                    <span className="text-sm text-muted">
                      {t("admin.plans.users", { count: metric.users })}
                    </span>
                  </div>
                  <Progress
                    className="mt-2"
                    value={metric.users}
                    max={planTotal || 1}
                    label={t(planLabelKey(metric.plan))}
                  />
                </li>
              ))}
            </ul>
          </Card>
        </>
      )}

      <Card className="p-5">
        <CardHeader
          title={t("admin.users.title")}
          description={t("admin.users.description")}
          action={
            <div className="flex flex-wrap items-center gap-2">
              {usersQuery.data && (
                <Badge variant="info">
                  {t("admin.users.total", { count: usersQuery.data.totalElements })}
                </Badge>
              )}
              <Button
                size="sm"
                onClick={() => setCreateOpen(true)}
                leftIcon={<UserPlus className="h-4 w-4" aria-hidden="true" />}
              >
                {t("admin.users.create")}
              </Button>
            </div>
          }
        />

        <div className="mt-4 grid gap-3 sm:grid-cols-3">
          <Input
            label={t("admin.users.search")}
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="ada@example.com"
            type="search"
          />
          <Select
            label={t("admin.users.filterPlan")}
            value={planFilter}
            onChange={(event) => {
              setPlanFilter(event.target.value as Plan | "");
              setPage(0);
            }}
            options={planOptions}
            placeholder={t("admin.users.allPlans")}
          />
          <Select
            label={t("admin.users.filterRole")}
            value={roleFilter}
            onChange={(event) => {
              setRoleFilter(event.target.value as UserRole | "");
              setPage(0);
            }}
            options={roleOptions}
            placeholder={t("admin.users.allRoles")}
          />
        </div>

        <div className="mt-4 overflow-x-auto">
          {usersQuery.isLoading ? (
            <div className="grid place-items-center py-10">
              <Spinner />
            </div>
          ) : users.length === 0 ? (
            <EmptyState title={t("admin.users.empty")} />
          ) : (
            <table className="w-full min-w-[720px] border-collapse text-sm">
              <thead>
                <tr className="border-b border-border text-left text-xs uppercase tracking-wide text-muted">
                  <th scope="col" className="py-2 pr-3 font-semibold">{t("admin.users.email")}</th>
                  <th scope="col" className="py-2 pr-3 font-semibold">{t("admin.users.role")}</th>
                  <th scope="col" className="py-2 pr-3 font-semibold">{t("admin.users.plan")}</th>
                  <th scope="col" className="py-2 pr-3 font-semibold">{t("admin.users.createdAt")}</th>
                  <th scope="col" className="py-2 text-right font-semibold">{t("admin.users.actions")}</th>
                </tr>
              </thead>
              <tbody>
                {users.map((candidate) => (
                  <tr key={candidate.id} className="border-b border-border/60 last:border-0">
                    <td className="py-3 pr-3">
                      <p className="font-medium text-fg">{candidate.email}</p>
                      <p className="text-xs text-muted">
                        {fullName(candidate.firstName, candidate.lastName) || t("common.notProvided")}
                      </p>
                    </td>
                    <td className="py-3 pr-3">
                      <Badge variant={candidate.role === "PLATFORM_ADMIN" ? "accent" : "neutral"}>
                        {t(roleKey(candidate.role))}
                      </Badge>
                    </td>
                    <td className="py-3 pr-3">
                      <Select
                        aria-label={t("admin.users.planFor", { email: candidate.email })}
                        value={candidate.plan}
                        options={planOptions}
                        containerClassName="min-w-[9rem]"
                        onChange={(event) => void handlePlanChange(candidate.id, event.target.value)}
                        disabled={updatePlan.isPending}
                      />
                    </td>
                    <td className="py-3 pr-3 text-muted">{formatDate(candidate.createdAt, language)}</td>
                    <td className="py-3 text-right">
                      <Button variant="ghost" size="sm" onClick={() => setSelectedId(candidate.id)}>
                        {t("admin.users.view")}
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {usersQuery.data && usersQuery.data.totalPages > 1 && (
          <div className="mt-4 flex items-center justify-between gap-3">
            <p className="text-sm text-muted">
              {t("admin.users.page", {
                page: usersQuery.data.number + 1,
                pages: usersQuery.data.totalPages,
              })}
            </p>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                disabled={page === 0}
                onClick={() => setPage((current) => Math.max(0, current - 1))}
              >
                {t("common.previous")}
              </Button>
              <Button
                variant="outline"
                size="sm"
                disabled={page + 1 >= usersQuery.data.totalPages}
                onClick={() => setPage((current) => current + 1)}
              >
                {t("common.next")}
              </Button>
            </div>
          </div>
        )}
      </Card>

      <Card className="p-5">
        <CardHeader
          title={t("admin.organizations.title")}
          description={t("admin.organizations.description")}
          action={
            organizationsQuery.data ? (
              <Badge variant="info">
                {t("admin.organizations.total", { count: organizationsQuery.data.totalElements })}
              </Badge>
            ) : undefined
          }
        />
        <div className="mt-4 max-w-md">
          <Input
            label={t("admin.organizations.search")}
            value={orgSearch}
            onChange={(event) => setOrgSearch(event.target.value)}
            type="search"
          />
        </div>
        <div className="mt-4 overflow-x-auto">
          {organizationsQuery.isLoading ? (
            <div className="grid place-items-center py-10">
              <Spinner />
            </div>
          ) : organizations.length === 0 ? (
            <EmptyState title={t("admin.organizations.empty")} />
          ) : (
            <table className="w-full min-w-[520px] border-collapse text-sm">
              <thead>
                <tr className="border-b border-border text-left text-xs uppercase tracking-wide text-muted">
                  <th scope="col" className="py-2 pr-3 font-semibold">{t("admin.organizations.name")}</th>
                  <th scope="col" className="py-2 pr-3 font-semibold">{t("admin.organizations.members")}</th>
                  <th scope="col" className="py-2 font-semibold">{t("admin.organizations.createdAt")}</th>
                </tr>
              </thead>
              <tbody>
                {organizations.map((organization) => (
                  <tr key={organization.id} className="border-b border-border/60 last:border-0">
                    <td className="py-3 pr-3 font-medium text-fg">{organization.name}</td>
                    <td className="py-3 pr-3 text-muted">{organization.members}</td>
                    <td className="py-3 text-muted">{formatDate(organization.createdAt, language)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
        {organizationsQuery.data && organizationsQuery.data.totalPages > 1 && (
          <div className="mt-4 flex items-center justify-between gap-3">
            <p className="text-sm text-muted">
              {t("admin.users.page", {
                page: organizationsQuery.data.number + 1,
                pages: organizationsQuery.data.totalPages,
              })}
            </p>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                disabled={orgPage === 0}
                onClick={() => setOrgPage((current) => Math.max(0, current - 1))}
              >
                {t("common.previous")}
              </Button>
              <Button
                variant="outline"
                size="sm"
                disabled={orgPage + 1 >= organizationsQuery.data.totalPages}
                onClick={() => setOrgPage((current) => current + 1)}
              >
                {t("common.next")}
              </Button>
            </div>
          </div>
        )}
      </Card>

      <Dialog
        open={Boolean(selectedId)}
        onClose={() => setSelectedId(null)}
        title={t("admin.detail.title", { email: detail?.email ?? "…" })}
        size="lg"
      >
        {detailQuery.isLoading && (
          <div className="grid place-items-center py-8">
            <Spinner />
          </div>
        )}
        {detailQuery.isError && <EmptyState title={t("admin.loadError")} />}
        {detail && (
          <div className="grid gap-5">
            <dl className="grid gap-3 sm:grid-cols-2">
              <div>
                <dt className="text-xs font-semibold uppercase tracking-wide text-muted">
                  {t("admin.users.name")}
                </dt>
                <dd className="text-sm text-fg">
                  {fullName(detail.firstName, detail.lastName) || t("common.notProvided")}
                </dd>
              </div>
              <div>
                <dt className="text-xs font-semibold uppercase tracking-wide text-muted">
                  {t("admin.users.createdAt")}
                </dt>
                <dd className="text-sm text-fg">{formatDate(detail.createdAt, language)}</dd>
              </div>
              <div>
                <dt className="text-xs font-semibold uppercase tracking-wide text-muted">
                  {t("admin.detail.applications")}
                </dt>
                <dd className="text-sm text-fg">{detail.applicationCount}</dd>
              </div>
              <div>
                <dt className="text-xs font-semibold uppercase tracking-wide text-muted">
                  {t("admin.detail.organizations")}
                </dt>
                <dd className="flex flex-wrap gap-1 text-sm text-fg">
                  {detail.organizations.length === 0
                    ? t("admin.detail.noOrganizations")
                    : detail.organizations.map((name) => (
                        <Badge key={name} variant="neutral">{name}</Badge>
                      ))}
                </dd>
              </div>
            </dl>

            <div className="grid gap-3 sm:grid-cols-2">
              <Select
                label={t("admin.users.plan")}
                value={detail.plan}
                options={planOptions}
                onChange={(event) => void handlePlanChange(detail.id, event.target.value)}
                disabled={updatePlan.isPending}
              />
              <Select
                label={t("admin.users.role")}
                value={detail.role}
                options={roleOptions}
                onChange={(event) => void handleRoleChange(detail.id, event.target.value)}
                disabled={updateRole.isPending || detail.id === user?.id}
                hint={detail.id === user?.id ? t("admin.detail.selfRoleHint") : t("admin.detail.roleHint")}
              />
            </div>

            <div>
              <div className="flex items-center justify-between gap-2">
                <p className="text-sm font-medium text-fg">{t("admin.detail.usage")}</p>
                <p className="text-sm text-muted">
                  {t("admin.detail.usageValue", {
                    used: detail.cvAnalysisUsed,
                    limit: detail.cvAnalysisLimit,
                  })}
                </p>
              </div>
              <Progress
                className="mt-2"
                value={detail.cvAnalysisUsed}
                max={detail.cvAnalysisLimit || 1}
                label={t("admin.detail.usage")}
              />
              <p className="mt-2 text-xs text-muted">
                {t("admin.detail.reset", { date: formatDate(detail.cvAnalysisResetAt, language) })}
              </p>
            </div>
          </div>
        )}
      </Dialog>

      <Dialog
        open={createOpen}
        onClose={closeCreateDialog}
        title={t("admin.create.title")}
        description={t("admin.create.description")}
        footer={
          <>
            <Button variant="ghost" onClick={closeCreateDialog}>
              {t("common.cancel")}
            </Button>
            <Button
              type="submit"
              form="admin-create-user"
              loading={createUser.isPending}
              leftIcon={<UserPlus className="h-4 w-4" aria-hidden="true" />}
            >
              {t("admin.create.submit")}
            </Button>
          </>
        }
      >
        <form id="admin-create-user" onSubmit={handleCreateUser} className="grid gap-3">
          <Input
            label={t("admin.create.email")}
            type="email"
            autoComplete="off"
            value={newEmail}
            onChange={(event) => setNewEmail(event.target.value)}
            required
          />
          <Input
            label={t("admin.create.password")}
            type="password"
            autoComplete="new-password"
            value={newPassword}
            onChange={(event) => setNewPassword(event.target.value)}
            required
            minLength={12}
            hint={t("admin.create.passwordHint")}
          />
          <Select
            label={t("admin.create.role")}
            value={newRole}
            onChange={(event) => setNewRole(event.target.value as UserRole)}
            options={roleOptions}
            hint={t("admin.create.roleHint")}
          />
        </form>
      </Dialog>
    </div>
  );
}
