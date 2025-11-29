// src/app/features/admin/components/period-item/period-item.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PayrollPeriod } from '../../../../core/models/payroll-period.model';

@Component({
  selector: 'app-period-item',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './period-item.html',
  styleUrl: './period-item.scss'
})
export class PeriodItemComponent {
  @Input() period!: PayrollPeriod;
  @Output() selectPeriod = new EventEmitter<PayrollPeriod>();

  onSelect(): void {
    this.selectPeriod.emit(this.period);
  }
}