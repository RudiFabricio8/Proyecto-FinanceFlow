import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Document {
  id: string;
  organizationId: string;
  periodId?: string;
  filename: string;
  type: 'PAYROLL_CERTIFICATE' | 'TAX_WITHHOLDING' | 'PAYMENT_PROOF' | 'OTHER';
  uploadStatus: 'PENDING' | 'UPLOADED' | 'FAILED';
  filePath: string;
  uploadedAt: string;
}

export interface UploadDocumentRequest {
  periodId?: string;
  type: 'PAYROLL_CERTIFICATE' | 'TAX_WITHHOLDING' | 'PAYMENT_PROOF' | 'OTHER';
  file: File;
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

    return this.http.post<Document>(this.apiUrl, formData);
  }

  listDocuments(periodId?: string): Observable<Document[]> {
    let params = new HttpParams();
    if (periodId) {
      params = params.set('periodId', periodId);
    }
    return this.http.get<Document[]>(this.apiUrl, { params });
  }

  downloadDocument(documentId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${documentId}/download`, {
      responseType: 'blob'
    });
  }
}
