import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { Customer, CustomerStatus } from '../../core/models/fleet.model';
import { CustomersService } from '../../core/services/customers.service';

@Component({
  selector: 'app-customers',
  imports: [ReactiveFormsModule],
  template: `
    <section class="page-heading">
      <span class="section-kicker">Portefeuille client</span>
      <h1>Clients</h1>
      <p>Consultez, recherchez, creez et validez les entreprises clientes.</p>
    </section>

    <section class="toolbar-panel">
      <form class="toolbar-form" [formGroup]="filters" (ngSubmit)="load()">
        <input type="search" formControlName="companyName" placeholder="Rechercher une entreprise" />
        <select formControlName="status">
          <option value="">Tous les statuts</option>
          @for (status of customerStatuses; track status) {
            <option [value]="status">{{ status }}</option>
          }
        </select>
        <button class="primary-button" type="submit">Filtrer</button>
        <button class="ghost-button" type="button" (click)="resetFilters()">Reinitialiser</button>
      </form>
    </section>

    <div class="module-grid">
      <section class="table-panel">
        <div class="panel-header">
          <div>
            <span class="section-kicker">Liste</span>
            <h2>{{ customers().length }} client(s)</h2>
          </div>
          <button class="ghost-button" type="button" (click)="load()" [disabled]="loading()">Actualiser</button>
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
                <th>Entreprise</th>
                <th>Contact</th>
                <th>Statut</th>
                <th>Ville</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (customer of customers(); track customer.id) {
                <tr>
                  <td>
                    <strong>{{ customer.companyName }}</strong>
                    <small>{{ customer.vatNumber }}</small>
                  </td>
                  <td>
                    <span>{{ customer.email }}</span>
                    <small>{{ customer.phone || customer.contactPhone || '-' }}</small>
                  </td>
                  <td><span class="status-pill" [class]="statusClass(customer.status)">{{ customer.status }}</span></td>
                  <td>{{ customer.address?.city || '-' }}</td>
                  <td>
                    <div class="row-actions">
                      @if (customer.status === 'PENDING') {
                        <button class="small-button" type="button" (click)="validate(customer)">Valider</button>
                      }
                      <select [value]="customer.status" (change)="changeStatus(customer, $any($event.target).value)">
                        @for (status of customerStatuses; track status) {
                          <option [value]="status">{{ status }}</option>
                        }
                      </select>
                    </div>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="5" class="empty-cell">Aucun client trouve.</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </section>

      <section class="form-panel">
        <span class="section-kicker">Nouveau client</span>
        <h2>Ajouter une entreprise</h2>
        <form class="stack-form" [formGroup]="form" (ngSubmit)="create()">
          <label>Entreprise <input formControlName="companyName" /></label>
          <label>Numero fiscal <input formControlName="vatNumber" /></label>
          <label>Email <input type="email" formControlName="email" /></label>
          <label>Telephone <input formControlName="phone" /></label>
          <div class="two-columns">
            <label>Ville <input formControlName="city" /></label>
            <label>Pays <input formControlName="country" /></label>
          </div>
          <label>Adresse <input formControlName="street" /></label>
          <label>Code postal <input formControlName="zipCode" /></label>
          <div class="two-columns">
            <label>Prenom contact <input formControlName="contactFirstName" /></label>
            <label>Nom contact <input formControlName="contactLastName" /></label>
          </div>
          <label>Email contact <input type="email" formControlName="contactEmail" /></label>
          <button class="primary-button" type="submit" [disabled]="form.invalid || saving()">
            {{ saving() ? 'Enregistrement...' : 'Creer le client' }}
          </button>
        </form>
      </section>
    </div>
  `,
})
export class CustomersComponent {
  private readonly fb = inject(FormBuilder);
  private readonly customersService = inject(CustomersService);

  readonly customerStatuses: CustomerStatus[] = ['PENDING', 'ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED'];
  readonly customers = signal<Customer[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);

  readonly filters = this.fb.nonNullable.group({
    companyName: [''],
    status: [''],
  });

  readonly form = this.fb.nonNullable.group({
    companyName: ['', [Validators.required, Validators.minLength(2)]],
    vatNumber: ['', [Validators.required, Validators.minLength(5)]],
    email: ['', [Validators.required, Validators.email]],
    phone: [''],
    street: [''],
    city: [''],
    zipCode: [''],
    country: ['Maroc'],
    contactFirstName: [''],
    contactLastName: [''],
    contactEmail: [''],
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    const { companyName, status } = this.filters.getRawValue();
    const request = companyName || status ? this.customersService.search({ companyName, status }) : this.customersService.list();

    request.pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (page) => this.customers.set(page.content ?? []),
      error: (error) => this.error.set(error?.error?.message ?? 'Impossible de charger les clients.'),
    });
  }

  resetFilters(): void {
    this.filters.reset();
    this.load();
  }

  create(): void {
    if (this.form.invalid) {
      return;
    }
    const value = this.form.getRawValue();
    this.saving.set(true);
    this.error.set(null);
    this.message.set(null);

    this.customersService
      .create({
        companyName: value.companyName,
        vatNumber: value.vatNumber,
        email: value.email,
        phone: value.phone || undefined,
        address: {
          street: value.street || undefined,
          city: value.city || undefined,
          zipCode: value.zipCode || undefined,
          country: value.country || undefined,
        },
        contactFirstName: value.contactFirstName || undefined,
        contactLastName: value.contactLastName || undefined,
        contactEmail: value.contactEmail || undefined,
      })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.message.set('Client cree. Il peut maintenant etre valide.');
          this.form.reset({ country: 'Maroc' });
          this.load();
        },
        error: (error) => this.error.set(error?.error?.message ?? 'Creation du client impossible.'),
      });
  }

  validate(customer: Customer): void {
    this.customersService.validate(customer.id).subscribe({
      next: () => {
        this.message.set('Client valide.');
        this.load();
      },
      error: (error) => this.error.set(error?.error?.message ?? 'Validation impossible.'),
    });
  }

  changeStatus(customer: Customer, status: CustomerStatus): void {
    if (status === customer.status) {
      return;
    }

    this.customersService.updateStatus(customer.id, status, `Statut modifie depuis Fleet Manager`).subscribe({
      next: () => {
        this.message.set('Statut client mis a jour.');
        this.load();
      },
      error: (error) => this.error.set(error?.error?.message ?? 'Mise a jour du statut impossible.'),
    });
  }

  statusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }
}
