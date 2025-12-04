import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

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

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getOrganizationId(): string | null {
    const user = this.authService.getCurrentUser();
    return user?.organizationId || null;
  }

  // ========== Payroll Periods ==========
  listPeriods(): Observable<PayrollPeriod[]> {
    const orgId = this.getOrganizationId();
    if (!orgId) {
      console.warn('No organizationId available');
      return of([]);
    }
    return this.http.get<PayrollPeriod[]>(`${this.apiUrl}/payroll-periods?organizationId=${orgId}`);
  }

  createPeriod(request: CreatePeriodRequest): Observable<PayrollPeriod> {
    const orgId = this.getOrganizationId();
    return this.http.post<PayrollPeriod>(`${this.apiUrl}/payroll-periods?organizationId=${orgId}`, request);
  }

  updatePeriodStatus(periodId: string, request: UpdatePeriodStatusRequest): Observable<PayrollPeriod> {
    return this.http.put<PayrollPeriod>(`${this.apiUrl}/payroll-periods/${periodId}/status`, request);
  }

  // ========== Payroll Formulas ==========
  listFormulas(): Observable<PayrollFormula[]> {
    const orgId = this.getOrganizationId();
    if (!orgId) {
      console.warn('No organizationId available');
      return of([]);
    }
    return this.http.get<PayrollFormula[]>(`${this.apiUrl}/payroll-formulas?organizationId=${orgId}`);
  }

  createFormula(request: CreateFormulaRequest): Observable<PayrollFormula> {
    const orgId = this.getOrganizationId();
    if (!orgId) {
      throw new Error('Organization ID not found');
    }
    
    const payload = {
      organizationId: orgId,
      name: request.concept,
      description: request.description,
      formulaExpression: request.formula
    };

    return this.http.post<PayrollFormula>(`${this.apiUrl}/payroll-formulas?organizationId=${orgId}`, payload);
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
