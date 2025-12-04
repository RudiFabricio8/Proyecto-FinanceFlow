import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ComplianceReport {
  organizationId: string;
  periodId: string;
  generatedAt: string;
  totalPayroll: number;
  totalWithholdings: number;
  complianceStatus: string;
  issues: string[];
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  getComplianceReport(periodId?: string): Observable<ComplianceReport> {
    let params = new HttpParams();
    if (periodId) {
      params = params.set('periodId', periodId);
    }
    return this.http.get<ComplianceReport>(`${this.apiUrl}/compliance-report`, { params });
  }
}
