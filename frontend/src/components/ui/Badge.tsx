import type { HTMLAttributes } from "react";
import { cn } from "../../lib/utils";

type BadgeVariant = "neutral" | "info" | "success" | "warning" | "danger" | "accent";

export interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  variant?: BadgeVariant;
}

const variants: Record<BadgeVariant, string> = {
  neutral: "bg-slate-100 text-slate-700 dark:bg-slate-700/50 dark:text-slate-200",
  info: "bg-primary-50 text-primary-700 dark:bg-primary-900/50 dark:text-primary-100",
  success: "bg-emerald-50 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-200",
  warning: "bg-amber-50 text-amber-800 dark:bg-amber-900/40 dark:text-amber-200",
  danger: "bg-rose-50 text-rose-700 dark:bg-rose-900/40 dark:text-rose-200",
  accent: "bg-accent-100 text-accent-800 dark:bg-accent-900/40 dark:text-accent-200",
};

export function Badge({ variant = "neutral", className, children, ...props }: BadgeProps) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-semibold",
        variants[variant],
        className,
      )}
      {...props}
    >
      {children}
    </span>
  );
}
