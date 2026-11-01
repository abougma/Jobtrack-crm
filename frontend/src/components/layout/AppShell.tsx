import { useEffect, useState } from "react";
import { Outlet, useLocation } from "react-router-dom";
import { useI18n } from "../../lib/i18n";
import { Sidebar } from "./Sidebar";
import { Topbar } from "./Topbar";

export function AppShell() {
  const { t } = useI18n();
  const [menuOpen, setMenuOpen] = useState(false);
  const { pathname } = useLocation();

  useEffect(() => {
    setMenuOpen(false);
  }, [pathname]);

  useEffect(() => {
    if (!menuOpen) return;
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") setMenuOpen(false);
    };
    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, [menuOpen]);

  return (
    <div className="min-h-screen bg-bg">
      <a href="#main-content" className="skip-link">
        {t("aria.skipToContent")}
      </a>

      <div className="hidden lg:fixed lg:inset-y-0 lg:left-0 lg:z-40 lg:block">
        <Sidebar />
      </div>

      {menuOpen && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <div
            className="absolute inset-0 bg-slate-950/50 backdrop-blur-sm"
            aria-hidden="true"
            onClick={() => setMenuOpen(false)}
          />
          <div className="relative h-full w-[280px] max-w-[85vw] bg-surface shadow-xl animate-fade-in">
            <Sidebar onNavigate={() => setMenuOpen(false)} />
          </div>
        </div>
      )}

      <div className="lg:pl-[260px]">
        <Topbar onOpenMenu={() => setMenuOpen(true)} />
        <main id="main-content" className="mx-auto w-full max-w-[1320px] px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
