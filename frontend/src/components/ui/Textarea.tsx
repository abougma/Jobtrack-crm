import { forwardRef, useId, type TextareaHTMLAttributes } from "react";
import { cn } from "../../lib/utils";

export interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
  hint?: string;
  containerClassName?: string;
}

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(function Textarea(
  { label, error, hint, className, containerClassName, id, required, ...props },
  ref,
) {
  const generatedId = useId();
  const textareaId = id ?? generatedId;
  const describedBy = [error ? `${textareaId}-error` : null, hint ? `${textareaId}-hint` : null]
    .filter(Boolean)
    .join(" ");

  return (
    <div className={cn("flex flex-col gap-1.5", containerClassName)}>
      {label && (
        <label htmlFor={textareaId} className="text-xs font-semibold text-fg">
          {label}
          {required && (
            <span className="ml-0.5 text-rose-500" aria-hidden="true">
              *
            </span>
          )}
        </label>
      )}
      <textarea
        ref={ref}
        id={textareaId}
        required={required}
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy || undefined}
        className={cn(
          "surface-input min-h-28 resize-y",
          error && "border-rose-500 focus:ring-rose-500/25",
          className,
        )}
        {...props}
      />
      {hint && !error && (
        <p id={`${textareaId}-hint`} className="text-xs text-muted">
          {hint}
        </p>
      )}
      {error && (
        <p
          id={`${textareaId}-error`}
          className="text-xs font-medium text-rose-600 dark:text-rose-400"
        >
          {error}
        </p>
      )}
    </div>
  );
});
