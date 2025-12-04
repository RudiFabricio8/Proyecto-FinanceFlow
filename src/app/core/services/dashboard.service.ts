import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Transaction, CreateTransactionRequest } from '../models/transaction.model';

export interface DashboardSummary {
  totalRevenue: number;
  payrollExpenses: number;
  outstandingInvoices: number;
  employeeSatisfaction: number;
  revenueChange: string;
  expensesChange: string;
  invoicesChange: string;
  satisfactionChange: string;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private apiUrl = `${environment.apiUrl}/dashboard`;
  private transactionsUrl = `${environment.apiUrl}/transactions`;

  // No in-memory mock transactions: views should render design-only until real data is provided.
  // TODO: Replace with real API-backed storage or implement development fixtures behind a feature flag.

  constructor(private http: HttpClient) {}

  getSummary(): Observable<DashboardSummary> {
    // Calculate summary from transactions
    return this.getRecentTransactions().pipe(
      map(transactions => {
        const totalRevenue = transactions
          .filter(t => t.category === 'Revenue')
          .reduce((sum, t) => sum + t.amount, 0);
        
        const payrollExpenses = Math.abs(transactions
          .filter(t => t.category === 'Payroll')
          .reduce((sum, t) => sum + t.amount, 0));
        
        const expenses = Math.abs(transactions
          .filter(t => t.category === 'Expenses')
          .reduce((sum, t) => sum + t.amount, 0));

        return {
          totalRevenue,
          payrollExpenses,
          outstandingInvoices: 12500,
          employeeSatisfaction: 92,
          revenueChange: '+15%',
          expensesChange: '-5%',
          invoicesChange: '+10%',
          satisfactionChange: '+2%'
        };
      })
    );
  }

  getRecentTransactions(limit: number = 10): Observable<Transaction[]> {
    // Return empty transactions list by default so the UI displays layout only.
    // TODO: Implement API call to fetch recent transactions.
    return of([] as Transaction[]);
  }

  createTransaction(request: CreateTransactionRequest): Observable<Transaction> {
    const newTransaction: Transaction = {
      id: Date.now().toString(),
      ...request,
      createdAt: new Date().toISOString()
    };

    // NOTE: This returns the created transaction but does not persist locally.
    // TODO: Implement persistence on backend and return the persisted resource.
    return of(newTransaction);
  }

  // Method to create transaction from document
  createTransactionFromDocument(documentData: {
    title: string;
    amount?: number;
    date?: string;
    category?: string;
    documentId: string;
  }): Observable<Transaction> {
    const category = this.inferCategory(documentData.category || documentData.title);
    
    const transactionRequest: CreateTransactionRequest = {
      date: documentData.date || new Date().toISOString().split('T')[0],
      description: documentData.title,
      category,
      amount: documentData.amount ? (category === 'Revenue' ? documentData.amount : -documentData.amount) : 0,
      documentId: documentData.documentId
    };

    return this.createTransaction(transactionRequest);
  }

  private inferCategory(text: string): 'Revenue' | 'Expenses' | 'Payroll' {
    const lowerText = text.toLowerCase();
    
    if (lowerText.includes('invoice') || lowerText.includes('payment') || lowerText.includes('ingreso')) {
      return 'Revenue';
    }
    
    if (lowerText.includes('payroll') || lowerText.includes('nómina') || lowerText.includes('salary')) {
      return 'Payroll';
    }
    
    return 'Expenses';
  }
}
