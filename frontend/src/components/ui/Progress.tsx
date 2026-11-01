import { cn } from "../../lib/utils";

interface ProgressProps {
  value: number;
  max?: number;
  label?: string;
  className?: string;
}

export function Progress({ value, max = 100, label, className }: ProgressProps) {
  const safeMax = max > 0 ? max : 100;
  const percent = Math.min(100, Math.max(0, (value / safeMax) * 100));
  return (
    <div
      className={cn("h-2 w-full overflow-hidden rounded-full bg-elevated", className)}
      role="progressbar"
      aria-valuenow={Math.round(value)}
      aria-valuemin={0}
      aria-valuemax={Math.round(safeMax)}
      aria-label={label}
    >
      <div
        className="h-full rounded-full bg-gradient-to-r from-primary-500 to-primary-400 transition-[width] duration-300"
        style={{ width: `${percent}%` }}
      />
    </div>
  );
}
