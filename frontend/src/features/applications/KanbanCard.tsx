import { Link } from "react-router-dom";
import { useDraggable } from "@dnd-kit/core";
import { CSS } from "@dnd-kit/utilities";
import { CalendarDays, GripVertical, MapPin } from "lucide-react";
import { useI18n } from "../../lib/i18n";
import { formatDate } from "../../lib/format";
import { cn } from "../../lib/utils";
import type { ApplicationSummary } from "./types";

interface KanbanCardProps {
  application: ApplicationSummary;
  dragging?: boolean;
}

function CardContent({ application }: { application: ApplicationSummary }) {
  const { language } = useI18n();
  return (
    <>
      <div className="flex items-start gap-2">
        <span className="mt-0.5 rounded p-0.5 text-muted opacity-60" aria-hidden="true">
          <GripVertical className="h-4 w-4" />
        </span>
        <div className="min-w-0 flex-1">
          <Link
            to={`/applications/${application.id}`}
            className="block truncate text-sm font-semibold text-fg hover:text-primary-600 hover:underline dark:hover:text-primary-300"
          >
            {application.position}
          </Link>
          <p className="truncate text-xs text-muted">{application.company}</p>
        </div>
      </div>

      <div className="mt-2 flex flex-wrap items-center gap-2 pl-6 text-[0.7rem] text-muted">
        {application.location && (
          <span className="inline-flex items-center gap-1">
            <MapPin className="h-3 w-3" aria-hidden="true" />
            {application.location}
          </span>
        )}
        {application.appliedAt && (
          <span className="inline-flex items-center gap-1">
            <CalendarDays className="h-3 w-3" aria-hidden="true" />
            {formatDate(application.appliedAt, language)}
          </span>
        )}
        {application.source && (
          <span className="rounded-full bg-elevated px-1.5 py-0.5 font-medium">{application.source}</span>
        )}
      </div>
    </>
  );
}

export function KanbanCard({ application, dragging }: KanbanCardProps) {
  const { t, language } = useI18n();
  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({
    id: application.id,
    data: { status: application.status },
  });

  return (
    <article
      ref={setNodeRef}
      style={{ transform: CSS.Translate.toString(transform) }}
      className={cn(
        "rounded-xl border border-border bg-surface p-3 shadow-sm transition-shadow focus-within:ring-2 focus-within:ring-primary-500",
        (isDragging || dragging) && "opacity-50",
      )}
    >
      <div className="flex items-start gap-2">
        <button
          type="button"
          {...attributes}
          {...listeners}
          aria-label={`${t("applications.kanban.hint")} — ${application.position}`}
          className="mt-0.5 cursor-grab touch-none rounded p-0.5 text-muted opacity-60 hover:text-fg focus-visible:ring-2 focus-visible:ring-primary-500 active:cursor-grabbing"
        >
          <GripVertical className="h-4 w-4" aria-hidden="true" />
        </button>
        <div className="min-w-0 flex-1">
          <Link
            to={`/applications/${application.id}`}
            className="block truncate text-sm font-semibold text-fg hover:text-primary-600 hover:underline dark:hover:text-primary-300"
          >
            {application.position}
          </Link>
          <p className="truncate text-xs text-muted">{application.company}</p>
          <div className="mt-2 flex flex-wrap items-center gap-2 text-[0.7rem] text-muted">
            {application.location && (
              <span className="inline-flex items-center gap-1">
                <MapPin className="h-3 w-3" aria-hidden="true" />
                {application.location}
              </span>
            )}
            {application.appliedAt && (
              <span className="inline-flex items-center gap-1">
                <CalendarDays className="h-3 w-3" aria-hidden="true" />
                {formatDate(application.appliedAt, language, "d MMM")}
              </span>
            )}
            {application.source && (
              <span className="rounded-full bg-elevated px-1.5 py-0.5 font-medium">
                {application.source}
              </span>
            )}
          </div>
        </div>
      </div>
    </article>
  );
}

export function KanbanCardPreview({ application }: { application: ApplicationSummary }) {
  return (
    <article className="w-[260px] rotate-2 rounded-xl border border-border bg-surface p-3 shadow-lg">
      <CardContent application={application} />
    </article>
  );
}
