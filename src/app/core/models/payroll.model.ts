export interface PayrollFormula {
  id: string;
  concept: string;
  description: string;
  formula: string;
  createdAt: string;
}

export interface PayrollCalculation {
  id: string;
  formulaId: string;
  concept: string;
  inputs: { [key: string]: number };
  result: number;
  process: string[];
  calculatedAt: string;
}

export interface PayrollPeriodData {
  id: string;
  period: string;
  totalAmount: number;
  employees: number;
  status: 'draft' | 'pending' | 'approved' | 'processed';
}