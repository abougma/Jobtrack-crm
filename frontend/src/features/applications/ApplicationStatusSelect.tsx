import { APPLICATION_STATUSES, STATUS_LABEL_KEYS, type ApplicationStatus } from "../../lib/domain";
import { useI18n } from "../../lib/i18n";
import { cn } from "../../lib/utils";

interface ApplicationStatusSelectProps {
  value: ApplicationStatus;
  onChange: (status: ApplicationStatus) => void;
  disabled?: boolean;
  label?: string;
  className?: string;
}

export function ApplicationStatusSelect({
  value,
  onChange,
  disabled,
  label,
  className,
}: ApplicationStatusSelectProps) {
  const { t } = useI18n();
  return (
    <select
      value={value}
      disabled={disabled}
      aria-label={label ?? t("applications.column.status")}
      onChange={(event) => onChange(event.target.value as ApplicationStatus)}
      className={cn(
        "rounded-lg border border-border bg-elevated px-2 py-1.5 text-xs font-semibold text-fg focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500/25 disabled:opacity-60",
        className,
      )}
    >
      {APPLICATION_STATUSES.map((status) => (
        <option key={status} value={status}>
          {t(STATUS_LABEL_KEYS[status])}
        </option>
      ))}
    </select>
  );
}
