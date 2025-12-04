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
      this.documentsService.uploadDocument({
        file: file,
        type: 'PAYMENT_PROOF'
      }).subscribe({
        next: (doc) => {
          // Create transaction from document using backend-extracted metadata
          // Note: DocumentsService should map extractedAmount/Date to amount/date
          this.dashboardService.createTransactionFromDocument({
            title: doc.title || doc.filename,
            amount: doc.amount,
            date: doc.date,
            category: doc.category || 'Expenses',
            documentId: doc.id
          }).subscribe({
            next: () => {
              console.log('Transaction created from document');
              this.loadDocuments();
            },
            error: (err) => {
              console.error('Error creating transaction', err);
              // Still reload documents even if transaction creation fails
              this.loadDocuments();
            }
          });
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

