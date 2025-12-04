// src/app/features/dashboard/dashboard.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService, DashboardSummary } from '../../core/services/dashboard.service';
import { StatCard } from '../../core/models/stat-card.model';
import { StatCardComponent } from './components/stat-card/stat-card';
import { ChartPlaceholderComponent } from './components/chart-placeholder/chart-placeholder';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
  standalone: true,
  imports: [
    CommonModule,
    StatCardComponent,
    ChartPlaceholderComponent
  ]
})
export class Dashboard implements OnInit {
  statCards: StatCard[] = [];
  loading = true;
  error = '';

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loadDashboardSummary();
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
        // Show default values on error
        this.setDefaultStatCards();
      }
    });
  }

  updateStatCards(summary: DashboardSummary): void {
    this.statCards = [
      {
        title: 'Períodos Totales',
        value: summary.totalPeriods.toString(),
        change: `${summary.activePeriods} activos`,
        isPositive: summary.activePeriods > 0
      },
      {
        title: 'Fórmulas Creadas',
        value: summary.totalFormulas.toString(),
        change: 'Configuradas',
        isPositive: true
      },
      {
        title: 'Documentos Pendientes',
        value: summary.pendingDocuments.toString(),
        change: summary.pendingDocuments > 0 ? 'Requieren atención' : 'Al día',
        isPositive: summary.pendingDocuments === 0
      },
      {
        title: 'Notificaciones',
        value: summary.unreadNotifications.toString(),
        change: summary.unreadNotifications > 0 ? 'Sin leer' : 'Todo leído',
        isPositive: summary.unreadNotifications === 0
      }
    ];
  }

  setDefaultStatCards(): void {
    this.statCards = [
      {
        title: 'Períodos Totales',
        value: '0',
        change: '0 activos',
        isPositive: true
      },
      {
        title: 'Fórmulas Creadas',
        value: '0',
        change: 'Configuradas',
        isPositive: true
      },
      {
        title: 'Documentos Pendientes',
        value: '0',
        change: 'Al día',
        isPositive: true
      },
      {
        title: 'Notificaciones',
        value: '0',
        change: 'Todo leído',
        isPositive: true
      }
    ];
  }
}