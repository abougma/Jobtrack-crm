export const queryKeys = {
  me: ["users", "me"] as const,
  dashboard: ["dashboard"] as const,
  applications: {
    all: ["applications"] as const,
    list: (params: Record<string, unknown>) => ["applications", "list", params] as const,
    detail: (id: string) => ["applications", "detail", id] as const,
    history: (id: string) => ["applications", "history", id] as const,
    interactions: (id: string) => ["applications", "interactions", id] as const,
    reminders: (id: string) => ["applications", "reminders", id] as const,
  },
  contacts: ["contacts"] as const,
  profile: ["profile"] as const,
  cv: {
    all: ["cv"] as const,
    list: ["cv", "list"] as const,
    detail: (id: string) => ["cv", "detail", id] as const,
  },
  interview: (offerText: string) => ["interview", offerText] as const,
  organizations: {
    all: ["organizations"] as const,
    detail: (id: string) => ["organizations", "detail", id] as const,
    groups: (id: string) => ["organizations", id, "groups"] as const,
    members: (id: string) => ["organizations", id, "members"] as const,
    dashboard: (id: string, groupId?: string) =>
      ["organizations", id, "dashboard", groupId ?? "all"] as const,
  },
  plan: ["plan", "quota"] as const,
  reminders: (params: Record<string, unknown>) => ["reminders", params] as const,
  admin: {
    all: ["admin"] as const,
    overview: ["admin", "overview"] as const,
    users: (params: object) => ["admin", "users", params] as const,
    user: (id: string) => ["admin", "user", id] as const,
    organizations: (params: object) => ["admin", "organizations", params] as const,
  },
};
