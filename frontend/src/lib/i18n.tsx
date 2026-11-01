import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { en } from "./translations/en";
import { fr, type TranslationKey } from "./translations/fr";

export type Language = "fr" | "en";
export type { TranslationKey };

const STORAGE_KEY = "jobtrack.lang";
const dictionaries: Record<Language, Record<TranslationKey, string>> = { fr, en };

function readInitialLanguage(): Language {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === "fr" || stored === "en") return stored;
  } catch {
    // localStorage can be unavailable (private mode); fall back to French.
  }
  return "fr";
}

export function interpolate(template: string, vars?: Record<string, string | number>): string {
  if (!vars) return template;
  return template.replace(/\{(\w+)\}/g, (match, name: string) =>
    name in vars ? String(vars[name]) : match,
  );
}

export type TranslateFn = (key: TranslationKey, vars?: Record<string, string | number>) => string;

interface I18nContextValue {
  language: Language;
  setLanguage: (language: Language) => void;
  t: TranslateFn;
}

const I18nContext = createContext<I18nContextValue | null>(null);

export function I18nProvider({ children }: { children: ReactNode }) {
  const [language, setLanguageState] = useState<Language>(readInitialLanguage);

  useEffect(() => {
    document.documentElement.lang = language;
  }, [language]);

  const setLanguage = useCallback((next: Language) => {
    setLanguageState(next);
    try {
      localStorage.setItem(STORAGE_KEY, next);
    } catch {
      // Persisting the language is best-effort.
    }
  }, []);

  const t = useCallback<TranslateFn>(
    (key, vars) => interpolate(dictionaries[language][key] ?? dictionaries.fr[key] ?? key, vars),
    [language],
  );

  const value = useMemo(() => ({ language, setLanguage, t }), [language, setLanguage, t]);

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

export function useI18n(): I18nContextValue {
  const context = useContext(I18nContext);
  if (!context) throw new Error("useI18n must be used within an I18nProvider");
  return context;
}
