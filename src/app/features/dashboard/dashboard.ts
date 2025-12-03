// src/app/features/dashboard/dashboard.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExportService } from '../../core/services/export';
import { StatCard } from '../../core/models/stat-card.model';
import { Transaction } from '../../core/models/transaction.model';
import { TransactionService } from '../../core/services/transaction.service';
import { StatCardComponent } from './components/stat-card/stat-card';
import { ChartPlaceholderComponent } from './components/chart-placeholder/chart-placeholder';
import { TransactionsTableComponent } from './components/transactions-table/transactions-table';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
  standalone: true,
  imports: [
    CommonModule,
    StatCardComponent,
    ChartPlaceholderComponent,
    TransactionsTableComponent
  ]
})
export class Dashboard implements OnInit {
  statCards: StatCard[] = [
    {
      title: 'Ingresos totales',
      value: '$0',
      change: '0%',
      isPositive: true
    },
    {
      title: 'Gastos totales',
      value: '$0',
      change: '0%',
      isPositive: false
    },
    {
      title: 'Balance',
      value: '$0',
      change: '0%',
      isPositive: true
    }
  ];

  transactions: Transaction[] = [];

  constructor(
    private exportService: ExportService,
    private transactionService: TransactionService
  ) {}

  ngOnInit(): void {
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.transactionService.getTransactions().subscribe({
      next: (data) => {
        this.transactions = data;
        this.calculateStats();
      },
      error: (err) => console.error('Error loading transactions', err)
    });
  }

  calculateStats(): void {
    const income = this.transactions
      .filter(t => t.type === 'INCOME')
      .reduce((acc, t) => acc + t.amount, 0);
    
    const expenses = this.transactions
      .filter(t => t.type === 'EXPENSE')
      .reduce((acc, t) => acc + t.amount, 0);

    const balance = income - expenses;

    this.statCards[0].value = `$${income.toFixed(2)}`;
    this.statCards[1].value = `$${expenses.toFixed(2)}`;
    this.statCards[2].value = `$${balance.toFixed(2)}`;
  }

  handleExport(): void {
    console.log('Exportando datos...');
    
    const choice = confirm('¿Desea exportar a Excel? (Aceptar = Excel, Cancelar = PDF)');
    
    if (choice) {
      this.exportService.exportTransactionsToExcel(this.transactions as any);
    } else {
      this.exportService.exportTransactionsToPDF(this.transactions as any);
    }
  }
}