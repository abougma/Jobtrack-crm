import type { ApplicationStatus } from "../../lib/domain";

export interface StatusCount {
  status: ApplicationStatus;
  count: number;
}

export interface SourceStat {
  source: string;
  count: number;
  responses: number;
  interviews: number;
  responseRate: number;
  interviewRate: number;
}

export interface WeeklyPoint {
  weekStart: string;
  applications: number;
  responses: number;
  interviews: number;
}

export interface FunnelStage {
  stage: string;
  count: number;
}

export interface DashboardReminder {
  id: string;
  applicationId?: string;
  applicationCompany?: string;
  applicationPosition?: string;
  title: string;
  dueAt: string;
  completed?: boolean;
}

export interface UpcomingInterview {
  id: string;
  applicationId: string;
  company: string;
  position: string;
  occurredAt: string;
}

export interface DashboardData {
  totalApplications: number;
  applied: number;
  responses: number;
  interviews: number;
  offers: number;
  rejected: number;
  noResponse: number;
  responseRate: number;
  interviewRate: number;
  offerRate: number;
  averageResponseDays: number;
  applicationsByStatus: StatusCount[];
  applicationsBySource: SourceStat[];
  weekly: WeeklyPoint[];
  funnel: FunnelStage[];
  dueReminders: DashboardReminder[];
  upcomingInterviews: UpcomingInterview[];
  topSources: SourceStat[];
}
