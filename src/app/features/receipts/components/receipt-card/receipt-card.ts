// src/app/features/receipts/components/receipt-card/receipt-card.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Receipt } from '../../../../core/models/receipt.model';

@Component({
  selector: 'app-receipt-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './receipt-card.html',
  styleUrl: './receipt-card.scss'
})
export class ReceiptCardComponent {
  @Input() receipt!: Receipt;
  isExpanded = false;

  togglePreview(): void {
    this.isExpanded = !this.isExpanded;
  }

  downloadPDF(): void {
    console.log('Descargando PDF:', this.receipt.title);
    // Aquí iría la lógica real de descarga
  }
}