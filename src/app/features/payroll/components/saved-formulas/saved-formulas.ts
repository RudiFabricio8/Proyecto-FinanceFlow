// src/app/features/payroll/components/saved-formulas/saved-formulas.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PayrollFormula } from '../../../../core/models/payroll.model';

export interface PayrollFormulaWithResult extends PayrollFormula {
  lastResult?: number;
}

@Component({
  selector: 'app-saved-formulas',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './saved-formulas.html',
  styleUrls: ['./saved-formulas.scss']
})
export class SavedFormulasComponent {
  @Input() formulas: PayrollFormulaWithResult[] = [];

  formatCurrency(amount: number | undefined): string {
    if (amount === undefined) return '';
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN'
    }).format(amount);
  }
}