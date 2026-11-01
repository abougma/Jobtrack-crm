import { useCallback, useMemo, useRef, useState, type ReactNode } from "react";
import { createPortal } from "react-dom";
import { CheckCircle2, Info, X, XCircle } from "lucide-react";
import { useI18n } from "../../lib/i18n";
import { cn } from "../../lib/utils";
import { ToastContext, type ToastVariant } from "./toast-context";

interface ToastItem {
  id: number;
  message: string;
  variant: ToastVariant;
}

const icons: Record<ToastVariant, ReactNode> = {
  success: <CheckCircle2 className="h-4 w-4 text-emerald-500" aria-hidden="true" />,
  error: <XCircle className="h-4 w-4 text-rose-500" aria-hidden="true" />,
  info: <Info className="h-4 w-4 text-primary-500" aria-hidden="true" />,
};

export function ToastProvider({ children }: { children: ReactNode }) {
  const { t } = useI18n();
  const [toasts, setToasts] = useState<ToastItem[]>([]);
  const counter = useRef(0);

  const dismiss = useCallback((id: number) => {
    setToasts((current) => current.filter((toast) => toast.id !== id));
  }, []);

  const push = useCallback(
    (message: string, variant: ToastVariant = "info") => {
      counter.current += 1;
      const id = counter.current;
      setToasts((current) => [...current.slice(-2), { id, message, variant }]);
      window.setTimeout(() => dismiss(id), 4500);
    },
    [dismiss],
  );

  const value = useMemo(() => ({ push, dismiss }), [push, dismiss]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      {createPortal(
        <div
          className="pointer-events-none fixed bottom-4 right-4 z-[60] flex w-[calc(100%-2rem)] max-w-sm flex-col gap-2"
          aria-live="polite"
          aria-atomic="false"
        >
          {toasts.map((toast) => (
            <div
              key={toast.id}
              role={toast.variant === "error" ? "alert" : "status"}
              className={cn(
                "pointer-events-auto flex items-start gap-2 rounded-xl border border-border bg-surface p-3 shadow-lg animate-fade-in",
              )}
            >
              <span className="mt-0.5">{icons[toast.variant]}</span>
              <p className="flex-1 text-sm text-fg">{toast.message}</p>
              <button
                type="button"
                onClick={() => dismiss(toast.id)}
                className="rounded p-0.5 text-muted hover:text-fg focus-visible:ring-2 focus-visible:ring-primary-500"
                aria-label={t("aria.close")}
              >
                <X className="h-3.5 w-3.5" aria-hidden="true" />
              </button>
            </div>
          ))}
        </div>,
        document.body,
      )}
    </ToastContext.Provider>
  );
}
