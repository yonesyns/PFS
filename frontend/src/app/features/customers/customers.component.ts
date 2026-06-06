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
                      <button class="small-button" type="button" (click)="edit(customer)">Modifier</button>
                      @if (customer.status !== 'DELETED') {
                        <button class="small-button muted-button" type="button" (click)="deleteCustomer(customer)">Supprimer</button>
                      }
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
        <span class="section-kicker">{{ editingCustomer() ? 'Edition client' : 'Nouveau client' }}</span>
        <h2>{{ editingCustomer() ? 'Modifier une entreprise' : 'Ajouter une entreprise' }}</h2>
        <form class="stack-form" [formGroup]="form" (ngSubmit)="save()">
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
          <div class="row-actions">
            <button class="primary-button" type="submit" [disabled]="form.invalid || saving()">
              {{ saving() ? 'Enregistrement...' : editingCustomer() ? 'Enregistrer' : 'Creer le client' }}
            </button>
            @if (editingCustomer()) {
              <button class="ghost-button" type="button" (click)="cancelEdit()">Annuler</button>
            }
          </div>
        </form>
      </section>
    </div>
  `,
})
export class CustomersComponent {
  private readonly fb = inject(FormBuilder);
  private readonly customersService = inject(CustomersService);

  readonly customerStatuses: CustomerStatus[] = ['PENDING', 'ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED'];
  readonly allCustomers = signal<Customer[]>([]);
  readonly customers = signal<Customer[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);
  readonly editingCustomer = signal<Customer | null>(null);

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
    this.customersService.list().pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (page) => {
        this.allCustomers.set(page.content ?? []);
        this.applyFilters();
      },
      error: (error) => this.error.set(error?.error?.message ?? 'Impossible de charger les clients.'),
    });
  }

  resetFilters(): void {
    this.filters.reset();
    this.applyFilters();
  }

  applyFilters(): void {
    const { companyName, status } = this.filters.getRawValue();
    const normalizedName = companyName.trim().toLowerCase();

    this.customers.set(
      this.allCustomers().filter((customer) => {
        const matchesName = !normalizedName || customer.companyName.toLowerCase().includes(normalizedName);
        const matchesStatus = !status || customer.status === status;
        return matchesName && matchesStatus;
      }),
    );
  }

  save(): void {
    if (this.form.invalid) {
      return;
    }
    const value = this.form.getRawValue();
    this.saving.set(true);
    this.error.set(null);
    this.message.set(null);

    const request = {
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
      };
    const editing = this.editingCustomer();
    const operation = editing ? this.customersService.update(editing.id, request) : this.customersService.create(request);

    operation
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.message.set(editing ? 'Client mis a jour.' : 'Client cree. Il peut maintenant etre valide.');
          this.cancelEdit();
          this.load();
        },
        error: (error) => this.error.set(this.errorMessage(error, 'Creation du client impossible.')),
      });
  }

  edit(customer: Customer): void {
    this.editingCustomer.set(customer);
    this.form.reset({
      companyName: customer.companyName,
      vatNumber: customer.vatNumber,
      email: customer.email,
      phone: customer.phone ?? '',
      street: customer.address?.street ?? '',
      city: customer.address?.city ?? '',
      zipCode: customer.address?.zipCode ?? '',
      country: customer.address?.country ?? 'Maroc',
      contactFirstName: customer.contactFirstName ?? '',
      contactLastName: customer.contactLastName ?? '',
      contactEmail: customer.contactEmail ?? '',
    });
  }

  cancelEdit(): void {
    this.editingCustomer.set(null);
    this.form.reset({ country: 'Maroc' });
  }

  validate(customer: Customer): void {
    this.customersService.validate(customer.id).subscribe({
      next: () => {
        this.message.set('Client valide.');
        this.load();
      },
      error: (error) => this.error.set(this.errorMessage(error, 'Validation impossible.')),
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
      error: (error) => this.error.set(this.errorMessage(error, 'Mise a jour du statut impossible.')),
    });
  }

  deleteCustomer(customer: Customer): void {
    if (!window.confirm(`Supprimer ${customer.companyName} ?`)) {
      return;
    }
    this.customersService.delete(customer.id).subscribe({
      next: () => {
        this.message.set('Client supprime.');
        this.load();
      },
      error: (error) => this.error.set(this.errorMessage(error, 'Suppression impossible.')),
    });
  }

  statusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }

  private errorMessage(error: unknown, fallback: string): string {
    const httpError = error as { error?: { message?: string } };
    return httpError.error?.message ?? fallback;
  }
}
