import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Transaction } from '../../../../core/models/transaction.model';

@Component({
  selector: 'app-transactions-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './transactions-table.html',
  styleUrl: './transactions-table.scss'
})
export class TransactionsTableComponent {
  @Input() transactions: Transaction[] = [];
  @Output() exportClick = new EventEmitter<void>();

  formatAmount(amount: number, type: string): string {
    const isPositive = type === 'INCOME';
    const sign = isPositive ? '+' : '-';
    return `${sign}$${amount.toFixed(2)}`;
  }

  isPositive(type: string): boolean {
    return type === 'INCOME';
  }

  onExportClick(): void {
    this.exportClick.emit();
  }
}