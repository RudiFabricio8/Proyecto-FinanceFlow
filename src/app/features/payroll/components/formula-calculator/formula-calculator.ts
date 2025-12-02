// src/app/features/payroll/components/formula-calculator/formula-calculator.ts
import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PayrollFormula } from '../../../../core/models/payroll.model';

@Component({
  selector: 'app-formula-calculator',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './formula-calculator.html',
  styleUrl: './formula-calculator.scss'
})
export class FormulaCalculatorComponent {
  @Output() saveFormula = new EventEmitter<PayrollFormula>();
  @Output() calculate = new EventEmitter<any>();

  concept = '';
  description = '';
  baseSalary = 0;
  hoursWorked = 0;
  overtimeHours = 0;
  bonuses = 0;

  onSave(): void {
    if (!this.concept || !this.description) {
      alert('Por favor completa concepto y descripción');
      return;
    }

    const formula: PayrollFormula = {
      id: Date.now().toString(),
      concept: this.concept,
      description: this.description,
      formula: this.generateFormula(),
      createdAt: new Date().toISOString()
    };

    this.saveFormula.emit(formula);
    this.clearForm();
  }

  onCalculate(): void {
    const inputs = {
      baseSalary: this.baseSalary,
      hoursWorked: this.hoursWorked,
      overtimeHours: this.overtimeHours,
      bonuses: this.bonuses
    };

    this.calculate.emit({ concept: this.concept, inputs });
  }

  private generateFormula(): string {
    return `Salario Base: ${this.baseSalary} + Horas Extra: ${this.overtimeHours} * 1.5 + Bonos: ${this.bonuses}`;
  }

  private clearForm(): void {
    this.concept = '';
    this.description = '';
    this.baseSalary = 0;
    this.hoursWorked = 0;
    this.overtimeHours = 0;
    this.bonuses = 0;
  }
}