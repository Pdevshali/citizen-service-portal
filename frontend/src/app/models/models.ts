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

export interface CertificateRequest {
  certificateType: string;
  purpose: string;
  remarks?: string;
}

export interface ServiceRequestResponse {
  serviceId: string;
  serviceName: string;
  description: string;
  status: string;
  kycRequired: boolean;
}
