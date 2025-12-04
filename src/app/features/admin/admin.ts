// src/app/features/admin/admin.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PayrollPeriod as UiPayrollPeriod, DailyPayrollRecord, ComplianceStatus } from '../../core/models/payroll-period.model';
import { PayrollService } from '../../core/services/payroll.service';
import { PayrollPeriod as ApiPayrollPeriod, PayrollCalculation } from '../../core/models/payroll.model';
import { PeriodSelectorComponent } from './components/period-selector/period-selector';
import { ComplianceTableComponent } from './components/compliance-table/compliance-table';
import { ExportService } from '../../core/services/export';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, PeriodSelectorComponent, ComplianceTableComponent],
  templateUrl: './admin.html',
  styleUrl: './admin.scss'
})
export class Admin implements OnInit {
  periods: UiPayrollPeriod[] = [];
  selectedPeriod: UiPayrollPeriod | null = null;
  dailyRecords: DailyPayrollRecord[] = [];
  loading = false;

  constructor(
    private payrollService: PayrollService,
    private exportService: ExportService
  ) {}

  ngOnInit(): void {
    this.loadPeriods();
  }

  loadPeriods(): void {
    this.loading = true;
    this.payrollService.listPeriods().subscribe({
      next: (apiPeriods) => {
        this.periods = apiPeriods.map(p => this.mapToUiPeriod(p));
        if (this.periods.length > 0) {
          this.onPeriodSelected(this.periods[0]);
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading periods', err);
        this.loading = false;
      }
    });
  }

  onPeriodSelected(period: UiPayrollPeriod): void {
    this.periods.forEach(p => p.isActive = p.id === period.id);
    this.selectedPeriod = period;
    this.loadDailyRecords(period);
  }

  loadDailyRecords(period: UiPayrollPeriod): void {
    // Ideally we filter by periodId, but listCalculations might return all or we need to filter client-side
    // if the API doesn't support filtering by periodId yet (it does in our implementation plan but let's check service)
    // The service has listCalculations(periodId?: string).
    
    this.payrollService.listCalculations(period.id).subscribe({
      next: (calculations) => {
        this.dailyRecords = calculations.map(c => this.mapToDailyRecord(c));
      },
      error: (err) => console.error('Error loading calculations', err)
    });
  }

  private mapToUiPeriod(apiPeriod: ApiPayrollPeriod): UiPayrollPeriod {
    const date = new Date(apiPeriod.startDate);
    const monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
    
    return {
      id: apiPeriod.id,
      month: monthNames[date.getMonth()],
      year: date.getFullYear(),
      isActive: false
    };
  }

  private mapToDailyRecord(calc: PayrollCalculation): DailyPayrollRecord {
    const date = new Date(calc.executedAt);
    const dayStr = date.getDate().toString().padStart(2, '0');
    const monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
    
    return {
      id: calc.id,
      date: `${dayStr} ${monthNames[date.getMonth()]} ${date.getFullYear()}`,
      status: 'processed', // Default status as calculations are usually processed
      lastModified: date.toLocaleString(),
      complianceScore: 100 // Mock score as we don't have it in calculation
    };
  }

  onStatusChange(data: { id: string; status: ComplianceStatus }): void {
    const record = this.dailyRecords.find(r => r.id === data.id);
    if (record) {
      record.status = data.status;
      console.log(`Estado actualizado para ${record.date}: ${data.status}`);
    }
  }

  onExport(): void {
    if (!this.selectedPeriod) return;
    
    console.log('Exportando datos de:', this.selectedPeriod.month, this.selectedPeriod.year);
    
    const dataToExport = this.dailyRecords.map(r => ({
      'Fecha': r.date,
      'Estado': r.status,
      'Última Modificación': r.lastModified,
      'Cumplimiento': `${r.complianceScore}%`
    }));

    this.exportService.exportToExcel(dataToExport, `Nomina_${this.selectedPeriod.month}_${this.selectedPeriod.year}`);
  }
}