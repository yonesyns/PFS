import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { Vehicle, VehicleRequest, VehicleStatus } from '../models/fleet.model';

@Injectable({ providedIn: 'root' })
export class VehiclesService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/vehicles`;

  list(page = 0, size = 50): Observable<PageResponse<Vehicle>> {
    return this.http
      .get<ApiResponse<PageResponse<Vehicle>>>(this.baseUrl, { params: { page, size } })
      .pipe(map((response) => response.data));
  }

  byStatus(status: VehicleStatus): Observable<PageResponse<Vehicle>> {
    return this.http
      .get<ApiResponse<PageResponse<Vehicle>>>(`${this.baseUrl}/status/${status}`, { params: { size: 50 } })
      .pipe(map((response) => response.data));
  }

  create(request: VehicleRequest): Observable<Vehicle> {
    return this.http.post<ApiResponse<Vehicle>>(this.baseUrl, request).pipe(map((response) => response.data));
  }

  update(id: string, request: VehicleRequest): Observable<Vehicle> {
    return this.http.put<ApiResponse<Vehicle>>(`${this.baseUrl}/${id}`, request).pipe(map((response) => response.data));
  }

  updateStatus(id: string, status: VehicleStatus, reason?: string): Observable<Vehicle> {
    return this.http
      .patch<ApiResponse<Vehicle>>(`${this.baseUrl}/${id}/status`, { status, reason })
      .pipe(map((response) => response.data));
  }

  assign(id: string, customerId: string): Observable<Vehicle> {
    return this.http
      .patch<ApiResponse<Vehicle>>(`${this.baseUrl}/${id}/assign`, { customerId })
      .pipe(map((response) => response.data));
  }

  unassign(id: string): Observable<Vehicle> {
    return this.http.patch<ApiResponse<Vehicle>>(`${this.baseUrl}/${id}/unassign`, {}).pipe(map((response) => response.data));
  }

  maintenanceDue(): Observable<Vehicle[]> {
    return this.http.get<ApiResponse<Vehicle[]>>(`${this.baseUrl}/maintenance-due`).pipe(map((response) => response.data));
  }

  expiringInsurance(days = 30): Observable<PageResponse<Vehicle>> {
    return this.http
      .get<ApiResponse<PageResponse<Vehicle>>>(`${this.baseUrl}/expiring-insurance`, { params: { days, size: 50 } })
      .pipe(map((response) => response.data));
  }

  delete(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`).pipe(map((response) => response.data));
  }
}
