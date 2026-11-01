import { useI18n } from "../../lib/i18n";
import { Button } from "../ui/Button";

export function LanguageToggle() {
  const { language, setLanguage, t } = useI18n();
  const next = language === "fr" ? "en" : "fr";
  return (
    <Button
      variant="ghost"
      size="sm"
      onClick={() => setLanguage(next)}
      aria-label={`${t("topbar.language")} (${next.toUpperCase()})`}
      title={t("topbar.language")}
      className="font-display uppercase tracking-wide"
    >
      {language}
    </Button>
  );
}
