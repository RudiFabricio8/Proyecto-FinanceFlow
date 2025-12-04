import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AnalyticsOverview {
  totalPayrollCost: number;
  employeeCount: number;
  turnoverRate: number;
  averageSalary: number;
}

export interface TimeseriesDataPoint {
  date: string;
  value: number;
}

export interface TimeseriesData {
  data: TimeseriesDataPoint[];
}

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private apiUrl = `${environment.apiUrl}/analytics`;

  constructor(private http: HttpClient) {}

  getOverview(): Observable<AnalyticsOverview> {
    return this.http.get<AnalyticsOverview>(`${this.apiUrl}/overview`);
  }

  getTimeseries(metric: string, startDate?: string, endDate?: string): Observable<TimeseriesData> {
    let params = new HttpParams().set('metric', metric);
    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }
    return this.http.get<TimeseriesData>(`${this.apiUrl}/timeseries`, { params });
  }
}
