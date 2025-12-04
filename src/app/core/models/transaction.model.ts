export interface Transaction {
  id: string;
  date: string;
  description: string;
  category: 'Revenue' | 'Expenses' | 'Payroll';
  // optional type used for display/export ('INCOME' | 'EXPENSE')
  type?: 'INCOME' | 'EXPENSE';
  amount: number;
  documentId?: string; // Reference to source document
  createdAt: string;
}

export interface CreateTransactionRequest {
  date: string;
  description: string;
  category: 'Revenue' | 'Expenses' | 'Payroll';
  amount: number;
  documentId?: string;
}