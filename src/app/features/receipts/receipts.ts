// src/app/features/receipts/receipts.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReceiptCardComponent } from './components/receipt-card/receipt-card';
import { DocumentsService } from '../../core/services/documents.service';
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

  constructor(private documentsService: DocumentsService) {}

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
        next: () => {
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

