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
                        @for (customer of activeCustomers(); track customer.id) {
                          <option [value]="customer.id">{{ customer.companyName }}</option>
                        }
                      </select>
                      <button class="small-button" type="button" (click)="edit(vehicle)">Modifier</button>
                      <button class="small-button muted-button" type="button" (click)="deleteVehicle(vehicle)">Supprimer</button>
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
        <span class="section-kicker">{{ editingVehicle() ? 'Edition vehicule' : 'Nouveau vehicule' }}</span>
        <h2>{{ editingVehicle() ? 'Modifier le vehicule' : 'Ajouter au parc' }}</h2>
        <form class="stack-form" [formGroup]="form" (ngSubmit)="save()">
          <div class="two-columns">
            <label>
              Immatriculation
              <input formControlName="plateNumber" placeholder="AA-123-AA" />
              <small>Format requis: AA-123-AA</small>
            </label>
            <label>
              VIN
              <input formControlName="vin" placeholder="1HGCM82633A004352" />
              <small>17 caracteres, sans I, O ou Q</small>
            </label>
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
              @for (customer of activeCustomers(); track customer.id) {
                <option [value]="customer.id">{{ customer.companyName }}</option>
              }
            </select>
            <small>Le client doit etre actif. Sinon, creez le vehicule non affecte puis validez le client.</small>
          </label>
          <div class="two-columns">
            <label>Date immatriculation <input type="date" formControlName="registrationDate" /></label>
            <label>Fin assurance <input type="date" formControlName="insuranceExpiryDate" /></label>
          </div>
          <label>Boitier GPS <input formControlName="gpsDeviceId" /></label>
          <label>Controle technique <input type="date" formControlName="technicalInspectionDate" /></label>
          <div class="row-actions">
            <button class="primary-button" type="submit" [disabled]="form.invalid || saving()">
              {{ saving() ? 'Enregistrement...' : editingVehicle() ? 'Enregistrer' : 'Ajouter le vehicule' }}
            </button>
            @if (editingVehicle()) {
              <button class="ghost-button" type="button" (click)="cancelEdit()">Annuler</button>
            }
          </div>
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
  readonly activeCustomers = signal<Customer[]>([]);
  readonly statusFilter = signal('');
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);
  readonly editingVehicle = signal<Vehicle | null>(null);

  readonly form = this.fb.nonNullable.group({
    plateNumber: ['', [Validators.required, Validators.pattern(/^[A-Z]{2}-\d{3}-[A-Z]{2}$/)]],
    vin: ['', [Validators.required, Validators.pattern(/^[A-HJ-NPR-Z0-9]{17}$/)]],
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
    gpsDeviceId: [''],
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
      next: (page) => {
        const customers = page.content ?? [];
        this.customers.set(customers);
        this.activeCustomers.set(customers.filter((customer) => customer.status === 'ACTIVE'));
      },
      error: () => {
        this.customers.set([]);
        this.activeCustomers.set([]);
      },
    });
  }

  filterByStatus(status: string): void {
    this.statusFilter.set(status);
    this.load();
  }

  save(): void {
    if (this.form.invalid) {
      return;
    }
    const value = this.form.getRawValue();
    const plateNumber = value.plateNumber.trim().toUpperCase();
    const vin = value.vin.trim().toUpperCase();

    this.form.patchValue({ plateNumber, vin });
    if (this.form.invalid) {
      this.error.set('Verifiez le format: plaque AA-123-AA et VIN de 17 caracteres sans I, O ou Q.');
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    this.message.set(null);

    const request = {
        ...value,
        plateNumber,
        vin,
        fuelType: (value.fuelType || undefined) as FuelType | undefined,
        customerId: value.customerId || undefined,
        registrationDate: value.registrationDate || undefined,
        insuranceExpiryDate: value.insuranceExpiryDate || undefined,
        technicalInspectionDate: value.technicalInspectionDate || undefined,
        gpsDeviceId: value.gpsDeviceId || undefined,
      };
    const editing = this.editingVehicle();
    const operation = editing ? this.vehiclesService.update(editing.id, request) : this.vehiclesService.create(request);

    operation
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.message.set(editing ? 'Vehicule mis a jour.' : 'Vehicule ajoute.');
          this.cancelEdit();
          this.load();
        },
        error: (error) => this.error.set(this.errorMessage(error, 'Ajout du vehicule impossible.')),
      });
  }

  edit(vehicle: Vehicle): void {
    this.editingVehicle.set(vehicle);
    this.form.reset({
      plateNumber: vehicle.plateNumber,
      vin: vehicle.vin,
      brand: vehicle.brand,
      model: vehicle.model,
      year: vehicle.year,
      color: vehicle.color ?? '',
      fuelType: vehicle.fuelType ?? '',
      mileage: vehicle.mileage ?? 0,
      customerId: vehicle.customerId ?? '',
      registrationDate: vehicle.registrationDate ?? '',
      insuranceExpiryDate: vehicle.insuranceExpiryDate ?? '',
      technicalInspectionDate: vehicle.technicalInspectionDate ?? '',
      gpsDeviceId: vehicle.gpsDeviceId ?? '',
    });
  }

  cancelEdit(): void {
    this.editingVehicle.set(null);
    this.form.reset({ year: 2026, mileage: 0 });
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
      error: (error) => this.error.set(this.errorMessage(error, 'Mise a jour impossible.')),
    });
  }

  assign(vehicle: Vehicle, customerId: string): void {
    const request = customerId ? this.vehiclesService.assign(vehicle.id, customerId) : this.vehiclesService.unassign(vehicle.id);
    request.subscribe({
      next: () => {
        this.message.set(customerId ? 'Vehicule affecte.' : 'Vehicule desaffecte.');
        this.load();
      },
      error: (error) => this.error.set(this.errorMessage(error, 'Affectation impossible.')),
    });
  }

  deleteVehicle(vehicle: Vehicle): void {
    if (!window.confirm(`Supprimer le vehicule ${vehicle.plateNumber} ?`)) {
      return;
    }
    this.vehiclesService.delete(vehicle.id).subscribe({
      next: () => {
        this.message.set('Vehicule supprime.');
        this.load();
      },
      error: (error) => this.error.set(this.errorMessage(error, 'Suppression impossible.')),
    });
  }

  customerName(customerId?: string): string {
    return this.customers().find((customer) => customer.id === customerId)?.companyName ?? 'Non affecte';
  }

  statusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }

  private errorMessage(error: unknown, fallback: string): string {
    const httpError = error as { error?: { message?: string } };
    return httpError.error?.message ?? fallback;
  }
}
