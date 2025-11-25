// src/app/features/admin/components/compliance-filters/compliance-filters.ts
import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-compliance-filters',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './compliance-filters.html',
  styleUrl: './compliance-filters.scss'
})
export class ComplianceFiltersComponent {
  @Output() filter = new EventEmitter<string>();
  @Output() export = new EventEmitter<void>();

  filterText = '';

  onFilter(): void {
    this.filter.emit(this.filterText);
  }

  onExport(): void {
    this.export.emit();
  }

  onFilterTextChange(): void {
    this.filter.emit(this.filterText);
  }
}