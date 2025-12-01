export interface Receipt {
  id: string;
  employeeName: string;
  employeeId: string;
  period: string;
  amount: number;
  issuedAt: Date;
  status: 'generated' | 'downloaded' | 'pending';
  pdfUrl?: string;
}
