export type UserRole = "JOB_SEEKER" | "ORG_ADMIN" | "ORG_COACH" | "PLATFORM_ADMIN" | "MEMBER";
export type Plan = "FREE" | "PRO" | "PREMIUM" | "ORGANIZATION";

export interface AuthUser {
  id: string;
  email: string;
  role: UserRole | string;
  firstName: string | null;
  lastName: string | null;
  headline: string | null;
  location: string | null;
  phone?: string | null;
  linkedinUrl?: string | null;
  portfolioUrl?: string | null;
  plan: Plan | string;
  createdAt?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: AuthUser;
}

const ACCESS_TOKEN_KEY = "jobtrack.accessToken";
const REFRESH_TOKEN_KEY = "jobtrack.refreshToken";
const USER_KEY = "jobtrack.user";

function safeGet(key: string): string | null {
  try {
    return localStorage.getItem(key);
  } catch {
    return null;
  }
}

function safeSet(key: string, value: string): void {
  try {
    localStorage.setItem(key, value);
  } catch {
    // Storage is best-effort; the in-memory session still works for the tab.
  }
}

function safeRemove(key: string): void {
  try {
    localStorage.removeItem(key);
  } catch {
    // Ignore.
  }
}

export function getAccessToken(): string | null {
  return safeGet(ACCESS_TOKEN_KEY);
}

export function getRefreshToken(): string | null {
  return safeGet(REFRESH_TOKEN_KEY);
}

export function getStoredUser(): AuthUser | null {
  const raw = safeGet(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export function storeUser(user: AuthUser): void {
  safeSet(USER_KEY, JSON.stringify(user));
}

export function storeSession(response: AuthResponse): void {
  safeSet(ACCESS_TOKEN_KEY, response.accessToken);
  safeSet(REFRESH_TOKEN_KEY, response.refreshToken);
  storeUser(response.user);
}

export function storeTokens(accessToken: string, refreshToken: string): void {
  safeSet(ACCESS_TOKEN_KEY, accessToken);
  safeSet(REFRESH_TOKEN_KEY, refreshToken);
}

export function clearSession(): void {
  safeRemove(ACCESS_TOKEN_KEY);
  safeRemove(REFRESH_TOKEN_KEY);
  safeRemove(USER_KEY);
}

export function hasSession(): boolean {
  return Boolean(getAccessToken() || getRefreshToken());
}

export const SESSION_EXPIRED_EVENT = "jobtrack:session-expired";
