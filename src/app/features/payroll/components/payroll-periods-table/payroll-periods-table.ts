// src/app/features/payroll/components/payroll-periods-table/payroll-periods-table.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PayrollPeriod } from '../../../../core/models/payroll.model';

@Component({
  selector: 'app-payroll-periods-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payroll-periods-table.html',
  styleUrl: './payroll-periods-table.scss'
})
export class PayrollPeriodsTableComponent {
  @Input() periods: PayrollPeriod[] = [];
  @Output() exportData = new EventEmitter<void>();

  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      'DRAFT': 'status-draft',
      'PROCESSED': 'status-processed',
      'PAUSED': 'status-paused'
    };
    return classes[status] || '';
  }

  getStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      'DRAFT': 'Borrador',
      'PROCESSED': 'Procesado',
      'PAUSED': 'Pausado'
    };
    return labels[status] || status;
  }

  onExport(): void {
    this.exportData.emit();
  }
}