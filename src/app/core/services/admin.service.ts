import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StorageService } from './storage.service';

export interface PeriodSummary {
  id: string;
  name: string;
  status: string;
  lastModified: string;
  complianceScore: number;
}

export interface DailyComplianceRecord {
  date: string;
  periodsCount: number;
  status: string;
  totalAmount: string;
}

export interface ComplianceSummary {
  totalPeriods: number;
  processedCount: number;
  draftCount: number;
  pausedCount: number;
}

export interface ComplianceResponse {
  year: number;
  month: number;
  dailyRecords: DailyComplianceRecord[];
  periods: PeriodSummary[];
  summary: ComplianceSummary;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(
    private http: HttpClient,
    private storageService: StorageService
  ) {}

  getComplianceData(year?: number, month?: number): Observable<ComplianceResponse> {
    const user = this.storageService.getUser();
    if (!user?.organizationId) {
      console.warn('Organization ID not found - please re-login');
      return of({
        year: new Date().getFullYear(),
        month: new Date().getMonth() + 1,
        dailyRecords: [],
        periods: [],
        summary: { totalPeriods: 0, processedCount: 0, draftCount: 0, pausedCount: 0 }
      });
    }

    let params = new HttpParams().set('organizationId', user.organizationId);
    if (year) params = params.set('year', year);
    if (month) params = params.set('month', month);

    return this.http.get<ComplianceResponse>(`${this.apiUrl}/compliance`, { params });
  }

  updatePeriodStatus(id: string, status: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/periods/${id}/status`, { status });
  }

  updateComplianceScore(id: string, score: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/periods/${id}/score`, { score });
  }

  exportCompliance(): Observable<any> {
    const user = this.storageService.getUser();
    if (!user?.organizationId) {
      console.warn('Organization ID not found - please re-login');
      return of(null);
    }
    const params = new HttpParams().set('organizationId', user.organizationId);
    return this.http.get(`${this.apiUrl}/compliance/export`, { params });
  }
}
