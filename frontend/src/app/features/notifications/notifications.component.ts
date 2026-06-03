import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NotificationChannel, NotificationItem, NotificationType } from '../../core/models/fleet.model';
import { AuthService } from '../../core/services/auth.service';
import { NotificationsService } from '../../core/services/notifications.service';

@Component({
  selector: 'app-notifications',
  imports: [ReactiveFormsModule],
  template: `
    <section class="page-heading">
      <span class="section-kicker">Alertes</span>
      <h1>Notifications</h1>
      <p>Envoyez des notifications, consultez les messages par destinataire et marquez les alertes comme lues.</p>
    </section>

    <div class="module-grid">
      <section class="table-panel">
        <div class="panel-header">
          <div>
            <span class="section-kicker">Boite de reception</span>
            <h2>{{ notifications().length }} notification(s)</h2>
          </div>
          <strong class="count-pill">{{ unreadCount() }} non lue(s)</strong>
        </div>

        <form class="toolbar-form" [formGroup]="recipientForm" (ngSubmit)="load()">
          <input formControlName="recipientId" placeholder="ID destinataire" />
          <button class="primary-button" type="submit" [disabled]="recipientForm.invalid">Afficher</button>
        </form>

        @if (message()) {
          <p class="success-message">{{ message() }}</p>
        }
        @if (error()) {
          <p class="error-message">{{ error() }}</p>
        }

        <div class="data-grid">
          @for (notification of notifications(); track notification.id) {
            <article class="mini-card">
              <div class="card-row">
                <strong>{{ notification.subject }}</strong>
                <span class="status-pill" [class]="statusClass(notification.status)">{{ notification.status }}</span>
              </div>
              <span>{{ notification.content }}</span>
              <small>{{ notification.type }} - {{ notification.channel }}</small>
              @if (notification.status !== 'READ') {
                <button class="small-button" type="button" (click)="markAsRead(notification)">Marquer comme lue</button>
              }
            </article>
          } @empty {
            <p class="muted-text">Aucune notification pour ce destinataire.</p>
          }
        </div>
      </section>

      <section class="form-panel">
        <span class="section-kicker">Envoi</span>
        <h2>Nouvelle notification</h2>
        <form class="stack-form" [formGroup]="sendForm" (ngSubmit)="send()">
          <label>ID destinataire <input formControlName="recipientId" /></label>
          <label>Email <input type="email" formControlName="recipientEmail" /></label>
          <div class="two-columns">
            <label>
              Type
              <select formControlName="type">
                @for (type of notificationTypes; track type) {
                  <option [value]="type">{{ type }}</option>
                }
              </select>
            </label>
            <label>
              Canal
              <select formControlName="channel">
                @for (channel of channels; track channel) {
                  <option [value]="channel">{{ channel }}</option>
                }
              </select>
            </label>
          </div>
          <label>Sujet <input formControlName="subject" /></label>
          <label>Message <textarea formControlName="content" rows="5"></textarea></label>
          <button class="primary-button" type="submit" [disabled]="sendForm.invalid">Envoyer</button>
        </form>
      </section>
    </div>
  `,
})
export class NotificationsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly notificationsService = inject(NotificationsService);
  private readonly authService = inject(AuthService);

  readonly channels: NotificationChannel[] = ['EMAIL', 'SMS', 'PUSH', 'IN_APP'];
  readonly notificationTypes: NotificationType[] = ['WELCOME', 'ALERT_INFO', 'ALERT_WARNING', 'ALERT_CRITICAL', 'MAINTENANCE_DUE', 'INVOICE_CREATED', 'INVOICE_PAID'];
  readonly notifications = signal<NotificationItem[]>([]);
  readonly unreadCount = signal(0);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);

  readonly recipientForm = this.fb.nonNullable.group({
    recipientId: ['', Validators.required],
  });

  readonly sendForm = this.fb.nonNullable.group({
    recipientId: ['', Validators.required],
    recipientEmail: [''],
    type: ['ALERT_INFO' as NotificationType, Validators.required],
    channel: ['IN_APP' as NotificationChannel, Validators.required],
    subject: ['', Validators.required],
    content: ['', Validators.required],
  });

  constructor() {
    const currentUserId = this.authService.currentUser()?.id ?? '';
    this.recipientForm.patchValue({ recipientId: currentUserId });
    this.sendForm.patchValue({ recipientId: currentUserId, recipientEmail: this.authService.currentUser()?.email ?? '' });
    if (currentUserId) {
      this.load();
    }
  }

  load(): void {
    if (this.recipientForm.invalid) {
      return;
    }
    const { recipientId } = this.recipientForm.getRawValue();
    this.notificationsService.byRecipient(recipientId).subscribe({
      next: (items) => this.notifications.set(items ?? []),
      error: (error) => this.error.set(error?.error?.message ?? 'Chargement des notifications impossible.'),
    });
    this.notificationsService.unreadCount(recipientId).subscribe({
      next: (count) => this.unreadCount.set(count ?? 0),
      error: () => this.unreadCount.set(0),
    });
  }

  send(): void {
    if (this.sendForm.invalid) {
      return;
    }
    const value = this.sendForm.getRawValue();
    this.notificationsService.send({ ...value, recipientEmail: value.recipientEmail || undefined }).subscribe({
      next: () => {
        this.message.set('Notification envoyee.');
        this.recipientForm.patchValue({ recipientId: value.recipientId });
        this.load();
      },
      error: (error) => this.error.set(error?.error?.message ?? 'Envoi impossible.'),
    });
  }

  markAsRead(notification: NotificationItem): void {
    this.notificationsService.markAsRead(notification.id).subscribe({
      next: () => {
        this.message.set('Notification marquee comme lue.');
        this.load();
      },
      error: (error) => this.error.set(error?.error?.message ?? 'Action impossible.'),
    });
  }

  statusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }
}
