import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="status-badge" [ngClass]="getClass()">{{ label }}</span>
  `,
  styleUrls: ['./status-badge.scss']
})
export class StatusBadgeComponent {
  @Input() status: string = '';
  @Input() label: string = '';

  getClass(): string {
    switch (this.status?.toLowerCase()) {
      case 'approved':
      case 'aprobado':
      case 'processed':
        return 'badge-approved';
      case 'draft':
      case 'borrador':
        return 'badge-draft';
      case 'cancelled':
      case 'cancelado':
        return 'badge-cancelled';
      default:
        return 'badge-default';
    }
  }
}
