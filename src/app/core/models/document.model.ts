// Document types matching backend enums
export type DocumentType = 'PAYROLL_CERTIFICATE' | 'TAX_WITHHOLDING' | 'PAYMENT_PROOF' | 'OTHER';
export type UploadStatus = 'PENDING' | 'UPLOADED' | 'FAILED';

export interface Document {
  id: string;
  organizationId: string;
  periodId?: string;
  filename: string;
  type: DocumentType;
  uploadStatus: UploadStatus;
  filePath: string;
  uploadedAt: string;
}
