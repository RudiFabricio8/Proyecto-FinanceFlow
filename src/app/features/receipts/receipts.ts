// src/app/features/receipts/receipts.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReceiptCardComponent } from './components/receipt-card/receipt-card';
import { DocumentsService } from '../../core/services/documents.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { Document as AppDocument } from '../../core/models/document.model';

@Component({
  selector: 'app-receipts',
  templateUrl: './receipts.html',
  styleUrls: ['./receipts.scss'],
  standalone: true,
  imports: [CommonModule, ReceiptCardComponent],
})
export class Receipts implements OnInit {
  documents: AppDocument[] = [];
  loading = false;

  constructor(
    private documentsService: DocumentsService,
    private dashboardService: DashboardService
  ) {}

  ngOnInit() {
    this.loadDocuments();
  }

  loadDocuments() {
    this.loading = true;
    this.documentsService.listDocuments().subscribe({
      next: (docs) => {
        this.documents = docs;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading documents', err);
        this.loading = false;
      }
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.loading = true;
      // Upload file without injecting mock metadata; real extraction will be handled by backend or dedicated logic
      this.documentsService.uploadDocument({
        file: file,
        type: 'PAYMENT_PROOF'
      }).subscribe({
        next: (doc) => {
          // After successful upload, reload documents. Transaction creation should be handled by backend or separate logic.
          this.loadDocuments();
        },
        error: (err) => {
          console.error('Error uploading document', err);
          this.loading = false;
        }
      });
    }
  }

  triggerFileInput() {
    document.getElementById('fileInput')?.click();
  }
}

