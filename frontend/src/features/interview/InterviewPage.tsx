import { useState, type FormEvent } from "react";
import { MessageCircleQuestion, Sparkles } from "lucide-react";
import { errorMessage } from "../../lib/api";
import { useI18n } from "../../lib/i18n";
import { Button } from "../../components/ui/Button";
import { Card, CardHeader } from "../../components/ui/Card";
import { EmptyState } from "../../components/ui/EmptyState";
import { Select } from "../../components/ui/Select";
import { Textarea } from "../../components/ui/Textarea";
import { DEFAULT_FILTERS } from "../applications/filters";
import { useApplicationsQuery } from "../applications/hooks";
import { usePrepareInterviewMutation } from "./hooks";
import type { InterviewPreparation, InterviewQuestion } from "./types";

export function InterviewPage() {
  const { t } = useI18n();
  const [offerText, setOfferText] = useState("");
  const [applicationId, setApplicationId] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [preparation, setPreparation] = useState<InterviewPreparation | null>(null);

  const applicationsQuery = useApplicationsQuery({ ...DEFAULT_FILTERS, size: 200 }, true);
  const mutation = usePrepareInterviewMutation();

  const applicationOptions = (applicationsQuery.data?.content ?? []).map((application) => ({
    value: application.id,
    label: `${application.company} — ${application.position}`,
  }));

  const handlePrepare = (event: FormEvent) => {
    event.preventDefault();
    if (offerText.trim().length < 40) {
      setError(t("validation.offerMin"));
      return;
    }
    setError(null);
    mutation.mutate(
      { offerText, applicationId: applicationId || null },
      {
        onSuccess: setPreparation,
        onError: (reason) => setError(errorMessage(reason, t("errors.generic"))),
      },
    );
  };

  return (
    <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(0,1.15fr)]">
      <Card className="p-5">
        <CardHeader title={t("interview.subtitle")} description={t("interview.guardrail")} />
        <form onSubmit={handlePrepare} className="mt-4 space-y-3">
          <Textarea
            label={t("interview.offer.label")}
            value={offerText}
            onChange={(event) => setOfferText(event.target.value)}
            placeholder={t("interview.offer.placeholder")}
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
          {error && (
            <p role="alert" className="text-xs font-medium text-rose-600 dark:text-rose-400">
              {error}
            </p>
          )}
          <Button type="submit" loading={mutation.isPending} leftIcon={<Sparkles className="h-4 w-4" />}>
            {mutation.isPending ? t("interview.preparing") : t("interview.prepare")}
          </Button>
        </form>
      </Card>

      <Card className="p-5">
        <CardHeader title={t("interview.title")} />
        {!preparation ? (
          <div className="mt-4">
            <EmptyState
              title={t("interview.empty")}
              icon={<MessageCircleQuestion className="h-5 w-5" aria-hidden="true" />}
            />
          </div>
        ) : (
          <div className="mt-4 space-y-5">
            <QuestionGroup
              title={t("interview.technical")}
              questions={preparation.technicalQuestions}
              emptyLabel={t("interview.noTechnical")}
            />
            <QuestionGroup
              title={t("interview.behavioral")}
              questions={preparation.behavioralQuestions}
              emptyLabel={t("interview.noBehavioral")}
            />

            {preparation.starPrompts.length > 0 && (
              <section>
                <h3 className="text-sm font-semibold text-fg">{t("interview.star")}</h3>
                <ol className="mt-2 space-y-1 text-sm text-muted">
                  {preparation.starPrompts.map((prompt, index) => (
                    <li key={prompt} className="rounded-lg border border-border px-3 py-2">
                      <span className="mr-2 font-semibold text-primary-600 dark:text-primary-300">
                        {index + 1}.
                      </span>
                      {prompt}
                    </li>
                  ))}
                </ol>
              </section>
            )}

            {preparation.recruiterQuestions.length > 0 && (
              <section>
                <h3 className="text-sm font-semibold text-fg">{t("interview.recruiter")}</h3>
                <ul className="mt-2 space-y-1 text-sm text-muted">
                  {preparation.recruiterQuestions.map((question) => (
                    <li key={question}>• {question}</li>
                  ))}
                </ul>
              </section>
            )}

            <section>
              <h3 className="text-sm font-semibold text-fg">{t("interview.missing")}</h3>
              {preparation.missingEvidence.length === 0 ? (
                <p className="mt-1 text-sm text-muted">{t("interview.noMissing")}</p>
              ) : (
                <ul className="mt-2 space-y-1 text-sm text-muted">
                  {preparation.missingEvidence.map((item) => (
                    <li key={item}>• {item}</li>
                  ))}
                </ul>
              )}
            </section>
          </div>
        )}
      </Card>
    </div>
  );
}

function QuestionGroup({
  title,
  questions,
  emptyLabel,
}: {
  title: string;
  questions: InterviewQuestion[];
  emptyLabel: string;
}) {
  const { t } = useI18n();

  return (
    <section>
      <h3 className="text-sm font-semibold text-fg">{title}</h3>
      {questions.length === 0 ? (
        <p className="mt-1 text-sm text-muted">{emptyLabel}</p>
      ) : (
        <div className="mt-2 space-y-2">
          {questions.map((question) => (
            <article key={question.question} className="rounded-lg border border-border p-3">
              <p className="text-sm font-semibold text-fg">{question.question}</p>
              <p className="mt-1 text-xs text-muted">
                <span className="font-semibold">{t("interview.reason")} : </span>
                {question.reason}
              </p>
              {question.profileEvidence.length > 0 && (
                <p className="mt-1 text-xs text-muted">
                  <span className="font-semibold">{t("interview.evidence")} : </span>
                  {question.profileEvidence.join(" · ")}
                </p>
              )}
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
