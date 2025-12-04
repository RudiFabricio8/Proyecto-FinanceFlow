import { Component} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PayrollService, PayrollPeriod } from '../../core/services/payroll.service';
import { ExportService } from '../../core/services/export';

interface CompliancePeriod {
  id: string;
  period: string;
  status: 'DRAFT' | 'PROCESSED' | 'PAUSED';
  lastModified: string;
  complianceScore: number;
  startDate: string;
  endDate: string;
}

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.html',
  styleUrl: './admin.scss'
})
export class Admin {
  periods: CompliancePeriod[] = [];
  selectedPeriodId: string | null = null;
  loading = false;
  filterText = '';

  constructor(
    private payrollService: PayrollService,
    private exportService: ExportService
  ) {
    this.loadPeriods();
  }

  loadPeriods(): void {
    this.loading = true;
    this.payrollService.listPeriods().subscribe({
      next: (apiPeriods) => {
        this.periods = apiPeriods.map(p => this.mapToCompliancePeriod(p));
        if (this.periods.length > 0) {
          this.selectedPeriodId = this.periods[0].id;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading periods', err);
        this.loading = false;
      }
    });
  }

  mapToCompliancePeriod(apiPeriod: PayrollPeriod): CompliancePeriod {
    let complianceScore = 0;
    
    switch (apiPeriod.status) {
      case 'PROCESSED':
        complianceScore = 95;
        break;
      case 'DRAFT':
        complianceScore = 70;
        break;
      case 'PAUSED':
        complianceScore = 80;
        break;
    }

    return {
      id: apiPeriod.id,
      period: apiPeriod.period,
      status: apiPeriod.status,
      lastModified: apiPeriod.updatedAt,
      complianceScore,
      startDate: apiPeriod.startDate,
      endDate: apiPeriod.endDate
    };
  }

  selectPeriod(periodId: string): void {
    this.selectedPeriodId = periodId;
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PROCESSED': return 'status-approved';
      case 'DRAFT': return 'status-draft';
      case 'PAUSED': return 'status-paused';
      default: return '';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'PROCESSED': return 'Aprobado';
      case 'DRAFT': return 'Borrador';
      case 'PAUSED': return 'Procesado';
      default: return status;
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('es-ES', { 
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    });
  }

  get filteredPeriods(): CompliancePeriod[] {
    if (!this.filterText) return this.periods;
    
    return this.periods.filter(p => 
      p.period.toLowerCase().includes(this.filterText.toLowerCase())
    );
  }

  exportCompliance(): void {
    const data = this.periods.map(p => ({
      'Período': p.period,
      'Estado': this.getStatusLabel(p.status),
      'Última Modificación': this.formatDate(p.lastModified),
      'Puntuación Cumplimiento': `${p.complianceScore}%`,
      'Fecha Inicio': p.startDate,
      'Fecha Fin': p.endDate
    }));

    this.exportService.exportToExcel(data, 'Reporte_Cumplimiento');
  }
}