// src/app/features/payroll/payroll.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PayrollFormula, PayrollCalculation, PayrollPeriod } from '../../core/models/payroll.model';
import { PayrollService } from '../../core/services/payroll.service';
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
  periods: PayrollPeriod[] = [];
  loading = false;
  error = '';

  constructor(
    private payrollService: PayrollService,
    private exportService: ExportService
  ) {}

  ngOnInit(): void {
    this.loadPeriods();
    this.loadFormulas();
  }

  loadPeriods(): void {
    this.loading = true;
    this.payrollService.listPeriods().subscribe({
      next: (data) => {
        this.periods = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading periods', err);
        this.error = 'Error al cargar períodos';
        this.loading = false;
      }
    });
  }

  loadFormulas(): void {
    this.payrollService.listFormulas().subscribe({
      next: (data) => {
        this.formulas = data;
      },
      error: (err) => console.error('Error loading formulas', err)
    });
  }

  onSaveFormula(formulaData: any): void {
    // formulaData comes from the component, likely needs adaptation to CreateFormulaRequest
    const request = {
      concept: formulaData.concept,
      description: formulaData.description || '',
      formula: formulaData.formula
    };

    this.payrollService.createFormula(request).subscribe({
      next: (newFormula) => {
        this.formulas.push(newFormula);
        alert('Fórmula guardada exitosamente');
      },
      error: (err) => {
        console.error('Error saving formula', err);
        alert('Error al guardar la fórmula');
      }
    });
  }

  onCalculate(data: any): void {
    // This is a local calculation simulation. 
    // Ideally we should use payrollService.executeFormula if we had a formulaId.
    // For now, we keep the local logic but map it to the new structure if needed, 
    // or better, if we are creating a new calculation, we might not send it to backend yet 
    // unless we want to persist it.
    
    const { inputs } = data;
    const result = inputs.baseSalary + (inputs.overtimeHours * 50 * 1.5) + inputs.bonuses;
    
    const calculation: PayrollCalculation = {
      id: Date.now().toString(),
      formulaId: '', // No formula ID for ad-hoc calculation
      periodId: '', // No period assigned yet
      inputs,
      result,
      executedAt: new Date().toISOString()
    };

    this.currentCalculation = calculation;
    this.showModal = true;
  }

  onCloseModal(): void {
    this.showModal = false;
  }

  onExportPdf(calculation: PayrollCalculation): void {
    const data = [{
      'ID': calculation.id,
      'Resultado': `$${calculation.result.toFixed(2)}`,
      'Fecha': new Date(calculation.executedAt).toLocaleString()
    }];
    this.exportService.exportToExcel(data, `Calculo_${calculation.id}`);
  }

  onExportPeriods(): void {
    const data = this.periods.map(p => ({
      'Período': p.period,
      'Estado': p.status,
      'Inicio': p.startDate,
      'Fin': p.endDate
    }));
    this.exportService.exportToExcel(data, 'Periodos_Nomina');
  }
}