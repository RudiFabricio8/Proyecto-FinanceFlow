// src/app/features/dashboard/dashboard.ts
import { Component, OnInit, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService, DashboardSummary, ChartData } from '../../core/services/dashboard.service';
import { Transaction } from '../../core/models/transaction.model';
import { StatCard } from '../../core/models/stat-card.model';
import { StatCardComponent } from './components/stat-card/stat-card';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
  standalone: true,
  imports: [
    CommonModule,
    StatCardComponent
  ]
})
export class Dashboard implements OnInit, AfterViewInit {
  @ViewChild('revenueChart') revenueChartCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('expensesChart') expensesChartCanvas!: ElementRef<HTMLCanvasElement>;

  statCards: StatCard[] = [];
  recentTransactions: Transaction[] = [];
  loading = true;
  loadingTransactions = true;
  error = '';

  private revenueChart: Chart | null = null;
  private expensesChart: Chart | null = null;
  private chartData: { revenue: ChartData; expenses: ChartData } | null = null;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  ngAfterViewInit(): void {
    // Charts will be created after data loads
  }

  loadDashboardData(): void {
    this.loading = true;
    this.loadingTransactions = true;

    // Load summary
    this.dashboardService.getSummary().subscribe({
      next: (summary) => {
        this.updateStatCards(summary);
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading summary', err);
        this.setDefaultStatCards();
        this.loading = false;
      }
    });

    // Load transactions
    this.dashboardService.getRecentTransactions(10).subscribe({
      next: (transactions) => {
        this.recentTransactions = transactions;
        this.loadingTransactions = false;
      },
      error: (err) => {
        console.error('Error loading transactions', err);
        this.recentTransactions = [];
        this.loadingTransactions = false;
      }
    });

    // Load chart data
    this.dashboardService.getChartData().subscribe({
      next: (data) => {
        this.chartData = data;
        setTimeout(() => this.createCharts(), 100);
      },
      error: (err) => {
        console.error('Error loading chart data', err);
      }
    });
  }

  private createCharts(): void {
    if (!this.chartData) return;

    // Revenue Chart
    if (this.revenueChartCanvas?.nativeElement) {
      const ctx = this.revenueChartCanvas.nativeElement.getContext('2d');
      if (ctx) {
        if (this.revenueChart) this.revenueChart.destroy();
        this.revenueChart = new Chart(ctx, {
          type: 'bar',
          data: {
            labels: this.chartData.revenue.labels.length ? this.chartData.revenue.labels : ['Sin datos'],
            datasets: [{
              label: 'Ingresos',
              data: this.chartData.revenue.values.length ? this.chartData.revenue.values : [0],
              backgroundColor: '#6c5ce7',
              borderRadius: 8,
              barThickness: 40 // Fixed thickness to prevent squashing/expanding too much
            }]
          },
          options: this.getChartOptions('Ingresos por Período')
        });
      }
    }

    // Expenses Chart
    if (this.expensesChartCanvas?.nativeElement) {
      const ctx = this.expensesChartCanvas.nativeElement.getContext('2d');
      if (ctx) {
        if (this.expensesChart) this.expensesChart.destroy();
        this.expensesChart = new Chart(ctx, {
          type: 'line',
          data: {
            labels: this.chartData.expenses.labels.length ? this.chartData.expenses.labels : ['Sin datos'],
            datasets: [{
              label: 'Gastos',
              data: this.chartData.expenses.values.length ? this.chartData.expenses.values : [0],
              borderColor: '#e17055',
              backgroundColor: 'rgba(225, 112, 85, 0.1)',
              tension: 0.4,
              fill: true,
              pointRadius: 4,
              pointHoverRadius: 6
            }]
          },
          options: this.getChartOptions('Gastos por Período')
        });
      }
    }
  }

  private getChartOptions(title: string): any {
    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        title: {
          display: true,
          text: title,
          color: '#1f2937', // Dark color for visibility on white card
          font: { size: 16, weight: 'bold' },
          padding: { bottom: 20 }
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          grid: { color: 'rgba(0,0,0,0.05)' },
          ticks: { color: '#6b7280' }
        },
        x: {
          grid: { display: false },
          ticks: { color: '#6b7280' }
        }
      },
      interaction: {
        intersect: false,
        mode: 'index'
      }
    };
  }

  formatSignedAmount(amount: number): string {
    const sign = amount >= 0 ? '+' : '-';
    return `${sign}$${Math.abs(amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  }

  updateStatCards(summary: DashboardSummary): void {
    this.statCards = [
      {
        title: 'Total Revenue',
        value: `$${this.formatNumber(summary.totalRevenue)}`,
        change: summary.revenueChange,
        isPositive: true
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
      { title: 'Total Revenue', value: '$0', change: '0%', isPositive: true },
      { title: 'Payroll Expenses', value: '$0', change: '0%', isPositive: true },
      { title: 'Outstanding Invoices', value: '$0', change: '0%', isPositive: true },
      { title: 'Employee Satisfaction', value: '0%', change: '0%', isPositive: true }
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
    if (this.recentTransactions.length === 0) {
      alert('No hay transacciones para exportar');
      return;
    }

    // Define CSV headers
    const headers = ['Date', 'Description', 'Category', 'Amount', 'ID'];
    
    // Map data to CSV rows
    const rows = this.recentTransactions.map(t => [
      this.formatDate(t.date),
      `"${t.description.replace(/"/g, '""')}"`, // Escape quotes
      t.category,
      t.amount.toFixed(2),
      t.id
    ]);

    // Combine headers and rows
    const csvContent = [
      headers.join(','),
      ...rows.map(row => row.join(','))
    ].join('\n');

    // Create blob and download link
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `transactions_export_${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}