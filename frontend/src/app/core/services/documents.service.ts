import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { DocumentStatus, DocumentType, EntityType, FleetDocument } from '../models/fleet.model';

@Injectable({ providedIn: 'root' })
export class DocumentsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/documents`;

  search(entityType: EntityType, entityId: string, documentType?: DocumentType | '', status?: DocumentStatus | ''): Observable<PageResponse<FleetDocument>> {
    let params = new HttpParams().set('entityType', entityType).set('entityId', entityId).set('size', 50);
    if (documentType) {
      params = params.set('documentType', documentType);
    }
    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<ApiResponse<PageResponse<FleetDocument>>>(this.baseUrl, { params }).pipe(map((response) => response.data));
  }

  expiring(days = 30): Observable<FleetDocument[]> {
    return this.http.get<ApiResponse<FleetDocument[]>>(`${this.baseUrl}/expiring`, { params: { days } }).pipe(map((response) => response.data));
  }

  upload(file: File, entityType: EntityType, entityId: string, documentType: DocumentType, expiryDate?: string): Observable<FleetDocument> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('entityType', entityType);
    formData.append('entityId', entityId);
    formData.append('documentType', documentType);
    if (expiryDate) {
      formData.append('expiryDate', expiryDate);
    }

    return this.http.post<ApiResponse<FleetDocument>>(`${this.baseUrl}/upload`, formData).pipe(map((response) => response.data));
  }

  downloadUrl(id: string): string {
    return `${this.baseUrl}/${id}/download`;
  }

  delete(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`).pipe(map((response) => response.data));
  }
}
