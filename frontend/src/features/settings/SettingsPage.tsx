import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Download, Languages, Moon, Sun, Trash2 } from "lucide-react";
import { errorMessage } from "../../lib/api";
import { useI18n, type Language } from "../../lib/i18n";
import { useTheme, type ThemeMode } from "../../lib/theme";
import { formatDate, formatNumber } from "../../lib/format";
import { downloadBlob } from "../../lib/utils";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Card, CardHeader } from "../../components/ui/Card";
import { Dialog } from "../../components/ui/Dialog";
import { Input } from "../../components/ui/Input";
import { Progress } from "../../components/ui/Progress";
import { Spinner } from "../../components/ui/Spinner";
import { useToast } from "../../components/ui/toast-context";
import { useAuth } from "../auth/AuthProvider";
import { exportUserData } from "./api";
import { useDeleteAccountMutation, usePlanQuery } from "./hooks";

const THEME_OPTIONS: { value: ThemeMode; labelKey: "settings.preferences.themeLight" | "settings.preferences.themeDark" | "settings.preferences.themeSystem" }[] = [
  { value: "light", labelKey: "settings.preferences.themeLight" },
  { value: "dark", labelKey: "settings.preferences.themeDark" },
  { value: "system", labelKey: "settings.preferences.themeSystem" },
];

const LANGUAGE_OPTIONS: { value: Language; label: string }[] = [
  { value: "fr", label: "Français" },
  { value: "en", label: "English" },
];

export function SettingsPage() {
  const { t, language, setLanguage } = useI18n();
  const { mode, setMode } = useTheme();
  const { logout } = useAuth();
  const { push } = useToast();
  const navigate = useNavigate();
  const planQuery = usePlanQuery();
  const deleteMutation = useDeleteAccountMutation();
  const [exporting, setExporting] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [confirmText, setConfirmText] = useState("");

  const handleExport = async () => {
    setExporting(true);
    try {
      const data = await exportUserData();
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: "application/json" });
      downloadBlob(blob, "jobtrack-data.json");
      push(t("settings.privacy.exported"), "success");
    } catch (error) {
      push(errorMessage(error, t("settings.privacy.exportError")), "error");
    } finally {
      setExporting(false);
    }
  };

  const handleDelete = async () => {
    try {
      await deleteMutation.mutateAsync();
      await logout();
      push(t("settings.privacy.deleted"), "success");
      navigate("/login", { replace: true });
    } catch (error) {
      push(errorMessage(error, t("errors.deleteFailed")), "error");
    }
  };

  const plan = planQuery.data;
  const unlimited = plan ? plan.limit <= 0 : false;
  const percent = plan && plan.limit > 0 ? Math.min(100, (plan.used / plan.limit) * 100) : 0;

  return (
    <div className="grid gap-4 lg:grid-cols-2">
      <Card className="p-5">
        <CardHeader
          title={t("settings.plan.title")}
          action={plan ? <Badge variant="accent">{t("settings.plan.name", { plan: plan.plan })}</Badge> : undefined}
        />
        {planQuery.isLoading ? (
          <Spinner />
        ) : planQuery.isError || !plan ? (
          <p className="mt-3 text-sm text-muted">
            {errorMessage(planQuery.error, t("settings.plan.loadError"))}
          </p>
        ) : (
          <div className="mt-4 space-y-3">
            <div>
              <div className="flex items-center justify-between text-sm">
                <span className="font-semibold text-fg">
                  {unlimited
                    ? t("settings.plan.unlimited")
                    : t("settings.plan.usage", { used: plan.used, limit: plan.limit })}
                </span>
                {!unlimited && <span className="text-xs text-muted">{formatNumber(plan.used, language)}</span>}
              </div>
              {!unlimited && <Progress value={percent} label={t("settings.plan.title")} className="mt-2" />}
            </div>
            <p className="text-xs text-muted">
              {t("settings.plan.reset", { date: formatDate(plan.resetAt, language) })}
            </p>
            {plan.features.length > 0 && (
              <div>
                <p className="text-xs font-semibold text-fg">{t("settings.plan.features")}</p>
                <ul className="mt-1 space-y-1 text-xs text-muted">
                  {plan.features.map((feature) => (
                    <li key={feature}>• {feature}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        )}
      </Card>

      <Card className="p-5">
        <CardHeader title={t("settings.preferences.title")} />
        <div className="mt-4 space-y-4">
          <div>
            <p className="text-xs font-semibold text-fg">{t("settings.preferences.theme")}</p>
            <div className="mt-2 flex flex-wrap gap-2">
              {THEME_OPTIONS.map((option) => (
                <Button
                  key={option.value}
                  variant={mode === option.value ? "primary" : "outline"}
                  size="sm"
                  onClick={() => setMode(option.value)}
                  leftIcon={
                    option.value === "dark" ? (
                      <Moon className="h-3.5 w-3.5" />
                    ) : option.value === "light" ? (
                      <Sun className="h-3.5 w-3.5" />
                    ) : undefined
                  }
                >
                  {t(option.labelKey)}
                </Button>
              ))}
            </div>
          </div>
          <div>
            <p className="text-xs font-semibold text-fg">{t("settings.preferences.language")}</p>
            <div className="mt-2 flex flex-wrap gap-2">
              {LANGUAGE_OPTIONS.map((option) => (
                <Button
                  key={option.value}
                  variant={language === option.value ? "primary" : "outline"}
                  size="sm"
                  onClick={() => setLanguage(option.value)}
                  leftIcon={<Languages className="h-3.5 w-3.5" />}
                >
                  {option.label}
                </Button>
              ))}
            </div>
          </div>
        </div>
      </Card>

      <Card className="p-5">
        <CardHeader title={t("settings.privacy.title")} description={t("settings.privacy.description")} />
        <div className="mt-4">
          <Button
            variant="outline"
            onClick={() => void handleExport()}
            loading={exporting}
            leftIcon={<Download className="h-4 w-4" />}
          >
            {exporting ? t("settings.privacy.exporting") : t("settings.privacy.export")}
          </Button>
        </div>
      </Card>

      <Card className="border-rose-200 p-5 dark:border-rose-900/60">
        <CardHeader title={t("settings.danger.title")} description={t("settings.privacy.deleteDescription")} />
        <div className="mt-4">
          <Button
            variant="danger"
            onClick={() => {
              setConfirmText("");
              setDeleteOpen(true);
            }}
            leftIcon={<Trash2 className="h-4 w-4" />}
          >
            {t("settings.privacy.delete")}
          </Button>
        </div>
      </Card>

      <Dialog
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
        title={t("settings.privacy.deleteTitle")}
        description={t("settings.privacy.deleteDescription")}
        footer={
          <>
            <Button variant="ghost" onClick={() => setDeleteOpen(false)}>
              {t("common.cancel")}
            </Button>
            <Button
              variant="danger"
              onClick={() => void handleDelete()}
              loading={deleteMutation.isPending}
              disabled={confirmText.trim() !== "SUPPRIMER"}
            >
              {t("settings.privacy.deleteButton")}
            </Button>
          </>
        }
      >
        <Input
          label={t("settings.privacy.deleteConfirmLabel")}
          hint={t("settings.privacy.deleteConfirmHint")}
          value={confirmText}
          onChange={(event) => setConfirmText(event.target.value)}
          autoFocus
        />
      </Dialog>
    </div>
  );
}
