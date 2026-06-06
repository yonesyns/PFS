import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { Maintenance, MaintenanceAlert, MaintenancePlan, MaintenancePlanRequest, MaintenanceRequest, MaintenanceStatus } from '../models/fleet.model';

@Injectable({ providedIn: 'root' })
export class MaintenanceService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  byStatus(status: MaintenanceStatus): Observable<PageResponse<Maintenance>> {
    return this.http
      .get<ApiResponse<PageResponse<Maintenance>>>(`${this.apiUrl}/api/maintenance/status/${status}`, { params: { size: 50 } })
      .pipe(map((response) => response.data));
  }

  history(vehicleId: string): Observable<Maintenance[]> {
    return this.http
      .get<ApiResponse<Maintenance[]>>(`${this.apiUrl}/api/vehicles/${vehicleId}/maintenance`)
      .pipe(map((response) => response.data));
  }

  schedule(vehicleId: string, request: MaintenanceRequest): Observable<Maintenance> {
    return this.http
      .post<ApiResponse<Maintenance>>(`${this.apiUrl}/api/vehicles/${vehicleId}/maintenance`, request)
      .pipe(map((response) => response.data));
  }

  start(id: string): Observable<Maintenance> {
    return this.http.patch<ApiResponse<Maintenance>>(`${this.apiUrl}/api/maintenance/${id}/start`, {}).pipe(map((response) => response.data));
  }

  complete(id: string, request: { completedDate: string; actualCost?: number; mileageAtMaintenance: number; partsUsed?: string; notes?: string }): Observable<Maintenance> {
    return this.http
      .patch<ApiResponse<Maintenance>>(`${this.apiUrl}/api/maintenance/${id}/complete`, request)
      .pipe(map((response) => response.data));
  }

  cancel(id: string, reason: string): Observable<Maintenance> {
    return this.http
      .patch<ApiResponse<Maintenance>>(`${this.apiUrl}/api/maintenance/${id}/cancel`, null, { params: { reason } })
      .pipe(map((response) => response.data));
  }

  upcoming(days = 30): Observable<MaintenanceAlert[]> {
    return this.http
      .get<ApiResponse<MaintenanceAlert[]>>(`${this.apiUrl}/api/maintenance/upcoming`, { params: { days } })
      .pipe(map((response) => response.data));
  }

  overdue(): Observable<MaintenanceAlert[]> {
    return this.http.get<ApiResponse<MaintenanceAlert[]>>(`${this.apiUrl}/api/maintenance/overdue`).pipe(map((response) => response.data));
  }

  plans(vehicleId: string): Observable<MaintenancePlan[]> {
    return this.http
      .get<ApiResponse<MaintenancePlan[]>>(`${this.apiUrl}/api/vehicles/${vehicleId}/maintenance-plan`)
      .pipe(map((response) => response.data));
  }

  createPlan(vehicleId: string, request: MaintenancePlanRequest): Observable<MaintenancePlan> {
    return this.http
      .post<ApiResponse<MaintenancePlan>>(`${this.apiUrl}/api/vehicles/${vehicleId}/maintenance-plan`, request)
      .pipe(map((response) => response.data));
  }

  deactivatePlan(planId: string): Observable<void> {
    return this.http
      .patch<ApiResponse<void>>(`${this.apiUrl}/api/maintenance-plan/${planId}/deactivate`, {})
      .pipe(map((response) => response.data));
  }
}
