import { ArrowRight, BarChart3, FileCheck2, Sparkles } from "lucide-react";
import { Link } from "react-router-dom";
import { LanguageToggle } from "../../components/layout/LanguageToggle";
import { ThemeToggle } from "../../components/layout/ThemeToggle";
import { useI18n } from "../../lib/i18n";
import { useAuth } from "../auth/AuthProvider";

export function LandingPage() {
  const { t } = useI18n();
  const { isAuthenticated, user } = useAuth();
  const features = [
    { icon: BarChart3, title: t("landing.feature1Title"), text: t("landing.feature1Text") },
    { icon: FileCheck2, title: t("landing.feature2Title"), text: t("landing.feature2Text") },
    { icon: Sparkles, title: t("landing.feature3Title"), text: t("landing.feature3Text") },
  ];

  return (
    <main className="min-h-screen overflow-hidden bg-bg">
      <header className="mx-auto flex max-w-7xl items-center justify-between px-5 py-5 sm:px-8">
        <Link to="/" className="flex items-center gap-2 font-display text-xl font-bold tracking-tight text-fg">
          <span className="grid h-9 w-9 place-items-center rounded-xl bg-accent-400 text-slate-950">J</span>
          JobTrack
        </Link>
        <div className="flex items-center gap-2">
          <LanguageToggle />
          <ThemeToggle />
          {isAuthenticated ? (
            <Link to="/dashboard" className="rounded-lg bg-primary-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-primary-700">
              {t("landing.openSpace")}
            </Link>
          ) : (
            <>
              <Link to="/login" className="hidden px-3 py-2 text-sm font-semibold text-muted hover:text-fg sm:block">
                {t("landing.login")}
              </Link>
              <Link to="/register" className="rounded-lg bg-primary-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-primary-700">
                {t("landing.start")}
              </Link>
            </>
          )}
        </div>
      </header>

      <section className="relative mx-auto grid max-w-7xl gap-12 px-5 pb-20 pt-16 sm:px-8 lg:grid-cols-[1.05fr_.95fr] lg:items-center lg:pb-28 lg:pt-24">
        <div className="relative z-10">
          <p className="text-xs font-bold uppercase tracking-[0.2em] text-primary-600">{t("landing.eyebrow")}</p>
          <h1 className="mt-5 max-w-2xl font-display text-4xl font-semibold leading-tight tracking-tight text-fg sm:text-6xl">
            {t("landing.title")}
          </h1>
          <p className="mt-6 max-w-xl text-base leading-relaxed text-muted sm:text-lg">{t("landing.description")}</p>
          <div className="mt-8 flex flex-wrap items-center gap-3">
            {isAuthenticated ? (
              <Link to="/dashboard" className="inline-flex items-center gap-2 rounded-lg bg-primary-600 px-5 py-3 text-sm font-semibold text-white shadow-sm hover:bg-primary-700">
                {t("landing.openSpace")} <ArrowRight className="h-4 w-4" aria-hidden="true" />
              </Link>
            ) : (
              <>
                <Link to="/register" className="inline-flex items-center gap-2 rounded-lg bg-primary-600 px-5 py-3 text-sm font-semibold text-white shadow-sm hover:bg-primary-700">
                  {t("landing.cta")} <ArrowRight className="h-4 w-4" aria-hidden="true" />
                </Link>
                <Link to="/login" className="rounded-lg border border-border bg-surface px-5 py-3 text-sm font-semibold text-fg hover:bg-elevated">
                  {t("landing.login")}
                </Link>
              </>
            )}
          </div>
          {isAuthenticated && user && <p className="mt-4 text-sm font-medium text-primary-700 dark:text-primary-200">{t("landing.welcomeBack", { name: user.firstName || user.email })}</p>}
          <p className="mt-5 text-xs text-muted">{t("landing.note")}</p>
        </div>
        <div className="relative rounded-3xl bg-slate-950 p-5 shadow-2xl shadow-primary-900/20 sm:p-8">
          <div className="absolute -right-12 -top-12 h-40 w-40 rounded-full bg-primary-500/30 blur-3xl" aria-hidden="true" />
          <div className="relative rounded-2xl border border-white/10 bg-white/5 p-5 text-white">
            <div className="flex items-center justify-between">
              <span className="text-sm font-semibold">{t("landing.previewTitle")}</span>
              <span className="rounded-full bg-accent-400/15 px-2 py-1 text-xs text-accent-300">{t("landing.previewBadge")}</span>
            </div>
            <div className="mt-7 grid grid-cols-2 gap-3">
              {[["24", t("landing.previewApplications")], ["78%", t("landing.previewMatch")], ["6", t("landing.previewInterviews")], ["3", t("landing.previewActions")]].map(([value, label]) => (
                <div key={label} className="rounded-xl bg-white/10 p-4">
                  <strong className="font-display text-2xl">{value}</strong>
                  <span className="mt-1 block text-xs text-slate-300">{label}</span>
                </div>
              ))}
            </div>
            <div className="mt-4 rounded-xl bg-white/10 p-4">
              <div className="flex items-center justify-between text-xs text-slate-300"><span>{t("landing.previewProgress")}</span><span>72%</span></div>
              <div className="mt-3 h-2 rounded-full bg-white/10"><div className="h-full w-[72%] rounded-full bg-accent-400" /></div>
            </div>
          </div>
        </div>
      </section>

      <section className="mx-auto grid max-w-7xl gap-4 px-5 pb-20 sm:px-8 md:grid-cols-3">
        {features.map(({ icon: Icon, title, text }) => (
          <article key={title} className="rounded-2xl border border-border bg-surface p-6">
            <span className="grid h-10 w-10 place-items-center rounded-xl bg-primary-50 text-primary-600 dark:bg-primary-900/40 dark:text-primary-200"><Icon className="h-5 w-5" aria-hidden="true" /></span>
            <h2 className="mt-5 font-display text-lg font-semibold text-fg">{title}</h2>
            <p className="mt-2 text-sm leading-relaxed text-muted">{text}</p>
          </article>
        ))}
      </section>
    </main>
  );
}
