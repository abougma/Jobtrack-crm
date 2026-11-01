import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Download, FileText, Sparkles, Trash2 } from "lucide-react";
import { errorMessage } from "../../lib/api";
import { useI18n } from "../../lib/i18n";
import { formatDate } from "../../lib/format";
import { downloadBlob } from "../../lib/utils";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Card, CardHeader } from "../../components/ui/Card";
import { Dialog } from "../../components/ui/Dialog";
import { EmptyState } from "../../components/ui/EmptyState";
import { Input } from "../../components/ui/Input";
import { Progress } from "../../components/ui/Progress";
import { Select } from "../../components/ui/Select";
import { Spinner } from "../../components/ui/Spinner";
import { Tabs } from "../../components/ui/Tabs";
import { Textarea } from "../../components/ui/Textarea";
import { useToast } from "../../components/ui/toast-context";
import { useAuth } from "../auth/AuthProvider";
import { DEFAULT_FILTERS } from "../applications/filters";
import { useApplicationsQuery } from "../applications/hooks";
import { exportCv } from "./api";
import {
  useAnalyzeCvMutation,
  useCvListQuery,
  useDeleteCvMutation,
  useGenerateCvMutation,
  useGenerateCvFromProfileMutation,
} from "./hooks";
import type { CvAnalysis, CvExportFormat, CvSummary } from "./types";

type CvTab = "analyze" | "list";

export function CvPage() {
  const { t, language } = useI18n();
  const { push } = useToast();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState<CvTab>("analyze");
  const [offerText, setOfferText] = useState("");
  const [applicationId, setApplicationId] = useState("");
  const [title, setTitle] = useState("");
  const [analysis, setAnalysis] = useState<CvAnalysis | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [deleting, setDeleting] = useState<CvSummary | null>(null);
  const [profileTitle, setProfileTitle] = useState("CV — Profil professionnel");
  const [profileName, setProfileName] = useState("");
  const [profileLocation, setProfileLocation] = useState("");
  const [profileSummary, setProfileSummary] = useState("");

  const applicationsQuery = useApplicationsQuery({ ...DEFAULT_FILTERS, size: 200 }, true);
  const analyzeMutation = useAnalyzeCvMutation();
  const generateMutation = useGenerateCvMutation();
  const profileGenerateMutation = useGenerateCvFromProfileMutation();
  const deleteMutation = useDeleteCvMutation();
  const listQuery = useCvListQuery();

  useEffect(() => {
    if (!user) return;
    const name = [user.firstName, user.lastName].filter(Boolean).join(" ");
    setProfileName(name);
    setProfileLocation(user.location ?? "");
    setProfileSummary(
      user.headline
        ? `${user.headline}. Profil professionnel orienté vers des réalisations concrètes et une progression continue.`
        : "Professionnel motivé, prêt à mettre ses compétences et ses expériences au service d'une nouvelle équipe.",
    );
  }, [user]);

  const applicationOptions = (applicationsQuery.data?.content ?? []).map((application) => ({
    value: application.id,
    label: `${application.company} — ${application.position}`,
  }));

  const validateOffer = (): boolean => {
    if (offerText.trim().length < 40) {
      setFormError(t("validation.offerMin"));
      return false;
    }
    setFormError(null);
    return true;
  };

  const handleAnalyze = () => {
    if (!validateOffer()) return;
    analyzeMutation.mutate(
      { offerText, applicationId: applicationId || null },
      {
        onSuccess: (result) => setAnalysis(result),
        onError: (error) => setFormError(errorMessage(error, t("errors.generic"))),
      },
    );
  };

  const handleGenerate = () => {
    if (!validateOffer()) return;
    generateMutation.mutate(
      { offerText, applicationId: applicationId || null, title: title.trim() || null },
      {
        onSuccess: (cv) => {
          push(t("cv.generated"), "success");
          navigate(`/cv/${cv.id}`);
        },
        onError: (error) => setFormError(errorMessage(error, t("errors.saveFailed"))),
      },
    );
  };

  const handleGenerateFromProfile = () => {
    if (!profileName.trim() || !profileSummary.trim()) {
      setFormError(t("cv.profile.required"));
      return;
    }
    setFormError(null);
    profileGenerateMutation.mutate(
      {
        title: profileTitle.trim() || "CV — Profil professionnel",
        fullName: profileName.trim(),
        location: profileLocation.trim() || null,
        summary: profileSummary.trim(),
      },
      {
        onSuccess: (cv) => {
          push(t("cv.generated"), "success");
          navigate(`/cv/${cv.id}`);
        },
        onError: (error) => setFormError(errorMessage(error, t("errors.saveFailed"))),
      },
    );
  };

  const handleExport = async (id: string, format: CvExportFormat) => {
    try {
      const file = await exportCv(id, format);
      downloadBlob(file.blob, file.filename);
    } catch (error) {
      push(errorMessage(error, t("cv.export.error")), "error");
    }
  };

  const handleDelete = async () => {
    if (!deleting) return;
    try {
      await deleteMutation.mutateAsync(deleting.id);
      push(t("cv.list.deleted"), "success");
      setDeleting(null);
    } catch (error) {
      push(errorMessage(error, t("errors.deleteFailed")), "error");
    }
  };

  return (
    <div className="space-y-5">
      <Card className="p-5">
        <CardHeader title={t("cv.profile.title")} description={t("cv.profile.description")} />
        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          <Input label={t("cv.profile.titleField")} value={profileTitle} onChange={(event) => setProfileTitle(event.target.value)} />
          <Input label={t("cv.profile.name")} value={profileName} onChange={(event) => setProfileName(event.target.value)} />
          <Input label={t("cv.profile.location")} value={profileLocation} onChange={(event) => setProfileLocation(event.target.value)} />
          <Textarea label={t("cv.profile.summary")} value={profileSummary} onChange={(event) => setProfileSummary(event.target.value)} rows={3} required />
        </div>
        <p className="mt-3 text-xs text-muted">{t("cv.profile.atsNote")}</p>
        {formError && <p role="alert" className="mt-3 text-xs font-medium text-rose-600 dark:text-rose-400">{formError}</p>}
        <div className="mt-4 flex flex-wrap gap-2">
          <Button onClick={handleGenerateFromProfile} loading={profileGenerateMutation.isPending}>
            {profileGenerateMutation.isPending ? t("cv.generating") : t("cv.profile.generate")}
          </Button>
          <Button variant="outline" onClick={() => setTab("list")}>{t("cv.profile.viewSaved")}</Button>
        </div>
      </Card>
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="text-sm text-muted">{t("cv.subtitle")}</p>
        </div>
        <Tabs
          value={tab}
          onChange={(value) => setTab(value as CvTab)}
          ariaLabel={t("cv.title")}
          items={[
            { value: "analyze", label: t("cv.tab.analyze"), icon: <Sparkles className="h-3.5 w-3.5" /> },
            { value: "list", label: t("cv.tab.list"), icon: <FileText className="h-3.5 w-3.5" /> },
          ]}
        />
      </div>

      {tab === "analyze" ? (
        <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(0,1.1fr)]">
          <Card className="p-5">
            <CardHeader title={t("cv.offer.label")} description={t("cv.offer.hint")} />
            <div className="mt-4 space-y-3">
              <Textarea
                label={t("cv.offer.label")}
                value={offerText}
                onChange={(event) => setOfferText(event.target.value)}
                placeholder={t("cv.offer.placeholder")}
                rows={12}
                required
              />
              <Select
                label={t("cv.application")}
                placeholder={t("cv.applicationNone")}
                value={applicationId}
                onChange={(event) => setApplicationId(event.target.value)}
                options={applicationOptions}
              />
              {formError && (
                <p role="alert" className="text-xs font-medium text-rose-600 dark:text-rose-400">
                  {formError}
                </p>
              )}
              <div className="flex flex-wrap gap-2">
                <Button onClick={handleAnalyze} loading={analyzeMutation.isPending}>
                  {analyzeMutation.isPending ? t("cv.analyzing") : t("cv.analyze")}
                </Button>
                <Button
                  variant="secondary"
                  onClick={handleGenerate}
                  loading={generateMutation.isPending}
                  disabled={!analysis}
                >
                  {generateMutation.isPending ? t("cv.generating") : t("cv.generate")}
                </Button>
              </div>
              {analysis && (
                <Input
                  label={t("cv.titleField")}
                  value={title}
                  onChange={(event) => setTitle(event.target.value)}
                  placeholder={t("cv.titlePlaceholder")}
                />
              )}
            </div>
          </Card>

          <Card className="p-5">
            <CardHeader title={t("cv.tab.analyze")} />
            {!analysis ? (
              <div className="mt-4">
                <EmptyState title={t("cv.result.empty")} icon={<Sparkles className="h-5 w-5" />} />
                <p className="mt-4 rounded-lg border border-border bg-elevated p-3 text-xs text-muted">
                  <strong className="block text-fg">{t("cv.guardrail.title")}</strong>
                  {t("cv.guardrail.body")}
                </p>
              </div>
            ) : (
              <div className="mt-4 space-y-4">
                <div>
                  <div className="flex items-end justify-between">
                    <p className="font-display text-3xl font-semibold text-fg">
                      {t("cv.score", { score: analysis.score })}
                    </p>
                    <span className="text-xs text-muted">{t("cv.scoreLabel")}</span>
                  </div>
                  <Progress value={analysis.score} label={t("cv.scoreLabel")} className="mt-2" />
                </div>

                <SkillList title={t("cv.covered")} values={analysis.coveredSkills} variant="success" />
                <SkillList title={t("cv.missing")} values={analysis.missingSkills} variant="warning" />

                {analysis.missingInformation.length > 0 && (
                  <div>
                    <h3 className="text-sm font-semibold text-fg">{t("cv.missingInfo.title")}</h3>
                    <p className="mt-0.5 text-xs text-muted">{t("cv.missingInfo.description")}</p>
                    <ul className="mt-2 space-y-1 text-sm text-muted">
                      {analysis.missingInformation.map((item) => (
                        <li key={item}>• {item}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {analysis.experienceGap && (
                  <div>
                    <h3 className="text-sm font-semibold text-fg">{t("cv.experienceGap")}</h3>
                    <p className="mt-1 text-sm text-muted">{analysis.experienceGap}</p>
                  </div>
                )}

                {analysis.alerts.length > 0 && (
                  <div>
                    <h3 className="text-sm font-semibold text-fg">{t("cv.alerts.title")}</h3>
                    <ul className="mt-2 space-y-1 text-sm text-muted">
                      {analysis.alerts.map((item) => (
                        <li key={item}>• {item}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {analysis.suggestions.length > 0 && (
                  <div>
                    <h3 className="text-sm font-semibold text-fg">{t("cv.suggestions")}</h3>
                    <ul className="mt-2 space-y-1 text-sm text-muted">
                      {analysis.suggestions.map((item) => (
                        <li key={item}>• {item}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            )}
          </Card>
        </div>
      ) : (
        <Card className="p-5">
          <CardHeader title={t("cv.list.title")} />
          {listQuery.isLoading ? (
            <Spinner />
          ) : listQuery.isError ? (
            <EmptyState
              title={t("errors.loadFailed")}
              description={errorMessage(listQuery.error, t("errors.generic"))}
              action={
                <Button variant="outline" onClick={() => void listQuery.refetch()}>
                  {t("common.retry")}
                </Button>
              }
            />
          ) : (listQuery.data ?? []).length === 0 ? (
            <EmptyState
              title={t("cv.list.empty")}
              action={
                <Button variant="secondary" onClick={() => setTab("analyze")}>
                  {t("cv.list.emptyAction")}
                </Button>
              }
            />
          ) : (
            <div className="mt-4 divide-y divide-border">
              {(listQuery.data ?? []).map((cv) => (
                <div key={cv.id} className="flex flex-wrap items-center gap-3 py-3">
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-semibold text-fg">{cv.title}</p>
                    <p className="text-xs text-muted">
                      {t("cv.list.createdAt", { date: formatDate(cv.createdAt, language) })}
                    </p>
                  </div>
                  <Badge variant={cv.score >= 70 ? "success" : cv.score >= 40 ? "warning" : "neutral"}>
                    {t("cv.list.score")} · {t("cv.score", { score: cv.score })}
                  </Badge>
                  <div className="flex gap-1">
                    <Button variant="outline" size="sm" onClick={() => navigate(`/cv/${cv.id}`)}>
                      {t("cv.list.view")}
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => void handleExport(cv.id, "pdf")}
                      aria-label={`${t("common.download")} PDF`}
                    >
                      <Download className="h-4 w-4" aria-hidden="true" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => setDeleting(cv)}
                      aria-label={t("cv.list.delete")}
                      className="text-muted hover:text-rose-600"
                    >
                      <Trash2 className="h-4 w-4" aria-hidden="true" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      )}

      <Dialog
        open={Boolean(deleting)}
        onClose={() => setDeleting(null)}
        title={t("cv.list.delete")}
        description={t("cv.list.deleteConfirm")}
        footer={
          <>
            <Button variant="ghost" onClick={() => setDeleting(null)}>
              {t("common.cancel")}
            </Button>
            <Button variant="danger" onClick={() => void handleDelete()} loading={deleteMutation.isPending}>
              {t("common.delete")}
            </Button>
          </>
        }
      >
        <p className="text-sm text-muted">{deleting?.title}</p>
      </Dialog>
    </div>
  );
}

function SkillList({
  title,
  values,
  variant,
}: {
  title: string;
  values: string[];
  variant: "success" | "warning";
}) {
  if (values.length === 0) return null;
  return (
    <div>
      <h3 className="text-sm font-semibold text-fg">{title}</h3>
      <div className="mt-2 flex flex-wrap gap-1.5">
        {values.map((value) => (
          <Badge key={value} variant={variant}>
            {value}
          </Badge>
        ))}
      </div>
    </div>
  );
}
