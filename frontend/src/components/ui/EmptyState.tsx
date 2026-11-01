import type { ReactNode } from "react";
import { Inbox } from "lucide-react";
import { cn } from "../../lib/utils";

interface EmptyStateProps {
  title: string;
  description?: string;
  action?: ReactNode;
  icon?: ReactNode;
  className?: string;
}

export function EmptyState({ title, description, action, icon, className }: EmptyStateProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center gap-2 rounded-xl border border-dashed border-border bg-elevated/60 px-6 py-10 text-center",
        className,
      )}
    >
      <div className="mb-1 grid h-10 w-10 place-items-center rounded-full bg-primary-50 text-primary-600 dark:bg-primary-900/40 dark:text-primary-200">
        {icon ?? <Inbox className="h-5 w-5" aria-hidden="true" />}
      </div>
      <p className="text-sm font-semibold text-fg">{title}</p>
      {description && <p className="max-w-md text-sm text-muted">{description}</p>}
      {action && <div className="mt-2">{action}</div>}
    </div>
  );
}
