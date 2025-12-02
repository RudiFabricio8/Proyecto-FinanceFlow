// src/app/features/payroll/components/calculation-modal/calculation-modal.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PayrollCalculation } from '../../../../core/models/payroll.model';

@Component({
  selector: 'app-calculation-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './calculation-modal.html',
  styleUrl: './calculation-modal.scss'
})
export class CalculationModalComponent {
  @Input() calculation: PayrollCalculation | null = null;
  @Input() isOpen = false;
  @Output() close = new EventEmitter<void>();
  @Output() exportPdf = new EventEmitter<PayrollCalculation>();

  onClose(): void {
    this.close.emit();
  }

  onExport(): void {
    if (this.calculation) {
      this.exportPdf.emit(this.calculation);
    }
  }
}