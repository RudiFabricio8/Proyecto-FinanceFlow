import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Transaction } from '../models/transaction.model';

export interface DashboardSummary {
  totalRevenue: number;
  payrollExpenses: number;
  outstandingInvoices: number;
  employeeSatisfaction: number;
  revenueChange: string; // eg "+15%"
  expensesChange: string; // eg "-5%"
  invoicesChange: string; // eg "+10%"
  satisfactionChange: string; // eg "+2%"
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  getSummary(): Observable<DashboardSummary> {
    // Return mock data until backend endpoint is ready
    return of({
      totalRevenue: 250000,
      payrollExpenses: 75000,
      outstandingInvoices: 12500,
      employeeSatisfaction: 92,
      revenueChange: '+15%',
      expensesChange: '-5%',
      invoicesChange: '+10%',
      satisfactionChange: '+2%'
    });
  }

  getRecentTransactions(limit: number = 10): Observable<Transaction[]> {
    // Return mock data - will be replaced with real API call
    const mockTransactions: Transaction[] = [
      {
        id: '1',
        date: '2024-07-26',
        description: 'Invoice #12345 Payment',
        category: 'Revenue',
        amount: 5000.00,
        createdAt: new Date().toISOString()
      },
      {
        id: '2',
        date: '2024-07-25',
        description: 'Office Supplies',
        category: 'Expenses',
        amount: -150.25,
        createdAt: new Date().toISOString()
      },
      {
        id: '3',
        date: '2024-07-24',
        description: 'Payroll - July',
        category: 'Payroll',
        amount: -15000.00,
        createdAt: new Date().toISOString()
      },
      {
        id: '4',
        date: '2024-07-23',
        description: 'Software Subscription',
        category: 'Expenses',
        amount: -99.00,
        createdAt: new Date().toISOString()
      }
    ];

    return of(mockTransactions.slice(0, limit));
  }
}
