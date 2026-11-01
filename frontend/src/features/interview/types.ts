export interface InterviewQuestion {
  question: string;
  reason: string;
  profileEvidence: string[];
}

export interface InterviewPreparation {
  technicalQuestions: InterviewQuestion[];
  behavioralQuestions: InterviewQuestion[];
  starPrompts: string[];
  recruiterQuestions: string[];
  missingEvidence: string[];
}

export interface InterviewPayload {
  offerText: string;
  applicationId?: string | null;
}
