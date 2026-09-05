export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface CitizenRegistrationRequest {
  fullName: string;
  email: string;
  phone: string;
  dateOfBirth: string;
  aadhaarNumber: string;
  address?: string;
  state?: string;
  pincode?: string;
}

/**
 * Payload for POST /api/citizens/me/onboarding.
 * Intentionally omits email and keycloakUserId — those are
 * derived exclusively from the JWT on the backend.
 */
export interface OnboardingRequest {
  fullName: string;
  phone: string;
  dateOfBirth: string;   // ISO date yyyy-MM-dd
  aadhaarNumber: string;
  address?: string;
  state?: string;
  pincode?: string;
}

export interface CitizenProfileResponse {
  id: string;
  fullName: string;
  email: string;
  phone: string;
  dateOfBirth: string;
  address: string;
  state: string;
  pincode: string;
  kycStatus: KycStatus;
  registeredAt: string;
  kycVerifiedAt: string | null;
}

export type KycStatus = 'PENDING' | 'INITIATED' | 'VERIFIED' | 'FAILED';

export interface KycInitiateRequest {
  citizenId: string;
  aadhaarNumber: string;
  phone?: number;
}

export interface GenerateOtpRequest {
  citizenId: string;
  aadhaarNumber: string;
  mobile?: number;
}

export interface GenerateOtpResponse {
  txnId: string;
}

export interface VerifyOtpRequest {
  txnId: string;
  otp: string;
}

export interface VerifyOtpResponse {
  citizenId: string;
  status: KycStatus;
}

export interface KycStatusResponse {
  citizenId: string;
  status: KycStatus;
  verifiedAt: string | null;
  demographicData: string | null;
}

export interface DocumentFetchRequest {
  citizenId: string;
  aadhaarNumber?: string;
  documentType: DocumentType;
}

export type DocumentType = 'AADHAAR' | 'PAN' | 'PASSPORT';
export type FetchStatus = 'REQUESTED' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';

export interface DocumentResponse {
  id: number;
  citizenId: string;
  documentType: DocumentType;
  status: FetchStatus;
  documentUrl: string | null;
  requestedAt: string | null;
  completedAt: string | null;
}

export interface CertificateRequest {
  certificateType: string;
  purpose: string;
  remarks?: string;
}

export type CertificateStatus = 'PENDING' | 'GENERATED' | 'FAILED';

export interface CertificateResponse {
  id: string;
  citizenId: string;
  certificateType: string;
  purpose: string;
  status: CertificateStatus;
  certificateNumber: string | null;
  downloadUrl: string | null;
  issuedAt: string | null;
  remarks: string | null;
  createdAt: string;
}

export interface ServiceRequestResponse {
  serviceId: string;
  serviceName: string;
  description: string;
  status: string;
  kycRequired: boolean;
}

// Grievance types
export type GrievanceCategory = 'SERVICE_DELIVERY' | 'DOCUMENT_ISSUES' | 'INCORRECT_CERTIFICATE' | 'BILLING' | 'TECHNICAL' | 'OTHER';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type GrievanceStatus = 'SUBMITTED' | 'ACKNOWLEDGED' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'REJECTED';

export interface GrievanceSubmitRequest {
  citizenId?: string; // optional; server will derive if omitted
  title: string;
  description: string;
  category: GrievanceCategory;
  priority: Priority;
  attachmentUrl?: string;
}

export interface GrievanceResponse {
  id: string;
  citizenId: string;
  referenceNumber: string;
  title: string;
  description: string;
  category: GrievanceCategory;
  status: GrievanceStatus;
  priority: Priority;
  submittedAt: string;
  acknowledgedAt?: string;
  resolvedAt?: string;
  closedAt?: string;
  expectedResolutionDate?: string;
  assignedOfficer?: string;
  resolutionNotes?: string;
  attachmentUrl?: string;
  updatedAt?: string;
}
