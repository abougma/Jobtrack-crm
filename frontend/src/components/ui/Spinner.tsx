import { Loader2 } from "lucide-react";
import { useI18n } from "../../lib/i18n";
import { cn } from "../../lib/utils";

interface SpinnerProps {
  className?: string;
  label?: string;
  fullPage?: boolean;
}

export function Spinner({ className, label, fullPage }: SpinnerProps) {
  const { t } = useI18n();
  const content = (
    <div className="flex items-center justify-center gap-2 text-muted" role="status">
      <Loader2 className={cn("h-5 w-5 animate-spin", className)} aria-hidden="true" />
      <span className="text-sm">{label ?? t("common.loading")}</span>
      <span className="sr-only">{t("aria.loading")}</span>
    </div>
  );

  if (fullPage) {
    return <div className="flex min-h-[50vh] items-center justify-center">{content}</div>;
  }
  return content;
}
