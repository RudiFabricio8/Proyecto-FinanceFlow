import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Document, DocumentMetadata } from '../models/document.model';

export interface UploadDocumentRequest {
  periodId?: string;
  type: 'PAYROLL_CERTIFICATE' | 'TAX_WITHHOLDING' | 'PAYMENT_PROOF' | 'OTHER';
  file: File;
  metadata?: DocumentMetadata; // Optional metadata for the document
}

@Injectable({ providedIn: 'root' })
export class DocumentsService {
  private apiUrl = `${environment.apiUrl}/documents`;

  constructor(private http: HttpClient) {}

  uploadDocument(request: UploadDocumentRequest): Observable<Document> {
    const formData = new FormData();
    formData.append('file', request.file);
    formData.append('type', request.type);
    if (request.periodId) {
      formData.append('periodId', request.periodId);
    }
    if (request.metadata) {
      formData.append('metadata', JSON.stringify(request.metadata));
    }

    return this.http.post<Document>(this.apiUrl, formData);
  }

  listDocuments(periodId?: string): Observable<Document[]> {
    // Return mock documents with metadata for demonstration
    const mockDocuments: Document[] = [
      {
        id: '1',
        organizationId: 'org-1',
        filename: 'recibo_supermercado.pdf',
        type: 'PAYMENT_PROOF',
        uploadStatus: 'UPLOADED',
        filePath: '/uploads/recibo_supermercado.pdf',
        uploadedAt: '2024-07-26T10:30:00Z',
        title: 'Recibo de Compra - Supermercado A',
        amount: 125.50,
        provider: 'Supermercado A',
        date: '2024-07-26',
        category: 'Groceries'
      },
      {
        id: '2',
        organizationId: 'org-1',
        filename: 'factura_electricidad.pdf',
        type: 'PAYMENT_PROOF',
        uploadStatus: 'UPLOADED',
        filePath: '/uploads/factura_electricidad.pdf',
        uploadedAt: '2024-07-20T14:15:00Z',
        title: 'Factura de Servicios - Electricidad',
        amount: 89.75,
        provider: 'Empresa Eléctrica XYZ',
        date: '2024-07-20',
        category: 'Utilities'
      },
      {
        id: '3',
        organizationId: 'org-1',
        filename: 'recibo_restaurante.pdf',
        type: 'PAYMENT_PROOF',
        uploadStatus: 'UPLOADED',
        filePath: '/uploads/recibo_restaurante.pdf',
        uploadedAt: '2024-07-15T19:45:00Z',
        title: 'Recibo de Caja - Restaurante El Sabor',
        amount: 45.00,
        provider: 'Restaurante El Sabor',
        date: '2024-07-15',
        category: 'Meals'
      }
    ];

    return of(mockDocuments);
  }

  downloadDocument(documentId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${documentId}/download`, {
      responseType: 'blob'
    });
  }
}
