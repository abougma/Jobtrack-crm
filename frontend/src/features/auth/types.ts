import type { AuthResponse, AuthUser, Plan, UserRole } from "../../lib/auth";

export type { AuthResponse, AuthUser, Plan, UserRole };

export interface Credentials {
  email: string;
  password: string;
}

export interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  isAuthenticated: boolean;
  login: (credentials: Credentials) => Promise<AuthUser>;
  register: (credentials: Credentials) => Promise<AuthUser>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<AuthUser | null>;
}
