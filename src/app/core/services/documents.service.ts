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
    // No default mock documents in views: return empty list.
    // TODO: Implement real API call to fetch documents (optionally filtered by periodId).
    return of([] as Document[]);
  }

  downloadDocument(documentId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${documentId}/download`, {
      responseType: 'blob'
    });
  }
}
