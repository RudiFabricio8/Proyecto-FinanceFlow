import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, PeriodSummary } from '../../core/services/admin.service';
import { ExportService } from '../../core/services/export';

interface CompliancePeriod {
  id: string;
  period: string;
  status: string;
  lastModified: string;
  complianceScore: number;
  startDate?: string;
  endDate?: string;
}

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.html',
  styleUrls: ['./admin.scss']
})
export class Admin {
  periods: CompliancePeriod[] = [];
  selectedPeriodId: string | null = null;
  loading = false;
  filterText = '';

  constructor(
    private adminService: AdminService,
    private exportService: ExportService
  ) {
    this.loadPeriods();
  }

  loadPeriods(): void {
    this.loading = true;
    this.adminService.getComplianceData().subscribe({
      next: (response) => {
        this.periods = response.periods.map(p => ({
          id: p.id,
          period: p.name,
          status: p.status,
          lastModified: p.lastModified,
          complianceScore: p.complianceScore
        }));
        
        if (this.periods.length > 0 && !this.selectedPeriodId) {
          this.selectedPeriodId = this.periods[0].id;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading compliance data', err);
        this.loading = false;
      }
    });
  }

  selectPeriod(periodId: string): void {
    this.selectedPeriodId = periodId;
  }

  updateStatus(period: CompliancePeriod, newStatus: string): void {
    this.adminService.updatePeriodStatus(period.id, newStatus).subscribe({
      next: () => {
        period.status = newStatus;
        period.lastModified = new Date().toISOString();
      },
      error: (err) => console.error('Error updating status', err)
    });
  }

  updateScore(period: CompliancePeriod, newScore: number): void {
    this.adminService.updateComplianceScore(period.id, newScore).subscribe({
      next: () => {
        period.complianceScore = newScore;
        period.lastModified = new Date().toISOString();
      },
      error: (err) => console.error('Error updating score', err)
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PROCESSED': return 'status-approved'; // Green
      case 'APPROVED': return 'status-approved';
      case 'DRAFT': return 'status-draft';       // Yellow
      case 'PENDING': return 'status-paused';    // Blue/Orange
      case 'PAUSED': return 'status-paused';
      case 'CANCELLED': return 'status-canceled'; // Red
      default: return '';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'PROCESSED': return 'Procesado';
      case 'APPROVED': return 'Aprobado';
      case 'DRAFT': return 'Borrador';
      case 'PENDING': return 'Pendiente';
      case 'PAUSED': return 'Pausado';
      case 'CANCELLED': return 'Cancelado';
      default: return status;
    }
  }

  formatDate(dateString: string): string {
    if (!dateString) return '-';
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
      'Puntuación Cumplimiento': `${p.complianceScore}%`
    }));

    this.exportService.exportToExcel(data, 'Reporte_Cumplimiento');
  }
}