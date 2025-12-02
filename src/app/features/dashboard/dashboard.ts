// src/app/features/dashboard/dashboard.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExportService } from '../../core/services/export';
import { StatCard } from '../../core/models/stat-card.model';
import { Transaction } from '../../core/models/transaction.model';
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
      value: '$250,000',
      change: '+15%',
      isPositive: true
    },
    {
      title: 'Gastos de nómina',
      value: '$75,000',
      change: '-5%',
      isPositive: false
    },
    {
      title: 'Facturas pendientes',
      value: '$12,500',
      change: '+10%',
      isPositive: true
    },
    {
      title: 'Satisfacción de empleados',
      value: '92%',
      change: '+2%',
      isPositive: true
    }
  ];

  transactions: Transaction[] = [
    {
      date: '26/07/2024',
      description: 'Pago factura #12345',
      category: 'Ingresos',
      amount: 5000.00,
      isPositive: true
    },
    {
      date: '25/07/2024',
      description: 'Suministros de oficina',
      category: 'Gastos',
      amount: 150.25,
      isPositive: false
    },
    {
      date: '24/07/2024',
      description: 'Nómina - Julio',
      category: 'Nómina',
      amount: 15000.00,
      isPositive: false
    },
    {
      date: '23/07/2024',
      description: 'Suscripción de software',
      category: 'Gastos',
      amount: 99.00,
      isPositive: false
    }
  ];

  constructor(private exportService: ExportService) {}

  ngOnInit(): void {
    // Inicialización del componente
  }

  handleExport(): void {
    console.log('Exportando datos...');
    
    const choice = confirm('¿Desea exportar a Excel? (Aceptar = Excel, Cancelar = PDF)');
    
    if (choice) {
      this.exportService.exportTransactionsToExcel(this.transactions);
    } else {
      this.exportService.exportTransactionsToPDF(this.transactions);
    }
  }
}