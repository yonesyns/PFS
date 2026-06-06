import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { Invoice, InvoiceRequest, PaymentMethod, PlanType, Subscription, Transaction } from '../models/fleet.model';

@Injectable({ providedIn: 'root' })
export class PaymentsService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  invoices(): Observable<PageResponse<Invoice>> {
    return this.http
      .get<ApiResponse<PageResponse<Invoice>>>(`${this.apiUrl}/api/invoices`, { params: { size: 50 } })
      .pipe(map((response) => response.data));
  }

  overdueInvoices(): Observable<PageResponse<Invoice>> {
    return this.http
      .get<ApiResponse<PageResponse<Invoice>>>(`${this.apiUrl}/api/invoices/overdue`, { params: { size: 50 } })
      .pipe(map((response) => response.data));
  }

  createInvoice(request: InvoiceRequest): Observable<Invoice> {
    return this.http.post<ApiResponse<Invoice>>(`${this.apiUrl}/api/invoices`, request).pipe(map((response) => response.data));
  }

  payInvoice(id: string, paymentMethod: PaymentMethod): Observable<Invoice> {
    return this.http
      .post<ApiResponse<Invoice>>(`${this.apiUrl}/api/invoices/${id}/pay`, { paymentMethod })
      .pipe(map((response) => response.data));
  }

  cancelInvoice(id: string, reason: string): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.apiUrl}/api/invoices/${id}/cancel`, null, { params: { reason } })
      .pipe(map((response) => response.data));
  }

  subscriptions(): Observable<Subscription[]> {
    return this.http.get<ApiResponse<Subscription[]>>(`${this.apiUrl}/api/subscriptions`).pipe(map((response) => response.data));
  }

  createSubscription(customerId: string, planType: PlanType, monthlyAmount: number): Observable<Subscription> {
    return this.http
      .post<ApiResponse<Subscription>>(`${this.apiUrl}/api/subscriptions`, null, {
        params: { customerId, planType, monthlyAmount },
      })
      .pipe(map((response) => response.data));
  }

  upgradeSubscription(id: string, newPlan: PlanType, newAmount: number): Observable<Subscription> {
    return this.http
      .patch<ApiResponse<Subscription>>(`${this.apiUrl}/api/subscriptions/${id}/upgrade`, null, {
        params: { newPlan, newAmount },
      })
      .pipe(map((response) => response.data));
  }

  cancelSubscription(id: string, reason: string): Observable<void> {
    return this.http
      .patch<ApiResponse<void>>(`${this.apiUrl}/api/subscriptions/${id}/cancel`, null, { params: { reason } })
      .pipe(map((response) => response.data));
  }

  transactions(invoiceId: string): Observable<PageResponse<Transaction>> {
    return this.http
      .get<ApiResponse<PageResponse<Transaction>>>(`${this.apiUrl}/api/transactions`, { params: { invoiceId, size: 50 } })
      .pipe(map((response) => response.data));
  }
}
