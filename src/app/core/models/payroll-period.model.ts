export type ComplianceStatus = 'approved' | 'processed' | 'draft' | 'cancelled';

export interface PayrollPeriod {
  id: string;
  month: string;
  year: number;
  isActive: boolean;
}

export interface DailyPayrollRecord {
  id: string;
  date: string;
  status: ComplianceStatus;
  lastModified: string;
  complianceScore: number;
}