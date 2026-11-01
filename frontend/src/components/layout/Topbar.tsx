import { useLocation } from "react-router-dom";
import { Menu, UserRound } from "lucide-react";
import { Link } from "react-router-dom";
import { useAuth } from "../../features/auth/AuthProvider";
import { useI18n, type TranslationKey } from "../../lib/i18n";
import { initials } from "../../lib/utils";
import { Button } from "../ui/Button";
import { LanguageToggle } from "./LanguageToggle";
import { ThemeToggle } from "./ThemeToggle";

const titles: { prefix: string; key: TranslationKey }[] = [
  { prefix: "/applications", key: "applications.title" },
  { prefix: "/profile", key: "profile.title" },
  { prefix: "/cv", key: "cv.title" },
  { prefix: "/interview", key: "interview.title" },
  { prefix: "/organizations", key: "organizations.title" },
  { prefix: "/settings", key: "settings.title" },
  { prefix: "/", key: "dashboard.title" },
];

interface TopbarProps {
  onOpenMenu: () => void;
}

export function Topbar({ onOpenMenu }: TopbarProps) {
  const { t } = useI18n();
  const { user } = useAuth();
  const { pathname } = useLocation();
  const titleKey = titles.find((entry) => pathname.startsWith(entry.prefix))?.key ?? "dashboard.title";

  return (
    <header className="sticky top-0 z-30 border-b border-border bg-bg/85 backdrop-blur">
      <div className="mx-auto flex h-16 w-full max-w-[1320px] items-center gap-3 px-4 sm:px-6 lg:px-8">
        <Button
          variant="ghost"
          size="icon"
          className="lg:hidden"
          onClick={onOpenMenu}
          aria-label={t("nav.menu")}
        >
          <Menu className="h-5 w-5" aria-hidden="true" />
        </Button>

        <h1 className="min-w-0 flex-1 truncate font-display text-lg font-semibold text-fg sm:text-xl">
          {t(titleKey)}
        </h1>

        <div className="flex items-center gap-1">
          <LanguageToggle />
          <ThemeToggle />
          <Link
            to="/profile"
            aria-label={t("topbar.account")}
            className="ml-1 flex items-center gap-2 rounded-full border border-border bg-surface p-1 pr-3 hover:bg-elevated focus-visible:ring-2 focus-visible:ring-primary-500"
          >
            <span className="grid h-7 w-7 place-items-center rounded-full bg-accent-400 text-[0.7rem] font-bold text-slate-950">
              {user ? initials(user.firstName, user.lastName) : <UserRound className="h-3.5 w-3.5" />}
            </span>
            <span className="hidden max-w-[9rem] truncate text-xs font-semibold text-fg sm:block">
              {user ? `${user.firstName ?? ""} ${user.lastName ?? ""}`.trim() || user.email : t("topbar.account")}
            </span>
          </Link>
        </div>
      </div>
    </header>
  );
}
