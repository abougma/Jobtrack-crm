import type { ReactNode } from "react";
import { BarChart3, FileCheck2, Sparkles } from "lucide-react";
import { useI18n } from "../../lib/i18n";
import { LanguageToggle } from "../../components/layout/LanguageToggle";
import { ThemeToggle } from "../../components/layout/ThemeToggle";

interface AuthLayoutProps {
  title: string;
  subtitle: string;
  children: ReactNode;
  footer?: ReactNode;
}

export function AuthLayout({ title, subtitle, children, footer }: AuthLayoutProps) {
  const { t } = useI18n();
  const features = [
    { icon: BarChart3, label: t("auth.feature1") },
    { icon: FileCheck2, label: t("auth.feature2") },
    { icon: Sparkles, label: t("auth.feature3") },
  ];

  return (
    <div className="min-h-screen bg-bg lg:grid lg:grid-cols-[1.05fr_1fr]">
      <aside className="relative hidden overflow-hidden bg-slate-950 px-12 py-12 text-white lg:flex lg:flex-col">
        <div
          className="pointer-events-none absolute -right-24 -top-24 h-72 w-72 rounded-full bg-primary-600/30 blur-3xl"
          aria-hidden="true"
        />
        <div
          className="pointer-events-none absolute -bottom-24 -left-16 h-80 w-80 rounded-full bg-accent-500/20 blur-3xl"
          aria-hidden="true"
        />
        <div className="relative flex items-center gap-2">
          <span className="grid h-9 w-9 place-items-center rounded-xl bg-accent-400 font-display text-lg font-bold text-slate-950">
            J
          </span>
          <span className="font-display text-xl font-bold tracking-tight">JobTrack</span>
        </div>
        <div className="relative mt-auto max-w-md">
          <p className="text-xs font-bold uppercase tracking-[0.2em] text-accent-300">
            {t("auth.brandEyebrow")}
          </p>
          <h1 className="mt-4 font-display text-4xl font-semibold leading-tight">
            {t("app.tagline")}
          </h1>
          <p className="mt-4 text-sm leading-relaxed text-slate-300">{t("app.description")}</p>
          <ul className="mt-8 space-y-3">
            {features.map(({ icon: Icon, label }) => (
              <li key={label} className="flex items-center gap-3 text-sm text-slate-200">
                <span className="grid h-8 w-8 place-items-center rounded-lg bg-white/10">
                  <Icon className="h-4 w-4 text-accent-300" aria-hidden="true" />
                </span>
                {label}
              </li>
            ))}
          </ul>
        </div>
        <p className="relative mt-10 text-xs text-slate-500">© {new Date().getFullYear()} JobTrack</p>
      </aside>

      <main className="flex min-h-screen flex-col px-5 py-6 sm:px-10 lg:px-14">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 lg:invisible">
            <span className="grid h-8 w-8 place-items-center rounded-lg bg-accent-400 font-display text-sm font-bold text-slate-950">
              J
            </span>
            <span className="font-display text-lg font-bold">JobTrack</span>
          </div>
          <div className="flex items-center gap-1">
            <LanguageToggle />
            <ThemeToggle />
          </div>
        </div>

        <div className="mx-auto flex w-full max-w-md flex-1 flex-col justify-center py-10">
          <h2 className="font-display text-2xl font-semibold tracking-tight text-fg sm:text-3xl">
            {title}
          </h2>
          <p className="mt-2 text-sm text-muted">{subtitle}</p>
          <div className="mt-8">{children}</div>
          {footer && <div className="mt-6 text-sm text-muted">{footer}</div>}
        </div>
      </main>
    </div>
  );
}
