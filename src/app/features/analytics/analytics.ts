// src/app/features/analytics/analytics.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalyticsChapter } from '../../core/models/analytics.model';
import { AnalyticsService, AnalyticsOverview, TimeseriesDataPoint } from '../../core/services/analytics.service';
import { ChapterAccordion } from './components/chapter-accordion/chapter-accordion';
import { AnalyticsChartComponent } from './components/analytics-chart/analytics-chart.component';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, ChapterAccordion, AnalyticsChartComponent],
  templateUrl: './analytics.html',
  styleUrls: ['./analytics.scss']
})
export class Analytics implements OnInit {
  chapters: AnalyticsChapter[] = [];
  overview: AnalyticsOverview | null = null;
  timeseriesData: TimeseriesDataPoint[] = [];
  loading = true;
  error = '';

  // Chart data
  costChartLabels: string[] = [];
  costChartData: number[] = [];
  statusChartLabels: string[] = ['Procesados', 'Borradores', 'Pausados'];
  statusChartData: number[] = [];
  timeseriesLabels: string[] = [];
  timeseriesGrossData: number[] = [];

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.initChapters();
    this.loadAnalytics();
  }

  private initChapters(): void {
    this.chapters = [
      {
        id: '1',
        title: 'Capítulo 1: Entendiendo el Costo Total de Nómina',
        description: 'El costo total de nómina es un indicador fundamental para la salud financiera de la empresa. Aquí, desglosamos las cifras que componen este gasto vital.',
        isExpanded: true
      },
      {
        id: '2',
        title: 'Capítulo 2: El Balance entre Deducciones y Percepciones',
        description: 'Comprender la relación entre lo que los empleados ganan y lo que se les retiene es vital para asegurar la transparencia y el cumplimiento.',
        isExpanded: false
      },
      {
        id: '3',
        title: 'Capítulo 3: Eficiencia y Cumplimiento en la Gestión de Nómina',
        description: 'La eficiencia en el procesamiento de la nómina y el cumplimiento normativo son esenciales para evitar errores y sanciones.',
        isExpanded: false
      }
    ];
  }

  private loadAnalytics(): void {
    this.loading = true;
    
    // Load overview data
    this.analyticsService.getOverview().subscribe({
      next: (data) => {
        this.overview = data;
        this.updateChartData();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading analytics overview', err);
        this.error = 'Error al cargar datos analíticos';
        this.loading = false;
      }
    });

    // Load timeseries data for charts
    this.analyticsService.getTimeseries().subscribe({
      next: (response) => {
        this.timeseriesData = response.series;
        this.updateTimeseriesChart();
      },
      error: (err) => {
        console.error('Error loading timeseries data', err);
      }
    });
  }

  private updateChartData(): void {
    if (!this.overview) return;

    // Chapter 1: Cost breakdown chart
    this.costChartLabels = ['Salario Bruto', 'Deducciones', 'Neto'];
    this.costChartData = [
      parseFloat(this.overview.totalGrossSalary) || 0,
      parseFloat(this.overview.totalDeductions) || 0,
      (parseFloat(this.overview.totalGrossSalary) || 0) - (parseFloat(this.overview.totalDeductions) || 0)
    ];

    // Chapter 3: Status distribution chart
    this.statusChartData = [
      this.overview.statusSummary.processed,
      this.overview.statusSummary.draft,
      this.overview.statusSummary.paused
    ];
  }

  private updateTimeseriesChart(): void {
    if (!this.timeseriesData || this.timeseriesData.length === 0) {
      // Use overview period breakdown if no timeseries
      if (this.overview?.periodBreakdown) {
        this.timeseriesLabels = this.overview.periodBreakdown.map(p => p.period);
        this.timeseriesGrossData = this.overview.periodBreakdown.map(p => parseFloat(p.grossSalary) || 0);
      }
      return;
    }

    this.timeseriesLabels = this.timeseriesData.map(d => {
      const date = new Date(d.date);
      return date.toLocaleDateString('es-MX', { month: 'short', day: 'numeric' });
    });
    this.timeseriesGrossData = this.timeseriesData.map(d => parseFloat(d.grossSalary) || 0);
  }

  toggleChapter(id: string): void {
    this.chapters = this.chapters.map(ch => ({
      ...ch,
      isExpanded: ch.id === id ? !ch.isExpanded : false
    }));
  }

  // Helper methods for displaying data
  formatCurrency(value: string): string {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN'
    }).format(parseFloat(value));
  }

  getStatusPercentage(status: 'draft' | 'processed' | 'paused'): number {
    if (!this.overview) return 0;
    const total = this.overview.statusSummary.draft + 
                  this.overview.statusSummary.processed + 
                  this.overview.statusSummary.paused;
    if (total === 0) return 0;
    return Math.round((this.overview.statusSummary[status] / total) * 100);
  }
}