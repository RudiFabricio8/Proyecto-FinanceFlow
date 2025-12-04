import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Document } from '../../../../core/models/document.model';

@Component({
  selector: 'app-receipt-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './receipt-card.component.html',
  styleUrls: ['./receipt-card.component.scss'],
})
export class ReceiptCardComponent {
  @Input() document!: Document;

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', { day: 'numeric', month: 'long', year: 'numeric' });
  }

  formatAmount(amount?: number): string {
    if (amount === undefined || amount === null) return 'N/A';
    return `$${amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  getDocumentIcon(): string {
    // Return icon class based on document type
    return 'bi bi-file-earmark-text';
  }
}
