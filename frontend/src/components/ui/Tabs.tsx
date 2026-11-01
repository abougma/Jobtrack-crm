import type { KeyboardEvent, ReactNode } from "react";
import { cn } from "../../lib/utils";

export interface TabItem {
  value: string;
  label: string;
  icon?: ReactNode;
}

interface TabsProps {
  items: TabItem[];
  value: string;
  onChange: (value: string) => void;
  ariaLabel?: string;
  className?: string;
}

export function Tabs({ items, value, onChange, ariaLabel, className }: TabsProps) {
  const handleKeyDown = (event: KeyboardEvent<HTMLButtonElement>, index: number) => {
    if (!["ArrowRight", "ArrowLeft", "Home", "End"].includes(event.key)) return;
    event.preventDefault();
    const last = items.length - 1;
    const nextIndex =
      event.key === "ArrowRight"
        ? index === last
          ? 0
          : index + 1
        : event.key === "ArrowLeft"
          ? index === 0
            ? last
            : index - 1
          : event.key === "Home"
            ? 0
            : last;
    const next = items[nextIndex];
    onChange(next.value);
    const target = event.currentTarget.parentElement?.querySelectorAll<HTMLButtonElement>(
      "[role='tab']",
    )[nextIndex];
    target?.focus();
  };

  return (
    <div
      role="tablist"
      aria-label={ariaLabel}
      className={cn(
        "inline-flex flex-wrap gap-1 rounded-xl border border-border bg-elevated p-1",
        className,
      )}
    >
      {items.map((item, index) => {
        const selected = item.value === value;
        return (
          <button
            key={item.value}
            type="button"
            role="tab"
            aria-selected={selected}
            tabIndex={selected ? 0 : -1}
            onClick={() => onChange(item.value)}
            onKeyDown={(event) => handleKeyDown(event, index)}
            className={cn(
              "inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-semibold transition-colors focus-visible:ring-2 focus-visible:ring-primary-500",
              selected
                ? "bg-surface text-primary-700 shadow-sm dark:text-primary-100"
                : "text-muted hover:text-fg",
            )}
          >
            {item.icon}
            {item.label}
          </button>
        );
      })}
    </div>
  );
}
