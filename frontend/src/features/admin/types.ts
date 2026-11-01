import type { Plan, UserRole } from "../../lib/auth";

export interface AdminOverview {
  totalUsers: number;
  platformAdmins: number;
  newUsersLast30Days: number;
  totalOrganizations: number;
  totalMemberships: number;
  totalApplications: number;
  usersByPlan: { plan: Plan; users: number }[];
}

export interface AdminUser {
  id: string;
  email: string;
  role: UserRole;
  plan: Plan;
  firstName: string | null;
  lastName: string | null;
  createdAt: string;
}

export interface AdminUserDetail extends AdminUser {
  headline: string | null;
  location: string | null;
  cvAnalysisUsed: number;
  cvAnalysisLimit: number;
  cvAnalysisResetAt: string;
  applicationCount: number;
  organizations: string[];
}

export interface AdminOrganization {
  id: string;
  name: string;
  members: number;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface AdminUserFilters {
  query?: string;
  plan?: Plan;
  role?: UserRole;
  page?: number;
  size?: number;
}

export interface AdminOrganizationFilters {
  query?: string;
  page?: number;
  size?: number;
}
