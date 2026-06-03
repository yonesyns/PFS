import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { Customer, Invoice, PaymentMethod, PlanType, Subscription, Transaction, Vehicle } from '../../core/models/fleet.model';
import { CustomersService } from '../../core/services/customers.service';
import { PaymentsService } from '../../core/services/payments.service';
import { VehiclesService } from '../../core/services/vehicles.service';

@Component({
  selector: 'app-payments',
  imports: [ReactiveFormsModule],
  template: `
    <section class="page-heading">
      <span class="section-kicker">Facturation</span>
      <h1>Paiements</h1>
      <p>Gerez les factures, paiements simules, abonnements et transactions.</p>
    </section>

    <div class="module-grid">
      <section class="table-panel">
        <div class="panel-header">
          <div>
            <span class="section-kicker">Factures</span>
            <h2>{{ invoices().length }} facture(s)</h2>
          </div>
          <button class="ghost-button" type="button" (click)="loadAll()">Actualiser</button>
        </div>
        @if (message()) {
          <p class="success-message">{{ message() }}</p>
        }
        @if (error()) {
          <p class="error-message">{{ error() }}</p>
        }
        <div class="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Facture</th>
                <th>Client</th>
                <th>Montant</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (invoice of invoices(); track invoice.id) {
                <tr>
                  <td>
                    <strong>{{ invoice.invoiceNumber || invoice.id.slice(0, 8) }}</strong>
                    <small>{{ invoice.description }}</small>
                  </td>
                  <td>{{ customerName(invoice.customerId) }}</td>
                  <td>{{ money(invoice.amount, invoice.currency) }}</td>
                  <td><span class="status-pill" [class]="statusClass(invoice.status)">{{ invoice.status }}</span></td>
                  <td>
                    <div class="row-actions">
                      @if (invoice.status !== 'PAID' && invoice.status !== 'CANCELLED') {
                        <button class="small-button" type="button" (click)="pay(invoice)">Payer</button>
                        <button class="small-button muted-button" type="button" (click)="cancel(invoice)">Annuler</button>
                      }
                      <button class="small-button" type="button" (click)="loadTransactions(invoice)">Transactions</button>
                    </div>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="5" class="empty-cell">Aucune facture trouvee.</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </section>

      <section class="form-panel">
        <span class="section-kicker">Nouvelle facture</span>
        <h2>Creer une facture</h2>
        <form class="stack-form" [formGroup]="invoiceForm" (ngSubmit)="createInvoice()">
          <label>
            Client
            <select formControlName="customerId">
              <option value="">Selectionner</option>
              @for (customer of customers(); track customer.id) {
                <option [value]="customer.id">{{ customer.companyName }}</option>
              }
            </select>
          </label>
          <label>
            Vehicule
            <select formControlName="vehicleId">
              <option value="">Optionnel</option>
              @for (vehicle of vehicles(); track vehicle.id) {
                <option [value]="vehicle.id">{{ vehicle.plateNumber }}</option>
              }
            </select>
          </label>
          <div class="two-columns">
            <label>Montant en centimes <input type="number" formControlName="amount" /></label>
            <label>Devise <input formControlName="currency" /></label>
          </div>
          <label>Description <input formControlName="description" /></label>
          <button class="primary-button" type="submit" [disabled]="invoiceForm.invalid || saving()">Creer</button>
        </form>
      </section>
    </div>

    <div class="module-grid compact-grid">
      <section class="table-panel">
        <div class="panel-header">
          <div>
            <span class="section-kicker">Abonnements</span>
            <h2>{{ subscriptions().length }} abonnement(s)</h2>
          </div>
        </div>
        <div class="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Client</th>
                <th>Plan</th>
                <th>Mensuel</th>
                <th>Statut</th>
              </tr>
            </thead>
            <tbody>
              @for (subscription of subscriptions(); track subscription.id) {
                <tr>
                  <td>{{ customerName(subscription.customerId) }}</td>
                  <td>{{ subscription.planType }}</td>
                  <td>{{ money(subscription.monthlyAmount, 'EUR') }}</td>
                  <td><span class="status-pill" [class]="statusClass(subscription.status)">{{ subscription.status }}</span></td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="4" class="empty-cell">Aucun abonnement trouve.</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </section>

      <section class="form-panel">
        <span class="section-kicker">Abonnement</span>
        <h2>Creer un abonnement</h2>
        <form class="stack-form" [formGroup]="subscriptionForm" (ngSubmit)="createSubscription()">
          <label>
            Client
            <select formControlName="customerId">
              <option value="">Selectionner</option>
              @for (customer of customers(); track customer.id) {
                <option [value]="customer.id">{{ customer.companyName }}</option>
              }
            </select>
          </label>
          <div class="two-columns">
            <label>
              Plan
              <select formControlName="planType">
                @for (plan of planTypes; track plan) {
                  <option [value]="plan">{{ plan }}</option>
                }
              </select>
            </label>
            <label>Mensuel en centimes <input type="number" formControlName="monthlyAmount" /></label>
          </div>
          <button class="primary-button" type="submit" [disabled]="subscriptionForm.invalid">Creer l'abonnement</button>
        </form>
      </section>
    </div>

    @if (transactions().length) {
      <section class="table-panel">
        <div class="panel-header">
          <div>
            <span class="section-kicker">Transactions</span>
            <h2>{{ transactions().length }} mouvement(s)</h2>
          </div>
        </div>
        @for (transaction of transactions(); track transaction.id) {
          <article class="mini-card">
            <strong>{{ money(transaction.amount, 'EUR') }} - {{ transaction.paymentMethod }}</strong>
            <span>{{ transaction.status }} {{ transaction.transactionDate || '' }}</span>
          </article>
        }
      </section>
    }
  `,
})
export class PaymentsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly paymentsService = inject(PaymentsService);
  private readonly customersService = inject(CustomersService);
  private readonly vehiclesService = inject(VehiclesService);

  readonly paymentMethod: PaymentMethod = 'CARD';
  readonly planTypes: PlanType[] = ['BASIC', 'PRO', 'ENTERPRISE'];
  readonly customers = signal<Customer[]>([]);
  readonly vehicles = signal<Vehicle[]>([]);
  readonly invoices = signal<Invoice[]>([]);
  readonly subscriptions = signal<Subscription[]>([]);
  readonly transactions = signal<Transaction[]>([]);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);

  readonly invoiceForm = this.fb.nonNullable.group({
    customerId: ['', Validators.required],
    vehicleId: [''],
    amount: [5000, Validators.required],
    currency: ['EUR', Validators.required],
    description: ['', Validators.required],
  });

  readonly subscriptionForm = this.fb.nonNullable.group({
    customerId: ['', Validators.required],
    planType: ['BASIC' as PlanType, Validators.required],
    monthlyAmount: [3000, Validators.required],
  });

  constructor() {
    this.loadLookups();
    this.loadAll();
  }

  loadLookups(): void {
    this.customersService.list().subscribe({ next: (page) => this.customers.set(page.content ?? []), error: () => this.customers.set([]) });
    this.vehiclesService.list().subscribe({ next: (page) => this.vehicles.set(page.content ?? []), error: () => this.vehicles.set([]) });
  }

  loadAll(): void {
    this.paymentsService.invoices().subscribe({
      next: (page) => this.invoices.set(page.content ?? []),
      error: (error) => this.error.set(error?.error?.message ?? 'Chargement des factures impossible.'),
    });
    this.paymentsService.subscriptions().subscribe({ next: (items) => this.subscriptions.set(items ?? []), error: () => this.subscriptions.set([]) });
  }

  createInvoice(): void {
    if (this.invoiceForm.invalid) {
      return;
    }
    const value = this.invoiceForm.getRawValue();
    this.saving.set(true);
    this.paymentsService
      .createInvoice({ ...value, vehicleId: value.vehicleId || undefined, subscriptionId: undefined })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.message.set('Facture creee.');
          this.invoiceForm.reset({ amount: 5000, currency: 'EUR' });
          this.loadAll();
        },
        error: (error) => this.error.set(error?.error?.message ?? 'Creation de facture impossible.'),
      });
  }

  createSubscription(): void {
    if (this.subscriptionForm.invalid) {
      return;
    }
    const value = this.subscriptionForm.getRawValue();
    this.paymentsService.createSubscription(value.customerId, value.planType, value.monthlyAmount).subscribe({
      next: () => {
        this.message.set('Abonnement cree.');
        this.loadAll();
      },
      error: (error) => this.error.set(error?.error?.message ?? 'Creation abonnement impossible.'),
    });
  }

  pay(invoice: Invoice): void {
    this.paymentsService.payInvoice(invoice.id, this.paymentMethod).subscribe({
      next: () => {
        this.message.set('Paiement enregistre.');
        this.loadAll();
      },
      error: (error) => this.error.set(error?.error?.message ?? 'Paiement impossible.'),
    });
  }

  cancel(invoice: Invoice): void {
    const reason = window.prompt('Motif d annulation', 'Annule depuis Fleet Manager');
    if (!reason) {
      return;
    }
    this.paymentsService.cancelInvoice(invoice.id, reason).subscribe({
      next: () => {
        this.message.set('Facture annulee.');
        this.loadAll();
      },
      error: (error) => this.error.set(error?.error?.message ?? 'Annulation impossible.'),
    });
  }

  loadTransactions(invoice: Invoice): void {
    this.paymentsService.transactions(invoice.id).subscribe({
      next: (page) => this.transactions.set(page.content ?? []),
      error: (error) => this.error.set(error?.error?.message ?? 'Chargement des transactions impossible.'),
    });
  }

  customerName(customerId?: string): string {
    return this.customers().find((customer) => customer.id === customerId)?.companyName ?? 'Client';
  }

  money(amount?: number, currency = 'EUR'): string {
    return `${((amount ?? 0) / 100).toFixed(2)} ${currency}`;
  }

  statusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }
}
