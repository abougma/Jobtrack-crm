import { useState, type ChangeEvent, type FormEvent, type ReactElement } from "react";
import { CalendarDays, Fingerprint, ShieldCheck, UserRound } from "lucide-react";
import { Link } from "react-router-dom";
import { errorMessage } from "../../lib/api";
import { useI18n } from "../../lib/i18n";
import { Button } from "../../components/ui/Button";
import { Card, CardHeader } from "../../components/ui/Card";
import { EmptyState } from "../../components/ui/EmptyState";
import { Input } from "../../components/ui/Input";
import { Spinner } from "../../components/ui/Spinner";
import { useToast } from "../../components/ui/toast-context";
import { useAuth } from "../auth/AuthProvider";
import { CollectionSection } from "./CollectionSection";
import { useProfileQuery, useUpdateIdentityMutation } from "./hooks";
import { PROFILE_SECTIONS } from "./profileFields";
import { useChangePlanMutation } from "../settings/hooks";
import type { Plan } from "../../lib/auth";

export function ProfilePage() {
  const { t } = useI18n();
  const { user } = useAuth();
  const profileQuery = useProfileQuery();

  return (
    <div className="space-y-5">
      <Card className="p-5">
        <CardHeader title={t("profile.subtitle")} action={<Link to="/cv" className="rounded-lg bg-primary-600 px-3 py-2 text-xs font-semibold text-white hover:bg-primary-700">{t("profile.generateCv")}</Link>} />
        <p className="mt-3 flex items-start gap-2 rounded-lg bg-primary-50 px-3 py-2 text-xs text-primary-800 dark:bg-primary-900/30 dark:text-primary-100">
          <ShieldCheck className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
          {t("profile.guardrail")}
        </p>
      </Card>

      {user && <IdentityForm key={user.id} />}
      {user && <AccountInformation user={user} items={profileQuery.data} />}
      {user && <PlanUpgrade currentPlan={user.plan} />}

      {profileQuery.isLoading ? (
        <Spinner fullPage />
      ) : profileQuery.isError || !profileQuery.data ? (
        <Card className="p-6">
          <EmptyState
            title={t("errors.loadFailed")}
            description={errorMessage(profileQuery.error, t("errors.generic"))}
            action={
              <Button variant="outline" onClick={() => void profileQuery.refetch()}>
                {t("common.retry")}
              </Button>
            }
          />
        </Card>
      ) : (
        <div className="grid gap-4 xl:grid-cols-2">
          {PROFILE_SECTIONS.map((config) => (
            <CollectionSection
              key={config.kind}
              config={config}
              items={profileQuery.data?.[config.kind] ?? []}
            />
          ))}
        </div>
      )}
    </div>
  );
}

function PlanUpgrade({ currentPlan }: { currentPlan: string }) {
  const { t } = useI18n();
  const { push } = useToast();
  const mutation = useChangePlanMutation();
  const plans: { id: Plan; price: string }[] = [
    { id: "FREE", price: "0 €" },
    { id: "PRO", price: "9,99 € / mois" },
    { id: "PREMIUM", price: "19,99 € / mois" },
  ];

  return (
    <Card className="p-5">
      <CardHeader title={t("profile.plan.title")} description={t("profile.plan.subtitle")} />
      <div className="mt-4 grid gap-3 md:grid-cols-3">
        {plans.map((plan) => (
          <div
            key={plan.id}
            className={`rounded-xl border p-4 ${
              currentPlan === plan.id
                ? "border-primary-500 bg-primary-50 dark:bg-primary-900/20"
                : "border-border"
            }`}
          >
            <div className="flex items-center justify-between">
              <strong>{t(`profile.plan.${plan.id}` as Parameters<typeof t>[0])}</strong>
              {currentPlan === plan.id && (
                <span className="text-xs text-primary-700">{t("profile.plan.current")}</span>
              )}
            </div>
            <p className="mt-2 text-sm text-muted">{plan.price}</p>
            <Button
              className="mt-4 w-full"
              variant={currentPlan === plan.id ? "outline" : "primary"}
              disabled={currentPlan === plan.id}
              loading={mutation.isPending}
              onClick={() =>
                mutation.mutate(plan.id, {
                  onSuccess: () => push(t("profile.plan.updated"), "success"),
                  onError: () => push(t("errors.saveFailed"), "error"),
                })
              }
            >
              {currentPlan === plan.id ? t("profile.plan.current") : t("profile.plan.choose")}
            </Button>
          </div>
        ))}
      </div>
      <p className="mt-3 text-xs text-muted">{t("profile.plan.billingNote")}</p>
    </Card>
  );
}

function AccountInformation({ user, items }: { user: NonNullable<ReturnType<typeof useAuth>["user"]>; items?: NonNullable<ReturnType<typeof useProfileQuery>["data"]> }) {
  const { t, language } = useI18n();
  const totalItems = items ? Object.values(items).reduce((total, collection) => total + collection.length, 0) : 0;
  const createdAt = user.createdAt
    ? new Intl.DateTimeFormat(language === "fr" ? "fr-FR" : "en-US", { dateStyle: "long" }).format(new Date(user.createdAt))
    : t("common.notProvided");

  return (
    <Card className="p-5">
      <CardHeader title={t("profile.account.title")} description={t("profile.account.subtitle")} />
      <div className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <AccountDetail icon={<UserRound className="h-4 w-4" />} label={t("profile.account.role")} value={t(`profile.role.${user.role}` as Parameters<typeof t>[0])} />
        <AccountDetail icon={<ShieldCheck className="h-4 w-4" />} label={t("profile.account.plan")} value={user.plan} />
        <AccountDetail icon={<CalendarDays className="h-4 w-4" />} label={t("profile.account.createdAt")} value={createdAt} />
        <AccountDetail icon={<Fingerprint className="h-4 w-4" />} label={t("profile.account.items")} value={String(totalItems)} />
      </div>
      <p className="mt-4 text-xs text-muted">{t("profile.account.identifier")}: {user.id}</p>
    </Card>
  );
}

function AccountDetail({ icon, label, value }: { icon: ReactElement; label: string; value: string }) {
  return <div className="rounded-lg border border-border bg-elevated/50 p-3"><span className="flex items-center gap-2 text-xs text-muted">{icon}{label}</span><strong className="mt-2 block truncate text-sm text-fg">{value}</strong></div>;
}

function IdentityForm() {
  const { t } = useI18n();
  const { user } = useAuth();
  const { push } = useToast();
  const mutation = useUpdateIdentityMutation();
  const [values, setValues] = useState({
    firstName: user?.firstName ?? "",
    lastName: user?.lastName ?? "",
    headline: user?.headline ?? "",
    location: user?.location ?? "",
  });

  const update = (field: keyof typeof values) => (event: ChangeEvent<HTMLInputElement>) =>
    setValues((current) => ({ ...current, [field]: event.target.value }));

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await mutation.mutateAsync({
        firstName: values.firstName.trim(),
        lastName: values.lastName.trim(),
        headline: values.headline.trim(),
        location: values.location.trim(),
      });
      push(t("profile.identity.saved"), "success");
    } catch (error) {
      push(errorMessage(error, t("errors.saveFailed")), "error");
    }
  };

  return (
    <Card className="p-5">
      <CardHeader
        title={t("profile.identity.title")}
        description={t("profile.identity.subtitle")}
        action={<span className="text-xs text-muted">{user?.email}</span>}
      />
      <form onSubmit={submit} className="mt-4 space-y-4">
        <div className="grid gap-3 sm:grid-cols-2">
          <Input
            label={t("profile.identity.firstName")}
            value={values.firstName}
            onChange={update("firstName")}
            autoComplete="given-name"
          />
          <Input
            label={t("profile.identity.lastName")}
            value={values.lastName}
            onChange={update("lastName")}
            autoComplete="family-name"
          />
          <Input
            label={t("profile.identity.headline")}
            value={values.headline}
            onChange={update("headline")}
            placeholder={t("profile.identity.headlinePlaceholder")}
            containerClassName="sm:col-span-2"
          />
          <Input
            label={t("profile.identity.location")}
            value={values.location}
            onChange={update("location")}
            placeholder={t("profile.identity.locationPlaceholder")}
            autoComplete="address-level2"
            containerClassName="sm:col-span-2"
          />
        </div>
        <div className="flex justify-end">
          <Button type="submit" loading={mutation.isPending}>
            {mutation.isPending ? t("common.saving") : t("common.save")}
          </Button>
        </div>
      </form>
    </Card>
  );
}
