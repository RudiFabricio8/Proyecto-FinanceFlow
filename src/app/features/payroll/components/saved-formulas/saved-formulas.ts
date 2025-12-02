// src/app/features/payroll/components/saved-formulas/saved-formulas.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PayrollFormula } from '../../../../core/models/payroll.model';

@Component({
  selector: 'app-saved-formulas',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './saved-formulas.html',
  styleUrl: './saved-formulas.scss'
})
export class SavedFormulasComponent {
  @Input() formulas: PayrollFormula[] = [];
}