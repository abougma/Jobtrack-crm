import { useEffect, useState, type FormEvent } from "react";
import { Building2, Copy, LogOut, Mail, Plus, ShieldCheck, Users } from "lucide-react";
import { errorMessage } from "../../lib/api";
import { useI18n } from "../../lib/i18n";
import { formatDate, formatNumber, formatPercent } from "../../lib/format";
import {
  CONSENT_SCOPES,
  CONSENT_SCOPE_LABEL_KEYS,
  ORGANIZATION_ROLES,
  ORGANIZATION_ROLE_LABEL_KEYS,
  type ConsentScope,
  type OrganizationRole,
} from "../../lib/domain";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Card, CardHeader } from "../../components/ui/Card";
import { Dialog } from "../../components/ui/Dialog";
import { EmptyState } from "../../components/ui/EmptyState";
import { Input } from "../../components/ui/Input";
import { Select } from "../../components/ui/Select";
import { Spinner } from "../../components/ui/Spinner";
import { Tabs } from "../../components/ui/Tabs";
import { useToast } from "../../components/ui/toast-context";
import { useAuth } from "../auth/AuthProvider";
import {
  useConsentQuery,
  useCreateGroupMutation,
  useCreateOrganizationMutation,
  useInviteMemberMutation,
  useJoinOrganizationMutation,
  useLeaveOrganizationMutation,
  useOrganizationDashboardQuery,
  useOrganizationGroupsQuery,
  useOrganizationMembersQuery,
  useOrganizationsQuery,
  useRevokeConsentMutation,
  useUpdateConsentMutation,
} from "./hooks";
import type {
  InvitationResult,
  Organization,
  OrganizationDashboard,
  OrganizationMember,
} from "./types";

type OrganizationTab = "overview" | "groups" | "members" | "consent" | "invite";

export function OrganizationsPage() {
  const { t } = useI18n();
  const { push } = useToast();
  const listQuery = useOrganizationsQuery();
  const [selectedId, setSelectedId] = useState("");
  const [tab, setTab] = useState<OrganizationTab>("overview");
  const [groupId, setGroupId] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [joinOpen, setJoinOpen] = useState(false);
  const [leaveOpen, setLeaveOpen] = useState(false);
  const [newName, setNewName] = useState("");
  const [joinToken, setJoinToken] = useState("");

  const createMutation = useCreateOrganizationMutation();
  const joinMutation = useJoinOrganizationMutation();
  const leaveMutation = useLeaveOrganizationMutation();

  const organizations = listQuery.data ?? [];
  const selected = organizations.find((organization) => organization.id === selectedId) ?? organizations[0] ?? null;
  const canManage = selected?.role === "ORG_ADMIN";
  const canViewMembers = selected?.role === "ORG_ADMIN" || selected?.role === "ORG_COACH";

  const handleCreate = async (event: FormEvent) => {
    event.preventDefault();
    if (!newName.trim()) return;
    try {
      const organization = await createMutation.mutateAsync(newName.trim());
      setSelectedId(organization.id);
      setNewName("");
      setCreateOpen(false);
      push(t("organizations.created"), "success");
    } catch (error) {
      push(errorMessage(error, t("errors.saveFailed")), "error");
    }
  };

  const handleJoin = async (event: FormEvent) => {
    event.preventDefault();
    if (!joinToken.trim()) return;
    try {
      const organization = await joinMutation.mutateAsync(joinToken.trim());
      setSelectedId(organization.id);
      setJoinToken("");
      setJoinOpen(false);
      push(t("organizations.join.joined"), "success");
    } catch (error) {
      push(errorMessage(error, t("errors.saveFailed")), "error");
    }
  };

  const handleLeave = async () => {
    if (!selected) return;
    try {
      await leaveMutation.mutateAsync(selected.id);
      setSelectedId("");
      setLeaveOpen(false);
      push(t("organizations.left"), "success");
    } catch (error) {
      push(errorMessage(error, t("errors.saveFailed")), "error");
    }
  };

  if (listQuery.isLoading) return <Spinner fullPage />;

  return (
    <div className="space-y-5">
      <Card className="p-5">
        <CardHeader
          title={t("organizations.subtitle")}
          action={
            <div className="flex flex-wrap gap-2">
              <Button variant="outline" size="sm" onClick={() => setJoinOpen(true)} leftIcon={<Users className="h-4 w-4" />}>
                {t("organizations.join.submit")}
              </Button>
              <Button size="sm" onClick={() => setCreateOpen(true)} leftIcon={<Plus className="h-4 w-4" />}>
                {t("organizations.create.submit")}
              </Button>
            </div>
          }
        />
        {organizations.length > 0 && (
          <div className="mt-4 max-w-md">
            <Select
              label={t("organizations.select")}
              value={selected?.id ?? ""}
              onChange={(event) => {
                setSelectedId(event.target.value);
                setGroupId("");
              }}
              options={organizations.map((organization) => ({
                value: organization.id,
                label: `${organization.name} · ${t(ORGANIZATION_ROLE_LABEL_KEYS[organization.role])}`,
              }))}
            />
          </div>
        )}
      </Card>

      {!selected ? (
        <Card className="p-6">
          <EmptyState
            icon={<Building2 className="h-5 w-5" aria-hidden="true" />}
            title={t("organizations.list.empty")}
            description={t("organizations.empty")}
            action={
              <div className="flex flex-wrap justify-center gap-2">
                <Button onClick={() => setCreateOpen(true)}>{t("organizations.create.submit")}</Button>
                <Button variant="outline" onClick={() => setJoinOpen(true)}>
                  {t("organizations.join.submit")}
                </Button>
              </div>
            }
          />
        </Card>
      ) : (
        <>
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="font-display text-xl font-semibold text-fg">{selected.name}</h2>
              <p className="mt-0.5 text-xs text-muted">
                {t("organizations.role")} : {t(ORGANIZATION_ROLE_LABEL_KEYS[selected.role])} ·{" "}
                {t("organizations.memberCount", { count: selected.memberCount })}
              </p>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setLeaveOpen(true)}
              leftIcon={<LogOut className="h-4 w-4" />}
            >
              {t("organizations.leave")}
            </Button>
          </div>

          <Tabs
            value={tab}
            onChange={(value) => setTab(value as OrganizationTab)}
            ariaLabel={t("organizations.title")}
            items={[
              { value: "overview", label: t("organizations.tab.overview") },
              { value: "groups", label: t("organizations.tab.groups") },
              { value: "members", label: t("organizations.tab.members") },
              { value: "consent", label: t("organizations.tab.consent") },
              { value: "invite", label: t("organizations.tab.invite") },
            ]}
          />

          {tab === "overview" && (
            <OverviewPanel organization={selected} groupId={groupId} onGroupChange={setGroupId} />
          )}
          {tab === "groups" && <GroupsPanel organization={selected} canManage={canManage} />}
          {tab === "members" && (
            <MembersPanel
              organization={selected}
              canView={canViewMembers}
              onLeave={() => setLeaveOpen(true)}
            />
          )}
          {tab === "consent" && <ConsentPanel organization={selected} />}
          {tab === "invite" && <InvitePanel organization={selected} canManage={canManage} />}
        </>
      )}

      <Dialog
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        title={t("organizations.create.title")}
        footer={
          <>
            <Button variant="ghost" onClick={() => setCreateOpen(false)}>
              {t("common.cancel")}
            </Button>
            <Button
              type="submit"
              form="create-organization-form"
              loading={createMutation.isPending}
            >
              {t("organizations.create.submit")}
            </Button>
          </>
        }
      >
        <form id="create-organization-form" onSubmit={handleCreate} className="space-y-3">
          <Input
            label={t("organizations.create.name")}
            placeholder={t("organizations.create.namePlaceholder")}
            value={newName}
            onChange={(event) => setNewName(event.target.value)}
            required
            autoFocus
          />
        </form>
      </Dialog>

      <Dialog
        open={joinOpen}
        onClose={() => setJoinOpen(false)}
        title={t("organizations.join.title")}
        footer={
          <>
            <Button variant="ghost" onClick={() => setJoinOpen(false)}>
              {t("common.cancel")}
            </Button>
            <Button type="submit" form="join-organization-form" loading={joinMutation.isPending}>
              {t("organizations.join.submit")}
            </Button>
          </>
        }
      >
        <form id="join-organization-form" onSubmit={handleJoin} className="space-y-3">
          <Input
            label={t("organizations.join.token")}
            value={joinToken}
            onChange={(event) => setJoinToken(event.target.value)}
            required
            autoFocus
          />
        </form>
      </Dialog>

      <Dialog
        open={leaveOpen}
        onClose={() => setLeaveOpen(false)}
        title={t("organizations.leave")}
        description={t("organizations.leaveConfirm")}
        footer={
          <>
            <Button variant="ghost" onClick={() => setLeaveOpen(false)}>
              {t("common.cancel")}
            </Button>
            <Button variant="danger" onClick={() => void handleLeave()} loading={leaveMutation.isPending}>
              {t("organizations.leave")}
            </Button>
          </>
        }
      >
        <p className="text-sm text-muted">{selected?.name}</p>
      </Dialog>
    </div>
  );
}

function OverviewPanel({
  organization,
  groupId,
  onGroupChange,
}: {
  organization: Organization;
  groupId: string;
  onGroupChange: (value: string) => void;
}) {
  const { t, language } = useI18n();
  const dashboardQuery = useOrganizationDashboardQuery(organization.id, groupId);
  const groupsQuery = useOrganizationGroupsQuery(organization.id);

  if (dashboardQuery.isLoading) return <Spinner fullPage />;
  if (dashboardQuery.isError || !dashboardQuery.data) {
    return (
      <Card className="p-6">
        <EmptyState
          title={t("organizations.dashboard.loadError")}
          description={errorMessage(dashboardQuery.error, t("errors.generic"))}
          action={
            <Button variant="outline" onClick={() => void dashboardQuery.refetch()}>
              {t("common.retry")}
            </Button>
          }
        />
      </Card>
    );
  }

  const dashboard = dashboardQuery.data;
  const groups = groupsQuery.data ?? organization.groups;

  return (
    <div className="space-y-4">
      <Card className="p-5">
        <CardHeader
          title={t("organizations.dashboard.title")}
          action={
            groups.length > 0 ? (
              <div className="w-56">
                <Select
                  aria-label={t("organizations.tab.groups")}
                  value={groupId}
                  onChange={(event) => onGroupChange(event.target.value)}
                  placeholder={t("common.all")}
                  options={groups.map((group) => ({ value: group.id, label: group.name }))}
                />
              </div>
            ) : undefined
          }
        />
        <MetricGrid dashboard={dashboard} />
      </Card>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card className="p-5">
          <CardHeader title={t("organizations.tab.groups")} />
          {dashboard.groups.length === 0 ? (
            <p className="mt-3 text-sm text-muted">{t("organizations.dashboard.noGroups")}</p>
          ) : (
            <div className="mt-3 divide-y divide-border">
              {dashboard.groups.map((group) => (
                <div key={group.id} className="flex items-center justify-between gap-3 py-2.5">
                  <div className="min-w-0">
                    <p className="truncate text-sm font-semibold text-fg">{group.name}</p>
                    <p className="text-xs text-muted">
                      {t("organizations.group.members", { count: group.members })} ·{" "}
                      {t("organizations.dashboard.applicationsShort", { count: group.applications })}
                    </p>
                  </div>
                  <Badge variant="info">
                    {t("organizations.dashboard.interviewsShort", { count: group.interviews })}
                  </Badge>
                </div>
              ))}
            </div>
          )}
        </Card>

        <Card className="p-5">
          <CardHeader title={t("organizations.dashboard.memberDetails")} />
          {dashboard.memberDetails.length === 0 ? (
            <p className="mt-3 text-sm text-muted">{t("organizations.dashboard.noDetails")}</p>
          ) : (
            <div className="mt-3 divide-y divide-border">
              {dashboard.memberDetails.map((member) => (
                <div key={member.userId} className="flex items-center justify-between gap-3 py-2.5">
                  <div className="min-w-0">
                    <p className="truncate text-sm font-semibold text-fg">{member.displayName}</p>
                    <p className="text-xs text-muted">
                      {t("organizations.dashboard.applicationsShort", { count: member.applications })} ·{" "}
                      {t("organizations.dashboard.interviewsShort", { count: member.interviews })}
                    </p>
                  </div>
                  <Badge variant="accent">{t(CONSENT_SCOPE_LABEL_KEYS[member.consentScope])}</Badge>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>

      <p className="text-xs text-muted">
        {t("organizations.dashboard.membersConsent", { count: dashboard.membersWithConsent })} ·{" "}
        {t("organizations.dashboard.responseRate")} {formatPercent(dashboard.responseRate, language)} ·{" "}
        {t("organizations.dashboard.interviewRate")} {formatPercent(dashboard.interviewRate, language)}
      </p>
    </div>
  );
}

function MetricGrid({ dashboard }: { dashboard: OrganizationDashboard }) {
  const { t, language } = useI18n();
  const metrics = [
    { label: t("organizations.dashboard.members"), value: formatNumber(dashboard.members, language) },
    { label: t("organizations.dashboard.applications"), value: formatNumber(dashboard.applications, language) },
    { label: t("organizations.dashboard.interviews"), value: formatNumber(dashboard.interviews, language) },
    { label: t("organizations.dashboard.offers"), value: formatNumber(dashboard.offers, language) },
    { label: t("organizations.dashboard.responseRate"), value: formatPercent(dashboard.responseRate, language) },
    { label: t("organizations.dashboard.interviewRate"), value: formatPercent(dashboard.interviewRate, language) },
  ];

  return (
    <section className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
      {metrics.map((metric) => (
        <div key={metric.label} className="rounded-xl border border-border bg-elevated p-3">
          <p className="text-xs font-medium text-muted">{metric.label}</p>
          <p className="mt-1 font-display text-xl font-semibold text-fg">{metric.value}</p>
        </div>
      ))}
    </section>
  );
}

function GroupsPanel({ organization, canManage }: { organization: Organization; canManage: boolean }) {
  const { t } = useI18n();
  const { push } = useToast();
  const query = useOrganizationGroupsQuery(organization.id);
  const createMutation = useCreateGroupMutation(organization.id);
  const [name, setName] = useState("");

  const handleCreate = async (event: FormEvent) => {
    event.preventDefault();
    if (!name.trim()) return;
    try {
      await createMutation.mutateAsync(name.trim());
      setName("");
      push(t("organizations.group.created"), "success");
    } catch (error) {
      push(errorMessage(error, t("errors.saveFailed")), "error");
    }
  };

  return (
    <Card className="p-5">
      <CardHeader title={t("organizations.group.title")} />
      {canManage && (
        <form onSubmit={handleCreate} className="mt-4 flex flex-wrap items-end gap-2">
          <Input
            label={t("organizations.group.name")}
            placeholder={t("organizations.group.namePlaceholder")}
            value={name}
            onChange={(event) => setName(event.target.value)}
            containerClassName="min-w-56 flex-1"
          />
          <Button type="submit" loading={createMutation.isPending}>
            {t("organizations.group.create")}
          </Button>
        </form>
      )}

      {query.isLoading ? (
        <Spinner />
      ) : (query.data ?? []).length === 0 ? (
        <p className="mt-4 text-sm text-muted">{t("organizations.group.empty")}</p>
      ) : (
        <div className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {(query.data ?? []).map((group) => (
            <div key={group.id} className="rounded-xl border border-border bg-elevated p-3">
              <p className="text-sm font-semibold text-fg">{group.name}</p>
              <p className="mt-1 text-xs text-muted">
                {t("organizations.group.members", { count: group.memberCount })}
              </p>
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}

function MembersPanel({
  organization,
  canView,
  onLeave,
}: {
  organization: Organization;
  canView: boolean;
  onLeave: () => void;
}) {
  const { t, language } = useI18n();
  const { user } = useAuth();
  const query = useOrganizationMembersQuery(organization.id, canView);

  if (!canView) {
    return (
      <Card className="p-6">
        <EmptyState title={t("errors.forbidden")} description={t("organizations.members.loadError")} />
      </Card>
    );
  }

  if (query.isLoading) return <Spinner fullPage />;
  if (query.isError) {
    return (
      <Card className="p-6">
        <EmptyState
          title={t("organizations.members.loadError")}
          description={errorMessage(query.error, t("errors.forbidden"))}
          action={
            <Button variant="outline" onClick={() => void query.refetch()}>
              {t("common.retry")}
            </Button>
          }
        />
      </Card>
    );
  }

  const members = query.data ?? [];

  return (
    <Card className="p-5">
      <CardHeader title={t("organizations.members.title")} />
      {members.length === 0 ? (
        <p className="mt-4 text-sm text-muted">{t("organizations.members.empty")}</p>
      ) : (
        <div className="mt-4 divide-y divide-border">
          {members.map((member: OrganizationMember) => {
            const displayName =
              [member.firstName, member.lastName].filter(Boolean).join(" ").trim() || member.email || "—";
            const isYou = member.userId === user?.id;
            return (
              <div key={member.userId} className="flex flex-wrap items-center gap-3 py-3">
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-semibold text-fg">
                    {displayName}
                    {isYou && <span className="ml-1 text-xs font-normal text-muted">({t("organizations.members.you")})</span>}
                  </p>
                  <p className="truncate text-xs text-muted">
                    {member.email} · {t("organizations.members.joinedAt", { date: formatDate(member.joinedAt, language) })}
                  </p>
                </div>
                <Badge variant="info">{t(ORGANIZATION_ROLE_LABEL_KEYS[member.role])}</Badge>
                <Badge variant={member.consentScope ? "success" : "neutral"}>
                  {member.consentScope ? t(CONSENT_SCOPE_LABEL_KEYS[member.consentScope]) : t("organizations.consent.inactive")}
                </Badge>
                {isYou && (
                  <Button variant="ghost" size="sm" onClick={onLeave} leftIcon={<LogOut className="h-3.5 w-3.5" />}>
                    {t("organizations.leave")}
                  </Button>
                )}
              </div>
            );
          })}
        </div>
      )}
    </Card>
  );
}

function ConsentPanel({ organization }: { organization: Organization }) {
  const { t } = useI18n();
  const { push } = useToast();
  const query = useConsentQuery(organization.id);
  const updateMutation = useUpdateConsentMutation(organization.id);
  const revokeMutation = useRevokeConsentMutation(organization.id);
  const [scope, setScope] = useState<ConsentScope>("AGGREGATED");

  useEffect(() => {
    if (query.data?.scope) setScope(query.data.scope);
  }, [query.data?.scope]);

  const handleSave = async () => {
    try {
      await updateMutation.mutateAsync(scope);
      push(t("organizations.consent.saved"), "success");
    } catch (error) {
      push(errorMessage(error, t("errors.saveFailed")), "error");
    }
  };

  const handleRevoke = async () => {
    try {
      await revokeMutation.mutateAsync();
      push(t("organizations.consent.revoked"), "success");
    } catch (error) {
      push(errorMessage(error, t("errors.saveFailed")), "error");
    }
  };

  if (query.isLoading) return <Spinner fullPage />;

  const active = Boolean(query.data?.active);

  return (
    <Card className="p-5">
      <CardHeader
        title={t("organizations.consent.title")}
        description={t("organizations.consent.description")}
        action={
          <Badge variant={active ? "success" : "neutral"}>
            {active ? t("organizations.consent.active") : t("organizations.consent.inactive")}
          </Badge>
        }
      />
      <div className="mt-4 max-w-md space-y-3">
        <Select
          label={t("organizations.consent.scope")}
          value={scope}
          onChange={(event) => setScope(event.target.value as ConsentScope)}
          options={CONSENT_SCOPES.map((value) => ({
            value,
            label: t(CONSENT_SCOPE_LABEL_KEYS[value]),
          }))}
        />
        <div className="flex flex-wrap gap-2">
          <Button onClick={() => void handleSave()} loading={updateMutation.isPending} leftIcon={<ShieldCheck className="h-4 w-4" />}>
            {t("organizations.consent.save")}
          </Button>
          <Button
            variant="outline"
            onClick={() => void handleRevoke()}
            loading={revokeMutation.isPending}
            disabled={!active}
          >
            {t("organizations.consent.revoke")}
          </Button>
        </div>
      </div>
    </Card>
  );
}

function InvitePanel({ organization, canManage }: { organization: Organization; canManage: boolean }) {
  const { t, language } = useI18n();
  const { push } = useToast();
  const mutation = useInviteMemberMutation(organization.id);
  const [email, setEmail] = useState("");
  const [role, setRole] = useState<OrganizationRole>("MEMBER");
  const [result, setResult] = useState<InvitationResult | null>(null);

  if (!canManage) {
    return (
      <Card className="p-6">
        <EmptyState title={t("errors.forbidden")} description={t("organizations.invite.title")} />
      </Card>
    );
  }

  const handleInvite = async (event: FormEvent) => {
    event.preventDefault();
    if (!email.trim()) return;
    try {
      const invitation = await mutation.mutateAsync({ email: email.trim(), role });
      setResult(invitation);
      setEmail("");
      push(t("organizations.invite.created"), "success");
    } catch (error) {
      push(errorMessage(error, t("errors.saveFailed")), "error");
    }
  };

  const copyToken = async () => {
    if (!result) return;
    try {
      await navigator.clipboard.writeText(result.token);
      push(t("common.copied"), "success");
    } catch {
      push(t("errors.generic"), "error");
    }
  };

  return (
    <Card className="p-5">
      <CardHeader title={t("organizations.invite.title")} />
      <form onSubmit={handleInvite} className="mt-4 flex flex-wrap items-end gap-2">
        <Input
          label={t("organizations.invite.email")}
          type="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          required
          containerClassName="min-w-64 flex-1"
        />
        <Select
          label={t("organizations.invite.role")}
          value={role}
          onChange={(event) => setRole(event.target.value as OrganizationRole)}
          options={ORGANIZATION_ROLES.map((value) => ({
            value,
            label: t(ORGANIZATION_ROLE_LABEL_KEYS[value]),
          }))}
          containerClassName="w-44"
        />
        <Button type="submit" loading={mutation.isPending} leftIcon={<Mail className="h-4 w-4" />}>
          {t("organizations.invite.submit")}
        </Button>
      </form>

      {result && (
        <div className="mt-4 rounded-xl border border-border bg-elevated p-4">
          <p className="text-xs font-semibold text-fg">{t("organizations.invite.token")}</p>
          <div className="mt-2 flex flex-wrap items-center gap-2">
            <code className="min-w-0 flex-1 break-all rounded-lg bg-surface px-3 py-2 text-xs text-fg">
              {result.token}
            </code>
            <Button variant="outline" size="sm" onClick={() => void copyToken()} leftIcon={<Copy className="h-3.5 w-3.5" />}>
              {t("organizations.invite.copy")}
            </Button>
          </div>
          <p className="mt-2 text-xs text-muted">
            {t("organizations.invite.expires", { date: formatDate(result.expiresAt, language) })}
          </p>
        </div>
      )}
    </Card>
  );
}
