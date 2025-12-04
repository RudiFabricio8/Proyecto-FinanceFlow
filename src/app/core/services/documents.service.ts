import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Document, DocumentMetadata } from '../models/document.model';
import { StorageService } from './storage.service';

export interface UploadDocumentRequest {
  periodId?: string;
  type: 'PAYROLL_CERTIFICATE' | 'TAX_WITHHOLDING' | 'PAYMENT_PROOF' | 'OTHER';
  file: File;
  metadata?: DocumentMetadata;
}

@Injectable({ providedIn: 'root' })
export class DocumentsService {
  private apiUrl = `${environment.apiUrl}/documents`;

  constructor(
    private http: HttpClient,
    private storageService: StorageService
  ) {}

  uploadDocument(request: UploadDocumentRequest): Observable<Document> {
    const user = this.storageService.getUser();
    if (!user?.organizationId) {
      console.warn('Organization ID not found - please re-login');
      return throwError(() => new Error('Por favor inicia sesión nuevamente'));
    }

    const formData = new FormData();
    formData.append('file', request.file);
    formData.append('type', request.type);
    formData.append('organizationId', user.organizationId);
    
    if (request.periodId) {
      formData.append('periodId', request.periodId);
    }
    if (request.metadata) {
      formData.append('metadata', JSON.stringify(request.metadata));
    }

    return this.http.post<any>(this.apiUrl, formData).pipe(
      map(item => ({
        ...item,
        amount: item.extractedAmount ? parseFloat(item.extractedAmount) : undefined,
        date: item.extractedDate,
        title: item.fileName
      } as Document))
    );
  }

  listDocuments(periodId?: string): Observable<Document[]> {
    const user = this.storageService.getUser();
    if (!user?.organizationId) {
      console.warn('Organization ID not found for listing documents');
      return of([]);
    }

    let params = new HttpParams().set('organizationId', user.organizationId);
    if (periodId) {
      params = params.set('periodId', periodId);
    }
    
    return this.http.get<{ items: any[] }>(this.apiUrl, { params }).pipe(
      map(response => response.items.map(item => ({
        ...item,
        amount: item.extractedAmount ? parseFloat(item.extractedAmount) : undefined,
        date: item.extractedDate,
        title: item.fileName
      } as Document)))
    );
  }

  downloadDocument(documentId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${documentId}/download`, {
      responseType: 'blob'
    });
  }
}
