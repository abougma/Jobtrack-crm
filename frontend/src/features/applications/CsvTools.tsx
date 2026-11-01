import { useState } from "react";
import { Download, Upload } from "lucide-react";
import { errorMessage } from "../../lib/api";
import { useI18n } from "../../lib/i18n";
import { downloadBlob } from "../../lib/utils";
import { Button } from "../../components/ui/Button";
import { Dialog } from "../../components/ui/Dialog";
import { useToast } from "../../components/ui/toast-context";
import { exportApplicationsCsv } from "./api";
import { useImportApplicationsMutation } from "./hooks";
import type { ApplicationFilters, ImportResult } from "./types";

export function CsvExportButton({ filters }: { filters: ApplicationFilters }) {
  const { t } = useI18n();
  const { push } = useToast();
  const [loading, setLoading] = useState(false);

  const handleExport = async () => {
    setLoading(true);
    try {
      const { blob, filename } = await exportApplicationsCsv(filters);
      downloadBlob(blob, filename || "jobtrack-candidatures.csv");
    } catch (error) {
      push(errorMessage(error, t("errors.generic")), "error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Button
      variant="outline"
      onClick={() => void handleExport()}
      loading={loading}
      leftIcon={<Download className="h-4 w-4" aria-hidden="true" />}
    >
      {loading ? t("applications.csv.exporting") : t("applications.csv.export")}
    </Button>
  );
}

export function CsvImportDialog({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { t } = useI18n();
  const { push } = useToast();
  const [file, setFile] = useState<File | null>(null);
  const [result, setResult] = useState<ImportResult | null>(null);
  const mutation = useImportApplicationsMutation();

  const handleClose = () => {
    setFile(null);
    setResult(null);
    mutation.reset();
    onClose();
  };

  const handleSubmit = async () => {
    if (!file) return;
    try {
      const imported = await mutation.mutateAsync(file);
      setResult(imported);
      push(
        t("applications.csv.importSuccess", {
          imported: imported.imported,
          skipped: imported.skipped,
        }),
        "success",
      );
    } catch (error) {
      push(errorMessage(error, t("errors.generic")), "error");
    }
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      title={t("applications.csv.importTitle")}
      description={t("applications.csv.importHint")}
      footer={
        <>
          <Button variant="ghost" onClick={handleClose}>
            {t("common.close")}
          </Button>
          <Button
            onClick={() => void handleSubmit()}
            disabled={!file}
            loading={mutation.isPending}
            leftIcon={<Upload className="h-4 w-4" aria-hidden="true" />}
          >
            {mutation.isPending ? t("applications.csv.importing") : t("common.import")}
          </Button>
        </>
      }
    >
      <div className="space-y-4">
        <div>
          <label
            htmlFor="csv-file"
            className="mb-1.5 block text-xs font-semibold text-fg"
          >
            {t("applications.csv.file")}
          </label>
          <input
            id="csv-file"
            type="file"
            accept=".csv,text/csv"
            onChange={(event) => setFile(event.target.files?.[0] ?? null)}
            className="block w-full cursor-pointer rounded-lg border border-border bg-elevated px-3 py-2 text-sm text-muted file:mr-3 file:rounded-md file:border-0 file:bg-primary-600 file:px-3 file:py-1.5 file:text-xs file:font-semibold file:text-white hover:file:bg-primary-700"
          />
          <p className="mt-1 text-xs text-muted">{file ? file.name : t("applications.csv.noFile")}</p>
        </div>

        {result && (
          <div className="space-y-2 rounded-xl border border-border bg-elevated p-3">
            <p className="text-sm font-semibold text-fg">
              {t("applications.csv.importSuccess", {
                imported: result.imported,
                skipped: result.skipped,
              })}
            </p>
            {result.errors.length > 0 && (
              <div>
                <p className="text-xs font-semibold text-rose-600 dark:text-rose-400">
                  {t("applications.csv.importErrors")}
                </p>
                <ul className="mt-1 space-y-1 text-xs text-muted">
                  {result.errors.slice(0, 8).map((item) => (
                    <li key={`${item.line}-${item.message}`}>
                      <span className="font-semibold text-fg">
                        {t("applications.csv.line", { line: item.line })}
                      </span>{" "}
                      — {item.message}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        )}
      </div>
    </Dialog>
  );
}
