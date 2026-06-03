import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { Customer, FuelType, Vehicle, VehicleStatus } from '../../core/models/fleet.model';
import { CustomersService } from '../../core/services/customers.service';
import { VehiclesService } from '../../core/services/vehicles.service';

@Component({
  selector: 'app-vehicles',
  imports: [ReactiveFormsModule],
  template: `
    <section class="page-heading">
      <span class="section-kicker">Parc automobile</span>
      <h1>Vehicules</h1>
      <p>Ajoutez les vehicules, affectez-les aux clients et suivez leur statut operationnel.</p>
    </section>

    <div class="module-grid">
      <section class="table-panel">
        <div class="panel-header">
          <div>
            <span class="section-kicker">Inventaire</span>
            <h2>{{ vehicles().length }} vehicule(s)</h2>
          </div>
          <div class="row-actions">
            <select [value]="statusFilter()" (change)="filterByStatus($any($event.target).value)">
              <option value="">Tous</option>
              @for (status of vehicleStatuses; track status) {
                <option [value]="status">{{ status }}</option>
              }
            </select>
            <button class="ghost-button" type="button" (click)="load()">Actualiser</button>
          </div>
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
                <th>Vehicule</th>
                <th>Client</th>
                <th>Statut</th>
                <th>Assurance</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (vehicle of vehicles(); track vehicle.id) {
                <tr>
                  <td>
                    <strong>{{ vehicle.plateNumber }}</strong>
                    <small>{{ vehicle.brand }} {{ vehicle.model }} - {{ vehicle.year }}</small>
                  </td>
                  <td>{{ customerName(vehicle.customerId) }}</td>
                  <td><span class="status-pill" [class]="statusClass(vehicle.status)">{{ vehicle.status }}</span></td>
                  <td>{{ vehicle.insuranceExpiryDate || '-' }}</td>
                  <td>
                    <div class="row-actions">
                      <select [value]="vehicle.status" (change)="changeStatus(vehicle, $any($event.target).value)">
                        @for (status of vehicleStatuses; track status) {
                          <option [value]="status">{{ status }}</option>
                        }
                      </select>
                      <select [value]="vehicle.customerId || ''" (change)="assign(vehicle, $any($event.target).value)">
                        <option value="">Non affecte</option>
                        @for (customer of customers(); track customer.id) {
                          <option [value]="customer.id">{{ customer.companyName }}</option>
                        }
                      </select>
                    </div>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="5" class="empty-cell">Aucun vehicule trouve.</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </section>

      <section class="form-panel">
        <span class="section-kicker">Nouveau vehicule</span>
        <h2>Ajouter au parc</h2>
        <form class="stack-form" [formGroup]="form" (ngSubmit)="create()">
          <div class="two-columns">
            <label>Immatriculation <input formControlName="plateNumber" /></label>
            <label>VIN <input formControlName="vin" /></label>
          </div>
          <div class="two-columns">
            <label>Marque <input formControlName="brand" /></label>
            <label>Modele <input formControlName="model" /></label>
          </div>
          <div class="two-columns">
            <label>Annee <input type="number" formControlName="year" /></label>
            <label>Couleur <input formControlName="color" /></label>
          </div>
          <div class="two-columns">
            <label>
              Energie
              <select formControlName="fuelType">
                <option value="">Non precise</option>
                @for (fuel of fuelTypes; track fuel) {
                  <option [value]="fuel">{{ fuel }}</option>
                }
              </select>
            </label>
            <label>Kilometrage <input type="number" formControlName="mileage" /></label>
          </div>
          <label>
            Client
            <select formControlName="customerId">
              <option value="">Non affecte</option>
              @for (customer of customers(); track customer.id) {
                <option [value]="customer.id">{{ customer.companyName }}</option>
              }
            </select>
          </label>
          <div class="two-columns">
            <label>Date immatriculation <input type="date" formControlName="registrationDate" /></label>
            <label>Fin assurance <input type="date" formControlName="insuranceExpiryDate" /></label>
          </div>
          <label>Controle technique <input type="date" formControlName="technicalInspectionDate" /></label>
          <button class="primary-button" type="submit" [disabled]="form.invalid || saving()">
            {{ saving() ? 'Ajout...' : 'Ajouter le vehicule' }}
          </button>
        </form>
      </section>
    </div>
  `,
})
export class VehiclesComponent {
  private readonly fb = inject(FormBuilder);
  private readonly vehiclesService = inject(VehiclesService);
  private readonly customersService = inject(CustomersService);

  readonly vehicleStatuses: VehicleStatus[] = ['PENDING', 'ACTIVE', 'INACTIVE', 'MAINTENANCE', 'ORPHANED', 'SOLD'];
  readonly fuelTypes: FuelType[] = ['DIESEL', 'PETROL', 'ELECTRIC', 'HYBRID', 'LPG'];
  readonly vehicles = signal<Vehicle[]>([]);
  readonly customers = signal<Customer[]>([]);
  readonly statusFilter = signal('');
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    plateNumber: ['', Validators.required],
    vin: ['', Validators.required],
    brand: ['', Validators.required],
    model: ['', Validators.required],
    year: [2026, Validators.required],
    color: [''],
    fuelType: [''],
    mileage: [0],
    customerId: [''],
    registrationDate: [''],
    insuranceExpiryDate: [''],
    technicalInspectionDate: [''],
  });

  constructor() {
    this.loadCustomers();
    this.load();
  }

  load(): void {
    const status = this.statusFilter();
    const request = status ? this.vehiclesService.byStatus(status as VehicleStatus) : this.vehiclesService.list();
    request.subscribe({
      next: (page) => this.vehicles.set(page.content ?? []),
      error: (error) => this.error.set(error?.error?.message ?? 'Impossible de charger les vehicules.'),
    });
  }

  loadCustomers(): void {
    this.customersService.list().subscribe({
      next: (page) => this.customers.set(page.content ?? []),
      error: () => this.customers.set([]),
    });
  }

  filterByStatus(status: string): void {
    this.statusFilter.set(status);
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

    this.vehiclesService
      .create({
        ...value,
        fuelType: (value.fuelType || undefined) as FuelType | undefined,
        customerId: value.customerId || undefined,
        registrationDate: value.registrationDate || undefined,
        insuranceExpiryDate: value.insuranceExpiryDate || undefined,
        technicalInspectionDate: value.technicalInspectionDate || undefined,
      })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.message.set('Vehicule ajoute.');
          this.form.reset({ year: 2026, mileage: 0 });
          this.load();
        },
        error: (error) => this.error.set(error?.error?.message ?? 'Ajout du vehicule impossible.'),
      });
  }

  changeStatus(vehicle: Vehicle, status: VehicleStatus): void {
    if (status === vehicle.status) {
      return;
    }
    this.vehiclesService.updateStatus(vehicle.id, status, 'Statut modifie depuis Fleet Manager').subscribe({
      next: () => {
        this.message.set('Statut vehicule mis a jour.');
        this.load();
      },
      error: (error) => this.error.set(error?.error?.message ?? 'Mise a jour impossible.'),
    });
  }

  assign(vehicle: Vehicle, customerId: string): void {
    const request = customerId ? this.vehiclesService.assign(vehicle.id, customerId) : this.vehiclesService.unassign(vehicle.id);
    request.subscribe({
      next: () => {
        this.message.set(customerId ? 'Vehicule affecte.' : 'Vehicule desaffecte.');
        this.load();
      },
      error: (error) => this.error.set(error?.error?.message ?? 'Affectation impossible.'),
    });
  }

  customerName(customerId?: string): string {
    return this.customers().find((customer) => customer.id === customerId)?.companyName ?? 'Non affecte';
  }

  statusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }
}
