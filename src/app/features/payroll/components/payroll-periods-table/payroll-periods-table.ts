// src/app/features/payroll/components/payroll-periods-table/payroll-periods-table.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PayrollPeriodData } from '../../../../core/models/payroll.model';

@Component({
  selector: 'app-payroll-periods-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payroll-periods-table.html',
  styleUrl: './payroll-periods-table.scss'
})
export class PayrollPeriodsTableComponent {
  @Input() periods: PayrollPeriodData[] = [];
  @Output() exportData = new EventEmitter<void>();

  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      'draft': 'status-draft',
      'pending': 'status-pending',
      'approved': 'status-approved',
      'processed': 'status-processed'
    };
    return classes[status] || '';
  }

  getStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      'draft': 'Borrador',
      'pending': 'Pendiente',
      'approved': 'Aprobado',
      'processed': 'Procesado'
    };
    return labels[status] || status;
  }

  onExport(): void {
    this.exportData.emit();
  }
}