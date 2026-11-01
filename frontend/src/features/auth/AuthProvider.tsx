import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { useQueryClient } from "@tanstack/react-query";
import { SESSION_EXPIRED_EVENT, clearSession, getStoredUser, hasSession, storeSession, storeUser, type AuthUser } from "../../lib/auth";
import { fetchMe, loginRequest, logoutRequest, registerRequest } from "./api";
import type { AuthContextValue, Credentials } from "./types";

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [user, setUser] = useState<AuthUser | null>(() => getStoredUser());
  const [loading, setLoading] = useState<boolean>(() => hasSession());

  useEffect(() => {
    let cancelled = false;

    if (!hasSession()) {
      setLoading(false);
      return;
    }

    fetchMe()
      .then((me) => {
        if (cancelled) return;
        setUser(me);
        storeUser(me);
      })
      .catch(() => {
        if (cancelled) return;
        clearSession();
        setUser(null);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    const onExpired = () => {
      setUser(null);
      queryClient.clear();
    };
    window.addEventListener(SESSION_EXPIRED_EVENT, onExpired);
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, onExpired);
  }, [queryClient]);

  const login = useCallback(async (credentials: Credentials) => {
    const response = await loginRequest(credentials);
    storeSession(response);
    setUser(response.user);
    return response.user;
  }, []);

  const register = useCallback(async (credentials: Credentials) => {
    const response = await registerRequest(credentials);
    storeSession(response);
    setUser(response.user);
    return response.user;
  }, []);

  const logout = useCallback(async () => {
    try {
      await logoutRequest();
    } catch {
      // A failed server-side logout must not keep the local session alive.
    }
    clearSession();
    setUser(null);
    queryClient.clear();
  }, [queryClient]);

  const refreshUser = useCallback(async () => {
    if (!hasSession()) return null;
    try {
      const me = await fetchMe();
      setUser(me);
      storeUser(me);
      return me;
    } catch {
      return null;
    }
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      loading,
      isAuthenticated: Boolean(user),
      login,
      register,
      logout,
      refreshUser,
    }),
    [user, loading, login, register, logout, refreshUser],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within an AuthProvider");
  return context;
}
