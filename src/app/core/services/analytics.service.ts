import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StorageService } from './storage.service';

export interface PeriodBreakdown {
  period: string;
  grossSalary: string;
  deductions: string;
  netSalary: string;
  status: string;
}

export interface StatusSummary {
  draft: number;
  processed: number;
  paused: number;
}

export interface AnalyticsOverview {
  totalCost: string;
  totalGrossSalary: string;
  totalDeductions: string;
  periodBreakdown: PeriodBreakdown[];
  statusSummary: StatusSummary;
}

export interface TimeseriesDataPoint {
  date: string;
  grossSalary: string;
  deductions: string;
  netSalary: string;
  status: string;
}

export interface TimeseriesResponse {
  series: TimeseriesDataPoint[];
}

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private apiUrl = `${environment.apiUrl}/analytics`;

  constructor(
    private http: HttpClient,
    private storageService: StorageService
  ) {}

  getOverview(): Observable<AnalyticsOverview> {
    const user = this.storageService.getUser();
    if (!user?.organizationId) {
      console.warn('Organization ID not found - please re-login');
      return of({
        totalCost: '0',
        totalGrossSalary: '0',
        totalDeductions: '0',
        periodBreakdown: [],
        statusSummary: { draft: 0, processed: 0, paused: 0 }
      });
    }
    const params = new HttpParams().set('organizationId', user.organizationId);
    return this.http.get<AnalyticsOverview>(`${this.apiUrl}/overview`, { params });
  }

  getTimeseries(): Observable<TimeseriesResponse> {
    const user = this.storageService.getUser();
    if (!user?.organizationId) {
      console.warn('Organization ID not found - please re-login');
      return of({ series: [] });
    }
    const params = new HttpParams().set('organizationId', user.organizationId);
    return this.http.get<TimeseriesResponse>(`${this.apiUrl}/timeseries`, { params });
  }
}
