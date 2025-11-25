// src/app/features/admin/components/compliance-table/compliance-table.ts
import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DailyPayrollRecord, ComplianceStatus } from '../../../../core/models/payroll-period.model';
import { ComplianceRowComponent } from '../compliance-row/compliance-row';
import { ComplianceFiltersComponent } from '../compliance-filters/compliance-filters';

@Component({
  selector: 'app-compliance-table',
  standalone: true,
  imports: [CommonModule, ComplianceRowComponent, ComplianceFiltersComponent],
  templateUrl: './compliance-table.html',
  styleUrl: './compliance-table.scss'
})
export class ComplianceTableComponent implements OnInit, OnChanges {
  @Input() records: DailyPayrollRecord[] = [];
  @Input() selectedPeriod: string = '';
  @Output() statusChange = new EventEmitter<{ id: string; status: ComplianceStatus }>();
  @Output() export = new EventEmitter<void>();

  filteredRecords: DailyPayrollRecord[] = [];
  sortColumn: string = '';
  sortDirection: 'asc' | 'desc' = 'asc';

  ngOnInit(): void {
    this.filteredRecords = [...this.records];
  }

  ngOnChanges(): void {
    this.filteredRecords = [...this.records];
  }

  onFilter(filterText: string): void {
    if (!filterText) {
      this.filteredRecords = [...this.records];
      return;
    }
    
    this.filteredRecords = this.records.filter(record =>
      record.date.toLowerCase().includes(filterText.toLowerCase())
    );
  }

  onExport(): void {
    this.export.emit();
  }

  onStatusChange(data: { id: string; status: ComplianceStatus }): void {
    this.statusChange.emit(data);
  }

  onSort(column: string): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    
    this.filteredRecords.sort((a, b) => {
      let aValue: any = a[column as keyof DailyPayrollRecord];
      let bValue: any = b[column as keyof DailyPayrollRecord];
      
      if (this.sortDirection === 'asc') {
        return aValue > bValue ? 1 : -1;
      } else {
        return aValue < bValue ? 1 : -1;
      }
    });
  }
}