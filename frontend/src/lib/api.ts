import {
  SESSION_EXPIRED_EVENT,
  clearSession,
  getAccessToken,
  getRefreshToken,
  storeTokens,
  storeUser,
  type AuthResponse,
} from "./auth";

export const API_BASE_URL: string = import.meta.env.VITE_API_URL ?? "/api/v1";

export interface FieldError {
  field: string;
  message: string;
}

export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  errors?: FieldError[];
}

export class ApiError extends Error {
  readonly status: number;
  readonly problem: ProblemDetail | null;
  readonly errors: FieldError[];

  constructor(status: number, message: string, problem: ProblemDetail | null = null) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.problem = problem;
    this.errors = problem?.errors ?? [];
  }

  fieldError(field: string): string | undefined {
    return this.errors.find((error) => error.field === field)?.message;
  }
}

export interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  headers?: Record<string, string>;
  auth?: boolean;
  signal?: AbortSignal;
}

function buildUrl(path: string): string {
  if (/^https?:\/\//.test(path)) return path;
  return `${API_BASE_URL}${path.startsWith("/") ? path : `/${path}`}`;
}

async function parseProblem(response: Response): Promise<ProblemDetail | null> {
  const contentType = response.headers.get("content-type") ?? "";
  if (!contentType.includes("json") && !contentType.includes("problem")) return null;
  try {
    return (await response.json()) as ProblemDetail;
  } catch {
    return null;
  }
}

async function toApiError(response: Response): Promise<ApiError> {
  const problem = await parseProblem(response);
  const message =
    problem?.detail ??
    problem?.title ??
    (response.status === 401
      ? "Session expirée."
      : response.status === 403
        ? "Accès refusé."
        : response.status === 404
          ? "Ressource introuvable."
          : "Une erreur est survenue.");
  return new ApiError(response.status, message, problem);
}

let refreshPromise: Promise<string | null> | null = null;

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return null;

  if (!refreshPromise) {
    refreshPromise = (async () => {
      try {
        const response = await fetch(buildUrl("/auth/refresh"), {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ refreshToken }),
        });
        if (!response.ok) return null;
        const data = (await response.json()) as AuthResponse;
        storeTokens(data.accessToken, data.refreshToken);
        if (data.user) storeUser(data.user);
        return data.accessToken;
      } catch {
        return null;
      } finally {
        refreshPromise = null;
      }
    })();
  }
  return refreshPromise;
}

function handleSessionExpired(): void {
  clearSession();
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT));
  }
}

async function authorizedFetch(path: string, options: RequestOptions): Promise<Response> {
  const { method = "GET", body, headers = {}, auth = true, signal } = options;
  const isFormData = typeof FormData !== "undefined" && body instanceof FormData;

  const execute = (token: string | null): Promise<Response> => {
    const finalHeaders: Record<string, string> = { ...headers };
    if (!isFormData && body !== undefined) finalHeaders["Content-Type"] = "application/json";
    if (auth && token) finalHeaders.Authorization = `Bearer ${token}`;

    return fetch(buildUrl(path), {
      method,
      signal,
      headers: finalHeaders,
      body:
        body === undefined ? undefined : isFormData ? (body as FormData) : JSON.stringify(body),
    });
  };

  let response = await execute(auth ? getAccessToken() : null);

  if (response.status === 401 && auth) {
    const newToken = await refreshAccessToken();
    if (newToken) {
      response = await execute(newToken);
    } else {
      handleSessionExpired();
      throw await toApiError(response);
    }
  }

  return response;
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await authorizedFetch(path, options);

  if (!response.ok) {
    throw await toApiError(response);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const contentType = response.headers.get("content-type") ?? "";
  if (contentType.includes("application/json") || contentType.includes("+json")) {
    return (await response.json()) as T;
  }

  const text = await response.text();
  if (!text) return undefined as T;
  try {
    return JSON.parse(text) as T;
  } catch {
    return text as unknown as T;
  }
}

function filenameFromDisposition(header: string | null, fallback: string): string {
  if (!header) return fallback;
  const utf8Match = /filename\*=UTF-8''([^;]+)/i.exec(header);
  if (utf8Match?.[1]) return decodeURIComponent(utf8Match[1]);
  const simpleMatch = /filename="?([^";]+)"?/i.exec(header);
  return simpleMatch?.[1] ?? fallback;
}

export interface DownloadedFile {
  blob: Blob;
  filename: string;
}

export async function apiDownload(
  path: string,
  fallbackFilename: string,
  options: RequestOptions = {},
): Promise<DownloadedFile> {
  const response = await authorizedFetch(path, { ...options, method: options.method ?? "GET" });
  if (!response.ok) throw await toApiError(response);
  const blob = await response.blob();
  const filename = filenameFromDisposition(
    response.headers.get("content-disposition"),
    fallbackFilename,
  );
  return { blob, filename };
}

export function buildQuery(params: Record<string, string | number | boolean | undefined | null>): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") return;
    search.set(key, String(value));
  });
  const query = search.toString();
  return query ? `?${query}` : "";
}

export function errorMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError) return error.message || fallback;
  if (error instanceof Error && error.message) return error.message;
  return fallback;
}
