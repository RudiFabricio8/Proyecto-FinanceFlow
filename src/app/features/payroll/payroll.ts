// src/app/features/payroll/payroll.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PayrollFormula, PayrollCalculation, PayrollPeriodData } from '../../core/models/payroll.model';
import { FormulaCalculatorComponent } from './components/formula-calculator/formula-calculator';
import { SavedFormulasComponent } from './components/saved-formulas/saved-formulas';
import { CalculationModalComponent } from './components/calculation-modal/calculation-modal';
import { PayrollPeriodsTableComponent } from './components/payroll-periods-table/payroll-periods-table';
import { ExportService } from '../../core/services/export';

@Component({
  selector: 'app-payroll',
  standalone: true,
  imports: [CommonModule, FormulaCalculatorComponent, SavedFormulasComponent, CalculationModalComponent, PayrollPeriodsTableComponent],
  templateUrl: './payroll.html',
  styleUrl: './payroll.scss'
})
export class Payroll implements OnInit {
  formulas: PayrollFormula[] = [];
  currentCalculation: PayrollCalculation | null = null;
  showModal = false;
  periods: PayrollPeriodData[] = [];

  constructor(private exportService: ExportService) {}

  ngOnInit(): void {
    this.loadPeriods();
  }

  onSaveFormula(formula: PayrollFormula): void {
    this.formulas.push(formula);
    alert('Fórmula guardada exitosamente');
  }

  onCalculate(data: any): void {
    const { concept, inputs } = data;
    const result = inputs.baseSalary + (inputs.overtimeHours * 50 * 1.5) + inputs.bonuses;
    
    const calculation: PayrollCalculation = {
      id: Date.now().toString(),
      formulaId: '',
      concept,
      inputs,
      result,
      process: [
        `Salario Base: $${inputs.baseSalary}`,
        `Horas Extra: ${inputs.overtimeHours} * $50 * 1.5 = $${(inputs.overtimeHours * 50 * 1.5).toFixed(2)}`,
        `Bonos: $${inputs.bonuses}`,
        `Total: $${result.toFixed(2)}`
      ],
      calculatedAt: new Date().toISOString()
    };

    this.currentCalculation = calculation;
    this.showModal = true;
    this.updatePeriods(result);
  }

  onCloseModal(): void {
    this.showModal = false;
  }

  onExportPdf(calculation: PayrollCalculation): void {
    const data = [{
      'Concepto': calculation.concept,
      'Resultado': `$${calculation.result.toFixed(2)}`,
      'Fecha': new Date(calculation.calculatedAt).toLocaleString()
    }];
    this.exportService.exportToExcel(data, `Calculo_${calculation.concept}`);
  }

  onExportPeriods(): void {
    const data = this.periods.map(p => ({
      'Período': p.period,
      'Monto': `$${p.totalAmount.toFixed(2)}`,
      'Empleados': p.employees,
      'Estado': p.status
    }));
    this.exportService.exportToExcel(data, 'Periodos_Nomina');
  }

  private loadPeriods(): void {
    this.periods = [
      { id: '1', period: 'Jul 2024', totalAmount: 78500, employees: 125, status: 'processed' },
      { id: '2', period: 'Jun 2024', totalAmount: 77200, employees: 124, status: 'approved' },
      { id: '3', period: 'May 2024', totalAmount: 76800, employees: 124, status: 'approved' },
      { id: '4', period: 'Apr 2024', totalAmount: 78000, employees: 125, status: 'processed' }
    ];
  }

  private updatePeriods(amount: number): void {
    // Actualizar el primer período con el nuevo cálculo
    if (this.periods.length > 0) {
      this.periods[0].totalAmount += amount;
    }
  }
}