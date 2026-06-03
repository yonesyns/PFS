import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { NotificationChannel, NotificationItem, NotificationType } from '../models/fleet.model';

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/notifications`;

  byRecipient(recipientId: string): Observable<NotificationItem[]> {
    return this.http.get<ApiResponse<NotificationItem[]>>(`${this.baseUrl}/recipient/${recipientId}`).pipe(map((response) => response.data));
  }

  byRecipientPaginated(recipientId: string): Observable<PageResponse<NotificationItem>> {
    return this.http
      .get<ApiResponse<PageResponse<NotificationItem>>>(`${this.baseUrl}/recipient/${recipientId}/paginated`, { params: { size: 50 } })
      .pipe(map((response) => response.data));
  }

  unreadCount(recipientId: string): Observable<number> {
    return this.http.get<ApiResponse<number>>(`${this.baseUrl}/recipient/${recipientId}/unread-count`).pipe(map((response) => response.data));
  }

  send(request: {
    recipientId: string;
    recipientEmail?: string;
    recipientPhone?: string;
    type: NotificationType;
    channel: NotificationChannel;
    subject: string;
    content: string;
  }): Observable<NotificationItem> {
    return this.http.post<ApiResponse<NotificationItem>>(this.baseUrl, request).pipe(map((response) => response.data));
  }

  markAsRead(id: string): Observable<void> {
    return this.http.patch<ApiResponse<void>>(`${this.baseUrl}/${id}/read`, {}).pipe(map((response) => response.data));
  }
}
