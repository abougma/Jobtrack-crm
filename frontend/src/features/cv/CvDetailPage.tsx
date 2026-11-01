import { Link, useParams } from "react-router-dom";
import { ArrowLeft, Download } from "lucide-react";
import { errorMessage } from "../../lib/api";
import { useI18n, type TranslationKey } from "../../lib/i18n";
import { formatDate } from "../../lib/format";
import { downloadBlob } from "../../lib/utils";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Card, CardHeader } from "../../components/ui/Card";
import { EmptyState } from "../../components/ui/EmptyState";
import { Spinner } from "../../components/ui/Spinner";
import { useToast } from "../../components/ui/toast-context";
import { exportCv } from "./api";
import { useCvQuery } from "./hooks";
import type { CvExportFormat } from "./types";

const EXPORT_LABEL_KEYS: Record<CvExportFormat, TranslationKey> = {
  docx: "cv.export.docx",
  pdf: "cv.export.pdf",
  txt: "cv.export.txt",
};

export function CvDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { t, language } = useI18n();
  const { push } = useToast();
  const query = useCvQuery(id);

  if (query.isLoading) return <Spinner fullPage />;

  if (query.isError || !query.data) {
    return (
      <Card className="p-6">
        <EmptyState
          title={t("cv.detail.notFound")}
          description={errorMessage(query.error, t("errors.notFound"))}
          action={
            <Link to="/cv">
              <Button variant="outline" leftIcon={<ArrowLeft className="h-4 w-4" />}>
                {t("cv.detail.back")}
              </Button>
            </Link>
          }
        />
      </Card>
    );
  }

  const cv = query.data;

  const handleExport = async (format: CvExportFormat) => {
    try {
      const file = await exportCv(cv.id, format);
      downloadBlob(file.blob, file.filename);
    } catch (error) {
      push(errorMessage(error, t("cv.export.error")), "error");
    }
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <Link
            to="/cv"
            className="inline-flex items-center gap-1 text-xs font-semibold text-muted hover:text-fg"
          >
            <ArrowLeft className="h-3.5 w-3.5" aria-hidden="true" />
            {t("cv.detail.back")}
          </Link>
          <h2 className="mt-2 font-display text-xl font-semibold text-fg">{cv.title}</h2>
          <p className="mt-1 text-sm text-muted">
            {t("cv.list.createdAt", { date: formatDate(cv.createdAt, language) })}
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Badge variant={cv.score >= 70 ? "success" : cv.score >= 40 ? "warning" : "neutral"}>
            {t("cv.score", { score: cv.score })}
          </Badge>
          {(["docx", "pdf", "txt"] as CvExportFormat[]).map((format) => (
            <Button
              key={format}
              variant="outline"
              size="sm"
              onClick={() => void handleExport(format)}
              leftIcon={<Download className="h-3.5 w-3.5" />}
            >
              {t(EXPORT_LABEL_KEYS[format])}
            </Button>
          ))}
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <Card className="p-5 lg:col-span-2">
          <CardHeader title={cv.fullName ?? cv.headline ?? t("cv.detail.headline")} description={cv.summary ?? undefined} />
          {cv.location && <p className="mt-1 text-xs text-muted">{cv.location}</p>}
          <div className="mt-4 space-y-5">
            {cv.skills.length > 0 && (
              <section>
                <h3 className="text-sm font-semibold text-fg">{t("cv.detail.skills")}</h3>
                <div className="mt-2 flex flex-wrap gap-1.5">
                  {cv.skills.map((skill) => (
                    <Badge key={skill} variant="info">
                      {skill}
                    </Badge>
                  ))}
                </div>
              </section>
            )}

            {cv.experiences.length > 0 && (
              <section>
                <h3 className="text-sm font-semibold text-fg">{t("cv.detail.experiences")}</h3>
                <div className="mt-2 space-y-3">
                  {cv.experiences.map((experience, index) => (
                    <article key={`${experience.company}-${index}`} className="rounded-lg border border-border p-3">
                      <p className="text-sm font-semibold text-fg">
                        {experience.jobTitle} · {experience.company}
                      </p>
                      <p className="text-xs text-muted">
                        {formatDate(experience.startedOn, language, "MMM yyyy")} —{" "}
                        {experience.endedOn
                          ? formatDate(experience.endedOn, language, "MMM yyyy")
                          : t("common.present")}
                      </p>
                      {experience.description && (
                        <p className="mt-1 whitespace-pre-line text-sm text-muted">{experience.description}</p>
                      )}
                    </article>
                  ))}
                </div>
              </section>
            )}

            {cv.projects.length > 0 && (
              <section>
                <h3 className="text-sm font-semibold text-fg">{t("cv.detail.projects")}</h3>
                <div className="mt-2 space-y-3">
                  {cv.projects.map((project) => (
                    <article key={project.name} className="rounded-lg border border-border p-3">
                      <p className="text-sm font-semibold text-fg">{project.name}</p>
                      {project.technologies && <p className="text-xs text-muted">{project.technologies}</p>}
                      {project.description && (
                        <p className="mt-1 text-sm text-muted">{project.description}</p>
                      )}
                    </article>
                  ))}
                </div>
              </section>
            )}

            {cv.educations.length > 0 && (
              <section>
                <h3 className="text-sm font-semibold text-fg">{t("cv.detail.educations")}</h3>
                <div className="mt-2 space-y-3">
                  {cv.educations.map((education, index) => (
                    <article key={`${education.degree}-${index}`} className="rounded-lg border border-border p-3">
                      <p className="text-sm font-semibold text-fg">{education.degree}</p>
                      <p className="text-xs text-muted">
                        {education.institution} · {formatDate(education.startedOn, language, "MMM yyyy")} —{" "}
                        {education.endedOn
                          ? formatDate(education.endedOn, language, "MMM yyyy")
                          : t("common.present")}
                      </p>
                    </article>
                  ))}
                </div>
              </section>
            )}
          </div>
        </Card>

        <div className="space-y-4">
          <Card className="p-5">
            <CardHeader title={t("cv.detail.missingInfo")} />
            {cv.missingInformation.length === 0 ? (
              <p className="mt-3 text-sm text-muted">{t("cv.detail.noMissing")}</p>
            ) : (
              <ul className="mt-3 space-y-1 text-sm text-muted">
                {cv.missingInformation.map((item) => (
                  <li key={item}>• {item}</li>
                ))}
              </ul>
            )}
          </Card>

          <Card className="p-5">
            <CardHeader title={t("cv.application")} />
            {cv.applicationId ? (
              <Link
                to={`/applications/${cv.applicationId}`}
                className="mt-3 inline-block text-sm font-semibold text-primary-600 hover:underline dark:text-primary-300"
              >
                {t("common.view")}
              </Link>
            ) : (
              <p className="mt-3 text-sm text-muted">{t("cv.applicationNone")}</p>
            )}
          </Card>

          <Card className="p-5">
            <CardHeader title={t("cv.guardrail.title")} />
            <p className="mt-2 text-xs text-muted">{t("cv.guardrail.body")}</p>
          </Card>
        </div>
      </div>
    </div>
  );
}
