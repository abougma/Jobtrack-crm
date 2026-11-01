import type { ConsentScope, OrganizationRole } from "../../lib/domain";

export interface OrganizationGroup {
  id: string;
  name: string;
  memberCount: number;
}

export interface Organization {
  id: string;
  name: string;
  role: OrganizationRole;
  memberCount: number;
  createdAt: string;
  groups: OrganizationGroup[];
}

export interface OrganizationMember {
  userId: string;
  email: string | null;
  firstName: string | null;
  lastName: string | null;
  role: OrganizationRole;
  consentScope: ConsentScope | null;
  joinedAt: string;
}

export interface InvitationResult {
  token: string;
  expiresAt: string;
}

export interface ConsentView {
  scope: ConsentScope | null;
  active: boolean;
}

export interface GroupMetric {
  id: string;
  name: string;
  members: number;
  applications: number;
  interviews: number;
}

export interface MemberDetail {
  userId: string;
  displayName: string;
  applications: number;
  interviews: number;
  consentScope: ConsentScope;
}

export interface OrganizationDashboard {
  members: number;
  membersWithConsent: number;
  applications: number;
  interviews: number;
  offers: number;
  responseRate: number;
  interviewRate: number;
  groups: GroupMetric[];
  memberDetails: MemberDetail[];
}
