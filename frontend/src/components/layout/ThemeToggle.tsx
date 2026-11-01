import { Moon, Sun } from "lucide-react";
import { useI18n } from "../../lib/i18n";
import { useTheme } from "../../lib/theme";
import { Button } from "../ui/Button";

export function ThemeToggle() {
  const { t } = useI18n();
  const { theme, toggle } = useTheme();
  return (
    <Button
      variant="ghost"
      size="icon"
      onClick={toggle}
      aria-label={t("topbar.theme")}
      title={theme === "dark" ? t("topbar.light") : t("topbar.dark")}
    >
      {theme === "dark" ? (
        <Sun className="h-4 w-4" aria-hidden="true" />
      ) : (
        <Moon className="h-4 w-4" aria-hidden="true" />
      )}
    </Button>
  );
}
