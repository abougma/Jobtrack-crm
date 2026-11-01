import { format, formatDistanceToNowStrict, isValid, parseISO } from "date-fns";
import { enUS, fr } from "date-fns/locale";
import type { Language } from "./i18n";

const locales = { fr, en: enUS };
const intlLocales: Record<Language, string> = { fr: "fr-FR", en: "en-GB" };

export function toDate(value: string | Date | null | undefined): Date | null {
  if (!value) return null;
  const date = value instanceof Date ? value : parseISO(value);
  return isValid(date) ? date : null;
}

export function formatDate(
  value: string | Date | null | undefined,
  language: Language = "fr",
  pattern = "d MMM yyyy",
): string {
  const date = toDate(value);
  if (!date) return "—";
  return format(date, pattern, { locale: locales[language] });
}

export function formatDateTime(
  value: string | Date | null | undefined,
  language: Language = "fr",
): string {
  return formatDate(value, language, "d MMM yyyy, HH:mm");
}

export function formatShortDate(
  value: string | Date | null | undefined,
  language: Language = "fr",
): string {
  return formatDate(value, language, "d MMM");
}

export function formatRelative(
  value: string | Date | null | undefined,
  language: Language = "fr",
): string {
  const date = toDate(value);
  if (!date) return "—";
  return formatDistanceToNowStrict(date, { addSuffix: true, locale: locales[language] });
}

export function formatNumber(value: number | null | undefined, language: Language = "fr"): string {
  if (value === null || value === undefined || Number.isNaN(value)) return "—";
  return new Intl.NumberFormat(intlLocales[language]).format(value);
}

export function formatPercent(
  value: number | null | undefined,
  language: Language = "fr",
  digits = 1,
): string {
  if (value === null || value === undefined || Number.isNaN(value)) return "—";
  return new Intl.NumberFormat(intlLocales[language], {
    style: "percent",
    minimumFractionDigits: 0,
    maximumFractionDigits: digits,
  }).format(value / 100);
}

export function formatSalary(
  min: number | null | undefined,
  max: number | null | undefined,
  language: Language = "fr",
): string {
  const formatter = new Intl.NumberFormat(intlLocales[language], {
    style: "currency",
    currency: "EUR",
    maximumFractionDigits: 0,
  });
  if (min && max) {
    if (min === max) return formatter.format(min);
    return `${formatter.format(min)} – ${formatter.format(max)}`;
  }
  if (min) return `${formatter.format(min)}+`;
  if (max) return `≤ ${formatter.format(max)}`;
  return "—";
}

export function toDateInputValue(value: string | Date | null | undefined): string {
  const date = toDate(value);
  if (!date) return "";
  return format(date, "yyyy-MM-dd");
}

export function toDateTimeInputValue(value: string | Date | null | undefined): string {
  const date = toDate(value);
  if (!date) return "";
  return format(date, "yyyy-MM-dd'T'HH:mm");
}

export function isPast(value: string | Date | null | undefined): boolean {
  const date = toDate(value);
  return date ? date.getTime() < Date.now() : false;
}

export function isToday(value: string | Date | null | undefined): boolean {
  const date = toDate(value);
  if (!date) return false;
  const now = new Date();
  return (
    date.getFullYear() === now.getFullYear() &&
    date.getMonth() === now.getMonth() &&
    date.getDate() === now.getDate()
  );
}
