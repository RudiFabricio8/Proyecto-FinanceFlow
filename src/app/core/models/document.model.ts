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
  
  // Enhanced metadata for card display
  title?: string; // e.g., "Recibo de Compra - Supermercado A"
  amount?: number; // Extracted or manually entered amount
  provider?: string; // e.g., "Supermercado A"
  date?: string; // Document date (may differ from uploadedAt)
  category?: string; // e.g., "Groceries", "Utilities"
}

export interface DocumentMetadata {
  title: string;
  amount?: number;
  provider?: string;
  date?: string;
  category?: string;
}
