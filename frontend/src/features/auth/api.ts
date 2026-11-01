import { apiRequest } from "../../lib/api";
import { getRefreshToken, type AuthResponse, type AuthUser } from "../../lib/auth";
import type { Credentials } from "./types";

export function loginRequest(credentials: Credentials): Promise<AuthResponse> {
  return apiRequest<AuthResponse>("/auth/login", {
    method: "POST",
    body: credentials,
    auth: false,
  });
}

export function registerRequest(credentials: Credentials): Promise<AuthResponse> {
  return apiRequest<AuthResponse>("/auth/register", {
    method: "POST",
    body: credentials,
    auth: false,
  });
}

export function logoutRequest(): Promise<void> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return Promise.resolve();
  return apiRequest<void>("/auth/logout", {
    method: "POST",
    body: { refreshToken },
    auth: false,
  });
}

export function fetchMe(): Promise<AuthUser> {
  return apiRequest<AuthUser>("/users/me");
}
