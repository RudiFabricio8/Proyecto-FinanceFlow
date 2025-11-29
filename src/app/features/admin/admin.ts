// src/app/features/admin/admin.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PayrollPeriod, DailyPayrollRecord, ComplianceStatus } from '../../core/models/payroll-period.model';
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
  periods: PayrollPeriod[] = [
    { id: '1', month: 'Julio', year: 2024, isActive: true },
    { id: '2', month: 'Junio', year: 2024, isActive: false },
    { id: '3', month: 'Mayo', year: 2024, isActive: false },
    { id: '4', month: 'Abril', year: 2024, isActive: false }
  ];

  selectedPeriod: PayrollPeriod = this.periods[0];
  dailyRecords: DailyPayrollRecord[] = [];

  constructor(private exportService: ExportService) {}

  ngOnInit(): void {
    this.loadDailyRecords(this.selectedPeriod);
  }

  onPeriodSelected(period: PayrollPeriod): void {
    this.periods.forEach(p => p.isActive = p.id === period.id);
    this.selectedPeriod = period;
    this.loadDailyRecords(period);
  }

  loadDailyRecords(period: PayrollPeriod): void {
    const daysInMonth = this.getDaysInMonth(period.year, period.month);
    this.dailyRecords = [];

    for (let day = 1; day <= daysInMonth; day++) {
      const dayStr = day.toString().padStart(2, '0');
      this.dailyRecords.push({
        id: `${period.id}-${day}`,
        date: `${dayStr} ${period.month} ${period.year}`,
        status: this.getRandomStatus(),
        lastModified: `${period.year}-${this.getMonthNumber(period.month)}-${dayStr} ${this.getRandomTime()}`,
        complianceScore: Math.floor(Math.random() * 30) + 70
      });
    }
  }

  getDaysInMonth(year: number, monthName: string): number {
    const monthNumber = this.getMonthNumber(monthName);
    return new Date(year, monthNumber, 0).getDate();
  }

  getMonthNumber(monthName: string): number {
    const months: { [key: string]: number } = {
      'Enero': 1, 'Febrero': 2, 'Marzo': 3, 'Abril': 4,
      'Mayo': 5, 'Junio': 6, 'Julio': 7, 'Agosto': 8,
      'Septiembre': 9, 'Octubre': 10, 'Noviembre': 11, 'Diciembre': 12
    };
    return months[monthName];
  }

  getRandomStatus(): ComplianceStatus {
    const statuses: ComplianceStatus[] = ['approved', 'processed', 'draft', 'cancelled'];
    return statuses[Math.floor(Math.random() * statuses.length)];
  }

  getRandomTime(): string {
    const hour = Math.floor(Math.random() * 12) + 1;
    const minute = Math.floor(Math.random() * 60);
    const ampm = Math.random() > 0.5 ? 'AM' : 'PM';
    return `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} ${ampm}`;
  }

  onStatusChange(data: { id: string; status: ComplianceStatus }): void {
    const record = this.dailyRecords.find(r => r.id === data.id);
    if (record) {
      record.status = data.status;
      console.log(`Estado actualizado para ${record.date}: ${data.status}`);
    }
  }

  onExport(): void {
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