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

  formatAmount(amount: number, isPositive: boolean): string {
    const sign = isPositive ? '+' : '-';
    return `${sign}$${amount.toFixed(2)}`;
  }

  onExportClick(): void {
    this.exportClick.emit();
  }
}