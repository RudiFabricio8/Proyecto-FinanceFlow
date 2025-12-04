// Payroll Formula
export interface PayrollFormula {
  id: string;
  organizationId: string;
  concept: string;
  description: string;
  formula: string;
  createdAt: string;
}

// Payroll Calculation
export interface PayrollCalculation {
  id: string;
  formulaId: string;
  periodId: string;
  inputs: { [key: string]: number };
  result: number;
  executedAt: string;
}

// Payroll Period
export interface PayrollPeriod {
  id: string;
  organizationId: string;
  period: string;
  status: 'DRAFT' | 'PROCESSED' | 'PAUSED';
  startDate: string;
  endDate: string;
  createdAt: string;
  updatedAt: string;
}