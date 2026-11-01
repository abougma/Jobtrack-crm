import type {
  ApplicationStatus,
  ContractType,
  InteractionType,
  WorkMode,
} from "../../lib/domain";

export interface ApplicationSummary {
  id: string;
  company: string;
  position: string;
  location: string | null;
  workMode: WorkMode | null;
  contractType: ContractType | null;
  salaryMin: number | null;
  salaryMax: number | null;
  source: string | null;
  offerUrl: string | null;
  status: ApplicationStatus;
  appliedAt: string | null;
  contactId: string | null;
  contactName: string | null;
  nextReminderAt: string | null;
  interactionCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface StatusChange {
  id: string;
  previousStatus: ApplicationStatus | null;
  newStatus: ApplicationStatus;
  changedAt: string;
}

export interface Interaction {
  id: string;
  applicationId?: string;
  type: InteractionType;
  message: string;
  occurredAt: string;
}

export interface Reminder {
  id: string;
  applicationId?: string;
  applicationCompany?: string;
  applicationPosition?: string;
  title: string;
  dueAt: string;
  completed: boolean;
}

export interface ApplicationDetail extends ApplicationSummary {
  notes: string | null;
  history: StatusChange[];
  interactions: Interaction[];
  reminders: Reminder[];
  generatedCvId: string | null;
}

export interface ApplicationPayload {
  company: string;
  position: string;
  location?: string | null;
  workMode?: WorkMode | null;
  contractType?: ContractType | null;
  salaryMin?: number | null;
  salaryMax?: number | null;
  source?: string | null;
  offerUrl?: string | null;
  appliedAt?: string | null;
  notes?: string | null;
  status?: ApplicationStatus;
  contactId?: string | null;
}

export interface InteractionPayload {
  type: InteractionType;
  message: string;
  occurredAt?: string;
}

export interface ReminderPayload {
  title: string;
  dueAt: string;
}

export interface ApplicationFilters {
  search: string;
  status: string;
  source: string;
  contractType: string;
  workMode: string;
  from: string;
  to: string;
  sort: string;
  page: number;
  size: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface ImportResult {
  imported: number;
  skipped: number;
  errors: { line: number; message: string }[];
}
