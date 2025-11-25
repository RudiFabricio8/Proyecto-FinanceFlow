// src/app/features/admin/components/compliance-row/compliance-row.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DailyPayrollRecord, ComplianceStatus } from '../../../../core/models/payroll-period.model';

@Component({
  selector: '[app-compliance-row]',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './compliance-row.html',
  styleUrl: './compliance-row.scss'
})
export class ComplianceRowComponent {
  @Input() record!: DailyPayrollRecord;
  @Output() statusChange = new EventEmitter<{ id: string; status: ComplianceStatus }>();

  statuses: ComplianceStatus[] = ['approved', 'processed', 'draft', 'cancelled'];

  onStatusChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const newStatus = select.value as ComplianceStatus;
    this.statusChange.emit({ id: this.record.id, status: newStatus });
  }

  getStatusConfig(): { label: string; icon: string; class: string } {
    const configs = {
      approved: { label: 'Aprobado', icon: 'check_circle', class: 'status-approved' },
      processed: { label: 'Procesado', icon: 'sync', class: 'status-processed' },
      draft: { label: 'Borrador', icon: 'edit_note', class: 'status-draft' },
      cancelled: { label: 'Cancelado', icon: 'cancel', class: 'status-cancelled' }
    };
    return configs[this.record.status];
  }

  getStatusLabel(status: ComplianceStatus): string {
    const labels = {
      approved: 'Aprobado',
      processed: 'Procesado',
      draft: 'Borrador',
      cancelled: 'Cancelado'
    };
    return labels[status];
  }
}