import { NavLink } from "react-router-dom";
import {
  BarChart3,
  BriefcaseBusiness,
  Building2,
  FileText,
  LogOut,
  Settings,
  ShieldCheck,
  Sparkles,
  UserRound,
} from "lucide-react";
import { useAuth } from "../../features/auth/AuthProvider";
import { useI18n } from "../../lib/i18n";
import { cn, initials } from "../../lib/utils";
import { Button } from "../ui/Button";

const navItems = [
  { to: "/dashboard", labelKey: "nav.dashboard", icon: BarChart3, end: true },
  { to: "/applications", labelKey: "nav.applications", icon: BriefcaseBusiness },
  { to: "/profile", labelKey: "nav.profile", icon: UserRound },
  { to: "/cv", labelKey: "nav.cv", icon: FileText },
  { to: "/interview", labelKey: "nav.interview", icon: Sparkles },
  { to: "/organizations", labelKey: "nav.organizations", icon: Building2 },
  { to: "/settings", labelKey: "nav.settings", icon: Settings },
] as const;

const adminNavItem = { to: "/admin", labelKey: "nav.admin", icon: ShieldCheck } as const;

interface SidebarProps {
  onNavigate?: () => void;
}

export function Sidebar({ onNavigate }: SidebarProps) {
  const { t } = useI18n();
  const { user, logout } = useAuth();
  const items = user?.role === "PLATFORM_ADMIN" ? [...navItems, adminNavItem] : navItems;

  return (
    <aside className="flex h-full w-[260px] flex-col border-r border-border bg-surface px-3 py-5">
      <div className="flex items-center gap-2 px-2">
        <span className="grid h-9 w-9 place-items-center rounded-xl bg-accent-400 font-display text-lg font-bold text-slate-950">
          J
        </span>
        <span className="font-display text-lg font-bold tracking-tight text-fg">JobTrack</span>
      </div>

      <p className="mt-8 px-3 text-[0.65rem] font-bold uppercase tracking-[0.16em] text-muted">
        {t("nav.personal")}
      </p>
      <nav aria-label={t("nav.main")} className="mt-2 flex flex-1 flex-col gap-1">
        {items.map(({ to, labelKey, icon: Icon, ...rest }) => (
          <NavLink
            key={to}
            to={to}
            end={"end" in rest ? rest.end : undefined}
            onClick={onNavigate}
            className={({ isActive }) =>
              cn(
                "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors focus-visible:ring-2 focus-visible:ring-primary-500",
                isActive
                  ? "bg-primary-50 text-primary-700 dark:bg-primary-900/40 dark:text-primary-100"
                  : "text-muted hover:bg-elevated hover:text-fg",
              )
            }
          >
            {({ isActive }) => (
              <>
                <Icon
                  className={cn("h-4 w-4", isActive ? "text-primary-600 dark:text-primary-200" : "")}
                  aria-hidden="true"
                />
                {t(labelKey)}
              </>
            )}
          </NavLink>
        ))}
      </nav>

      <div className="mt-4 rounded-xl border border-border bg-elevated p-3">
        <p className="text-[0.65rem] font-bold uppercase tracking-[0.14em] text-muted">
          {t("nav.plan", { plan: user?.plan ?? "FREE" })}
        </p>
        <div className="mt-3 flex items-center gap-2">
          <span className="grid h-8 w-8 place-items-center rounded-full bg-accent-400 text-xs font-bold text-slate-950">
            {initials(user?.firstName, user?.lastName)}
          </span>
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-semibold text-fg">
              {user ? `${user.firstName ?? ""} ${user.lastName ?? ""}`.trim() || user.email : "—"}
            </p>
            <p className="truncate text-xs text-muted">{user?.email}</p>
          </div>
        </div>
        <Button
          variant="ghost"
          size="sm"
          fullWidth
          className="mt-2 justify-start px-2 text-muted"
          onClick={() => void logout()}
          leftIcon={<LogOut className="h-4 w-4" aria-hidden="true" />}
        >
          {t("nav.logout")}
        </Button>
      </div>
    </aside>
  );
}
