// src/app/features/dashboard/dashboard.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService, DashboardSummary } from '../../core/services/dashboard.service';
import { Transaction } from '../../core/models/transaction.model';
import { StatCard } from '../../core/models/stat-card.model';
import { StatCardComponent } from './components/stat-card/stat-card';
import { ChartPlaceholderComponent } from './components/chart-placeholder/chart-placeholder';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
  standalone: true,
  imports: [
    CommonModule,
    StatCardComponent,
    ChartPlaceholderComponent
  ]
})
export class Dashboard implements OnInit {
  statCards: StatCard[] = [];
  recentTransactions: Transaction[] = [];
  loading = true;
  loadingTransactions = true;
  error = '';

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loadDashboardSummary();
    this.loadRecentTransactions();
  }

  formatSignedAmount(amount: number): string {
    const sign = amount >= 0 ? '+' : '-';
    return `${sign}$${Math.abs(amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  }

  loadDashboardSummary(): void {
    this.loading = true;
    this.dashboardService.getSummary().subscribe({
      next: (summary: DashboardSummary) => {
        this.updateStatCards(summary);
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading dashboard summary', err);
        this.error = 'Error al cargar el resumen del dashboard';
        this.loading = false;
        this.setDefaultStatCards();
      }
    });
  }

  loadRecentTransactions(): void {
    this.loadingTransactions = true;
    this.dashboardService.getRecentTransactions(10).subscribe({
      next: (transactions) => {
        this.recentTransactions = transactions;
        this.loadingTransactions = false;
      },
      error: (err) => {
        console.error('Error loading transactions', err);
        this.loadingTransactions = false;
      }
    });
  }

  updateStatCards(summary: DashboardSummary): void {
    this.statCards = [
      {
        title: 'Total Revenue',
        value: `$${this.formatNumber(summary.totalRevenue)}`,
        change: summary.revenueChange,
        isPositive: summary.revenueChange.includes('+')
      },
      {
        title: 'Payroll Expenses',
        value: `$${this.formatNumber(summary.payrollExpenses)}`,
        change: summary.expensesChange,
        isPositive: summary.expensesChange.includes('-')
      },
      {
        title: 'Outstanding Invoices',
        value: `$${this.formatNumber(summary.outstandingInvoices)}`,
        change: summary.invoicesChange,
        isPositive: summary.invoicesChange.includes('+')
      },
      {
        title: 'Employee Satisfaction',
        value: `${summary.employeeSatisfaction}%`,
        change: summary.satisfactionChange,
        isPositive: summary.satisfactionChange.includes('+')
      }
    ];
  }

  setDefaultStatCards(): void {
    this.statCards = [
      {
        title: 'Total Revenue',
        value: '$0',
        change: '0%',
        isPositive: true
      },
      {
        title: 'Payroll Expenses',
        value: '$0',
        change: '0%',
        isPositive: true
      },
      {
        title: 'Outstanding Invoices',
        value: '$0',
        change: '0%',
        isPositive: true
      },
      {
        title: 'Employee Satisfaction',
        value: '0%',
        change: '0%',
        isPositive: true
      }
    ];
  }

  formatNumber(num: number): string {
    return num.toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  getCategoryClass(category: string): string {
    switch (category) {
      case 'Revenue': return 'category-revenue';
      case 'Expenses': return 'category-expenses';
      case 'Payroll': return 'category-payroll';
      default: return '';
    }
  }

  getAmountClass(amount: number): string {
    return amount >= 0 ? 'amount-positive' : 'amount-negative';
  }

  exportTransactions(): void {
    console.log('Exporting transactions...');
    // TODO: Implement Excel/PDF export
    alert('Export functionality will be implemented soon');
  }
}