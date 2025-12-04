// src/app/features/payroll/payroll.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
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
  imports: [CommonModule, FormsModule, FormulaCalculatorComponent, SavedFormulasComponent, CalculationModalComponent, PayrollPeriodsTableComponent],
  templateUrl: './payroll.html',
  styleUrls: ['./payroll.scss']
})
export class Payroll implements OnInit {
  formulas: PayrollFormula[] = [];
  currentCalculation: PayrollCalculation | null = null;
  showModal = false;
  periods: PayrollPeriod[] = [];
  loading = false;
  error = '';

  // Modal states
  showNewPeriodModal = false;
  showNewFormulaModal = false;

  // Form models
  newPeriod = {
    name: '',
    startDate: '',
    endDate: '',
    notes: ''
  };

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

  onExportPeriods(): void {
    const data = this.periods.map(p => ({
      'Período': p.period,
      'Estado': p.status,
      'Inicio': p.startDate,
      'Fin': p.endDate
    }));
    this.exportService.exportToExcel(data, 'Periodos_Nomina');
  }

  // New Period Modal Methods
  openNewPeriodModal(): void {
    this.showNewPeriodModal = true;
    this.resetPeriodForm();
  }

  closeNewPeriodModal(): void {
    this.showNewPeriodModal = false;
  }

  resetPeriodForm(): void {
    this.newPeriod = {
      name: '',
      startDate: '',
      endDate: '',
      notes: ''
    };
  }

  createPeriod(): void {
    if (!this.newPeriod.name || !this.newPeriod.startDate || !this.newPeriod.endDate) {
      alert('Por favor complete todos los campos requeridos');
      return;
    }

    // Backend expects 'period' not 'name'
    const periodData = {
      period: this.newPeriod.name,
      startDate: this.newPeriod.startDate,
      endDate: this.newPeriod.endDate
    };

    this.payrollService.createPeriod(periodData).subscribe({
      next: (period) => {
        console.log('Período creado:', period);
        this.periods.push(period);
        this.closeNewPeriodModal();
        alert('Período de nómina creado exitosamente');
      },
      error: (err) => {
        console.error('Error creating period', err);
        alert('Error al crear el período: ' + (err.error?.message || 'Error desconocido'));
      }
    });
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