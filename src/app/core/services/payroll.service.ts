import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// Payroll Period interfaces
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

export interface CreatePeriodRequest {
  period: string;
  startDate: string;
  endDate: string;
}

export interface UpdatePeriodStatusRequest {
  status: 'DRAFT' | 'PROCESSED' | 'PAUSED';
}

// Payroll Formula interfaces
export interface PayrollFormula {
  id: string;
  organizationId: string;
  concept: string;
  description: string;
  formula: string;
  createdAt: string;
}

export interface CreateFormulaRequest {
  concept: string;
  description: string;
  formula: string;
}

export interface ExecuteFormulaRequest {
  periodId: string;
  inputs: { [key: string]: number };
}

export interface PayrollCalculation {
  id: string;
  formulaId: string;
  periodId: string;
  inputs: { [key: string]: number };
  result: number;
  executedAt: string;
}

@Injectable({ providedIn: 'root' })
export class PayrollService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // ========== Payroll Periods ==========
  listPeriods(): Observable<PayrollPeriod[]> {
    return this.http.get<PayrollPeriod[]>(`${this.apiUrl}/payroll-periods`);
  }

  createPeriod(request: CreatePeriodRequest): Observable<PayrollPeriod> {
    return this.http.post<PayrollPeriod>(`${this.apiUrl}/payroll-periods`, request);
  }

  updatePeriodStatus(periodId: string, request: UpdatePeriodStatusRequest): Observable<PayrollPeriod> {
    return this.http.put<PayrollPeriod>(`${this.apiUrl}/payroll-periods/${periodId}/status`, request);
  }

  // ========== Payroll Formulas ==========
  listFormulas(): Observable<PayrollFormula[]> {
    return this.http.get<PayrollFormula[]>(`${this.apiUrl}/payroll-formulas`);
  }

  createFormula(request: CreateFormulaRequest): Observable<PayrollFormula> {
    return this.http.post<PayrollFormula>(`${this.apiUrl}/payroll-formulas`, request);
  }

  deleteFormula(formulaId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/payroll-formulas/${formulaId}`);
  }

  executeFormula(formulaId: string, request: ExecuteFormulaRequest): Observable<PayrollCalculation> {
    return this.http.post<PayrollCalculation>(`${this.apiUrl}/payroll-formulas/${formulaId}/execute`, request);
  }

  // ========== Payroll Calculations ==========
  listCalculations(periodId?: string): Observable<PayrollCalculation[]> {
    let params = new HttpParams();
    if (periodId) {
      params = params.set('periodId', periodId);
    }
    return this.http.get<PayrollCalculation[]>(`${this.apiUrl}/payroll-calculations`, { params });
  }
}
