// src/app/features/payroll/payroll.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PayrollFormula, PayrollCalculation } from '../../core/models/payroll.model';
import { PayrollService } from '../../core/services/payroll.service';
import { FormulaCalculatorComponent } from './components/formula-calculator/formula-calculator';
import { SavedFormulasComponent } from './components/saved-formulas/saved-formulas';
import { CalculationModalComponent } from './components/calculation-modal/calculation-modal';
import { ExportService } from '../../core/services/export';

@Component({
  selector: 'app-payroll',
  standalone: true,
  imports: [CommonModule, FormsModule, FormulaCalculatorComponent, SavedFormulasComponent, CalculationModalComponent],
  templateUrl: './payroll.html',
  styleUrls: ['./payroll.scss']
})
export class Payroll implements OnInit {
  formulas: PayrollFormula[] = [];
  currentCalculation: PayrollCalculation | null = null;
  showModal = false;
  loading = false;
  error = '';

  // Modal states
  showNewFormulaModal = false;

  // Form models
  newFormula = {
    concept: '',
    description: '',
    formula: ''
  };

  constructor(
    private payrollService: PayrollService,
    private exportService: ExportService
  ) {}

  ngOnInit(): void {
    this.loadFormulas();
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
    const request = {
      concept: formulaData.concept,
      description: formulaData.description || '',
      formula: formulaData.formula
    };

    this.payrollService.createFormula(request).subscribe({
      next: (newFormula) => {
        // Attach the last calculation result if it matches the saved formula concept
        const formulaWithResult: any = { ...newFormula };
        if (this.currentCalculation && this.currentCalculation.result) {
           formulaWithResult.lastResult = this.currentCalculation.result;
        }
        
        this.formulas.push(formulaWithResult);
        alert('Fórmula guardada exitosamente');
      },
      error: (err) => {
        console.error('Error saving formula', err);
        alert('Error al guardar la fórmula');
      }
    });
  }

  onCalculate(data: any): void {
    const { inputs } = data;
    const result = inputs.baseSalary + (inputs.overtimeHours * 50 * 1.5) + inputs.bonuses;
    
    const calculation: PayrollCalculation = {
      id: Date.now().toString(),
      formulaId: '',
      periodId: '',
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

  // New Formula Modal Methods
  openNewFormulaModal(): void {
    this.showNewFormulaModal = true;
    this.resetFormulaForm();
  }

  closeNewFormulaModal(): void {
    this.showNewFormulaModal = false;
  }

  resetFormulaForm(): void {
    this.newFormula = {
      concept: '',
      description: '',
      formula: ''
    };
  }

  createFormula(): void {
    if (!this.newFormula.concept || !this.newFormula.formula) {
      alert('Por favor complete el concepto y la fórmula');
      return;
    }

    const formulaData = {
      concept: this.newFormula.concept,
      description: this.newFormula.description,
      formula: this.newFormula.formula
    };

    this.payrollService.createFormula(formulaData).subscribe({
      next: (formula) => {
        console.log('Fórmula creada:', formula);
        this.formulas.push(formula);
        this.closeNewFormulaModal();
        alert('Fórmula creada exitosamente');
      },
      error: (err) => {
        console.error('Error creating formula', err);
        alert('Error al crear la fórmula: ' + (err.error?.message || 'Error desconocido'));
      }
    });
  }
}