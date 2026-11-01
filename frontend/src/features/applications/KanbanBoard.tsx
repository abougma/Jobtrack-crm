import { useMemo, useState } from "react";
import {
  DndContext,
  DragOverlay,
  KeyboardSensor,
  PointerSensor,
  useDroppable,
  useSensor,
  useSensors,
  type DragEndEvent,
  type DragStartEvent,
} from "@dnd-kit/core";
import { sortableKeyboardCoordinates } from "@dnd-kit/sortable";
import { useI18n } from "../../lib/i18n";
import {
  APPLICATION_STATUSES,
  STATUS_LABEL_KEYS,
  type ApplicationStatus,
} from "../../lib/domain";
import { cn } from "../../lib/utils";
import { KanbanCard, KanbanCardPreview } from "./KanbanCard";
import type { ApplicationSummary } from "./types";

interface KanbanBoardProps {
  applications: ApplicationSummary[];
  onStatusChange: (id: string, status: ApplicationStatus) => void;
  updatingId?: string;
}

function KanbanColumn({
  status,
  items,
  updatingId,
}: {
  status: ApplicationStatus;
  items: ApplicationSummary[];
  updatingId?: string;
}) {
  const { t } = useI18n();
  const { setNodeRef, isOver } = useDroppable({ id: `column-${status}` });

  return (
    <section
      aria-label={t(STATUS_LABEL_KEYS[status])}
      className="flex w-[272px] shrink-0 flex-col rounded-xl border border-border bg-elevated/70"
    >
      <header className="flex items-center justify-between gap-2 px-3 py-2.5">
        <h3 className="text-xs font-bold uppercase tracking-wide text-muted">
          {t(STATUS_LABEL_KEYS[status])}
        </h3>
        <span className="rounded-full bg-surface px-2 py-0.5 text-[0.65rem] font-semibold text-muted">
          {items.length}
        </span>
      </header>
      <div
        ref={setNodeRef}
        className={cn(
          "flex min-h-[120px] flex-1 flex-col gap-2 rounded-b-xl p-2 transition-colors",
          isOver && "bg-primary-50/70 dark:bg-primary-900/20",
        )}
      >
        {items.length === 0 ? (
          <p className="rounded-lg border border-dashed border-border px-3 py-6 text-center text-xs text-muted">
            {t("applications.kanban.empty")}
          </p>
        ) : (
          items.map((application) => (
            <KanbanCard
              key={application.id}
              application={application}
              dragging={updatingId === application.id}
            />
          ))
        )}
      </div>
    </section>
  );
}

export function KanbanBoard({ applications, onStatusChange, updatingId }: KanbanBoardProps) {
  const { t } = useI18n();
  const [activeId, setActiveId] = useState<string | null>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 6 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  );

  const grouped = useMemo(() => {
    const map = new Map<ApplicationStatus, ApplicationSummary[]>();
    APPLICATION_STATUSES.forEach((status) => map.set(status, []));
    applications.forEach((application) => {
      map.get(application.status)?.push(application);
    });
    return map;
  }, [applications]);

  const activeApplication = applications.find((application) => application.id === activeId) ?? null;

  const handleDragStart = (event: DragStartEvent) => {
    setActiveId(String(event.active.id));
  };

  const handleDragEnd = (event: DragEndEvent) => {
    setActiveId(null);
    const overId = event.over?.id ? String(event.over.id) : null;
    if (!overId?.startsWith("column-")) return;
    const nextStatus = overId.replace("column-", "") as ApplicationStatus;
    const application = applications.find((item) => item.id === event.active.id);
    if (!application || application.status === nextStatus) return;
    onStatusChange(application.id, nextStatus);
  };

  return (
    <div>
      <p className="mb-2 text-xs text-muted">{t("applications.kanban.hint")}</p>
      <DndContext
        sensors={sensors}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
        onDragCancel={() => setActiveId(null)}
      >
        <div className="flex gap-3 overflow-x-auto pb-2">
          {APPLICATION_STATUSES.map((status) => (
            <KanbanColumn
              key={status}
              status={status}
              items={grouped.get(status) ?? []}
              updatingId={updatingId}
            />
          ))}
        </div>
        <DragOverlay>
          {activeApplication ? <KanbanCardPreview application={activeApplication} /> : null}
        </DragOverlay>
      </DndContext>
    </div>
  );
}
