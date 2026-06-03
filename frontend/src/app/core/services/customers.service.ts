import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { Customer, CustomerRequest, CustomerStatus } from '../models/fleet.model';

@Injectable({ providedIn: 'root' })
export class CustomersService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/customers`;

  list(page = 0, size = 50): Observable<PageResponse<Customer>> {
    return this.http
      .get<ApiResponse<PageResponse<Customer>>>(this.baseUrl, { params: { page, size } })
      .pipe(map((response) => response.data));
  }

  search(filters: { companyName?: string; status?: string; email?: string; vatNumber?: string }): Observable<PageResponse<Customer>> {
    let params = new HttpParams().set('size', 50);
    Object.entries(filters).forEach(([key, value]) => {
      if (value) {
        params = params.set(key, value);
      }
    });

    return this.http
      .get<ApiResponse<PageResponse<Customer>>>(`${this.baseUrl}/search`, { params })
      .pipe(map((response) => response.data));
  }

  create(request: CustomerRequest): Observable<Customer> {
    return this.http.post<ApiResponse<Customer>>(this.baseUrl, request).pipe(map((response) => response.data));
  }

  validate(id: string): Observable<Customer> {
    return this.http.patch<ApiResponse<Customer>>(`${this.baseUrl}/${id}/validate`, {}).pipe(map((response) => response.data));
  }

  updateStatus(id: string, status: CustomerStatus, reason?: string): Observable<Customer> {
    return this.http
      .patch<ApiResponse<Customer>>(`${this.baseUrl}/${id}/status`, { status, reason })
      .pipe(map((response) => response.data));
  }
}
