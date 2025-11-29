// src/app/features/admin/components/period-selector/period-selector.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PayrollPeriod } from '../../../../core/models/payroll-period.model';
import { PeriodItemComponent } from '../period-item/period-item';

@Component({
  selector: 'app-period-selector',
  standalone: true,
  imports: [CommonModule, PeriodItemComponent],
  templateUrl: './period-selector.html',
  styleUrl: './period-selector.scss'
})
export class PeriodSelectorComponent {
  @Input() periods: PayrollPeriod[] = [];
  @Output() periodSelected = new EventEmitter<PayrollPeriod>();

  onPeriodSelect(period: PayrollPeriod): void {
    this.periodSelected.emit(period);
  }
}