import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { Maintenance, MaintenanceAlert, MaintenanceStatus, MaintenanceType, Vehicle } from '../../core/models/fleet.model';
import { MaintenanceService } from '../../core/services/maintenance.service';
import { VehiclesService } from '../../core/services/vehicles.service';

@Component({
  selector: 'app-maintenance',
  imports: [ReactiveFormsModule],
  template: `
    <section class="page-heading">
      <span class="section-kicker">Suivi technique</span>
      <h1>Maintenance</h1>
      <p>Planifiez les interventions, suivez les retards et cloturez les maintenances terminees.</p>
    </section>

    <div class="module-grid">
      <section class="table-panel">
        <div class="panel-header">
          <div>
            <span class="section-kicker">Interventions</span>
            <h2>{{ maintenances().length }} intervention(s)</h2>
          </div>
          <div class="row-actions">
            <select [value]="status()" (change)="setStatus($any($event.target).value)">
              @for (item of maintenanceStatuses; track item) {
                <option [value]="item">{{ item }}</option>
              }
            </select>
            <button class="ghost-button" type="button" (click)="loadAll()">Actualiser</button>
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
                <th>Type</th>
                <th>Date</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (item of maintenances(); track item.id) {
                <tr>
                  <td>
                    <strong>{{ item.vehiclePlateNumber || vehiclePlate(item.vehicleId) }}</strong>
                    <small>{{ item.description }}</small>
                  </td>
                  <td>{{ item.type }}</td>
                  <td>{{ item.scheduledDate }}</td>
                  <td><span class="status-pill" [class]="statusClass(item.status)">{{ item.status }}</span></td>
                  <td>
                    <div class="row-actions">
                      @if (item.status === 'SCHEDULED') {
                        <button class="small-button" type="button" (click)="start(item)">Demarrer</button>
                      }
                      @if (item.status === 'IN_PROGRESS') {
                        <button class="small-button" type="button" (click)="prepareCompletion(item)">Cloturer</button>
                      }
                      @if (item.status !== 'COMPLETED' && item.status !== 'CANCELLED') {
                        <button class="small-button muted-button" type="button" (click)="cancel(item)">Annuler</button>
                      }
                    </div>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="5" class="empty-cell">Aucune intervention trouvee.</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </section>

      <section class="form-panel">
        <span class="section-kicker">Planifier</span>
        <h2>Nouvelle intervention</h2>
        <form class="stack-form" [formGroup]="form" (ngSubmit)="schedule()">
          <label>
            Vehicule
            <select formControlName="vehicleId">
              <option value="">Selectionner</option>
              @for (vehicle of vehicles(); track vehicle.id) {
                <option [value]="vehicle.id">{{ vehicle.plateNumber }} - {{ vehicle.brand }} {{ vehicle.model }}</option>
              }
            </select>
          </label>
          <label>
            Type
            <select formControlName="type">
              @for (type of maintenanceTypes; track type) {
                <option [value]="type">{{ type }}</option>
              }
            </select>
          </label>
          <label>Description <input formControlName="description" /></label>
          <div class="two-columns">
            <label>Date prevue <input type="date" formControlName="scheduledDate" /></label>
            <label>Cout estime <input type="number" formControlName="estimatedCost" /></label>
          </div>
          <div class="two-columns">
            <label>Garage <input formControlName="garageName" /></label>
            <label>Technicien <input formControlName="technicianName" /></label>
          </div>
          <label>Notes <input formControlName="notes" /></label>
          <button class="primary-button" type="submit" [disabled]="form.invalid || saving()">
            {{ saving() ? 'Planification...' : 'Planifier' }}
          </button>
        </form>
      </section>
    </div>

    <div class="module-grid compact-grid">
      <section class="table-panel">
        <div class="panel-header">
          <div>
            <span class="section-kicker">A venir</span>
            <h2>{{ upcoming().length }} alerte(s)</h2>
          </div>
        </div>
        @for (alert of upcoming(); track alert.maintenanceId) {
          <article class="mini-card">
            <strong>{{ alert.vehiclePlateNumber || vehiclePlate(alert.vehicleId) }}</strong>
            <span>{{ alert.message || alert.description }}</span>
          </article>
        } @empty {
          <p class="muted-text">Aucune alerte a venir.</p>
        }
      </section>
      <section class="table-panel">
        <div class="panel-header">
          <div>
            <span class="section-kicker">En retard</span>
            <h2>{{ overdue().length }} alerte(s)</h2>
          </div>
        </div>
        @for (alert of overdue(); track alert.maintenanceId) {
          <article class="mini-card danger-card">
            <strong>{{ alert.vehiclePlateNumber || vehiclePlate(alert.vehicleId) }}</strong>
            <span>{{ alert.message || alert.description }}</span>
          </article>
        } @empty {
          <p class="muted-text">Aucune maintenance en retard.</p>
        }
      </section>
    </div>

    @if (completionTarget()) {
      <section class="form-panel overlay-panel">
        <span class="section-kicker">Cloture</span>
        <h2>{{ completionTarget()?.vehiclePlateNumber || 'Intervention' }}</h2>
        <form class="stack-form" [formGroup]="completionForm" (ngSubmit)="complete()">
          <div class="two-columns">
            <label>Date realisee <input type="date" formControlName="completedDate" /></label>
            <label>Kilometrage <input type="number" formControlName="mileageAtMaintenance" /></label>
          </div>
          <label>Cout reel <input type="number" formControlName="actualCost" /></label>
          <label>Pieces utilisees <input formControlName="partsUsed" /></label>
          <label>Notes <input formControlName="notes" /></label>
          <div class="row-actions">
            <button class="primary-button" type="submit" [disabled]="completionForm.invalid">Cloturer</button>
            <button class="ghost-button" type="button" (click)="completionTarget.set(null)">Fermer</button>
          </div>
        </form>
      </section>
    }
  `,
})
export class MaintenanceComponent {
  private readonly fb = inject(FormBuilder);
  private readonly maintenanceService = inject(MaintenanceService);
  private readonly vehiclesService = inject(VehiclesService);

  readonly maintenanceStatuses: MaintenanceStatus[] = ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'OVERDUE'];
  readonly maintenanceTypes: MaintenanceType[] = [
    'PREVENTIVE',
    'CORRECTIVE',
    'TECHNICAL_INSPECTION',
    'OIL_CHANGE',
    'TIRE_CHANGE',
    'BRAKE_CHECK',
    'BATTERY_REPLACEMENT',
    'GENERAL_OVERHAUL',
  ];
  readonly vehicles = signal<Vehicle[]>([]);
  readonly maintenances = signal<Maintenance[]>([]);
  readonly upcoming = signal<MaintenanceAlert[]>([]);
  readonly overdue = signal<MaintenanceAlert[]>([]);
  readonly status = signal<MaintenanceStatus>('SCHEDULED');
  readonly completionTarget = signal<Maintenance | null>(null);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    vehicleId: ['', Validators.required],
    type: ['PREVENTIVE' as MaintenanceType, Validators.required],
    description: ['', Validators.required],
    scheduledDate: ['', Validators.required],
    estimatedCost: [0],
    garageName: [''],
    garageContact: [''],
    technicianName: [''],
    notes: [''],
  });

  readonly completionForm = this.fb.nonNullable.group({
    completedDate: [new Date().toISOString().slice(0, 10), Validators.required],
    actualCost: [0],
    mileageAtMaintenance: [0, Validators.required],
    partsUsed: [''],
    notes: [''],
  });

  constructor() {
    this.loadVehicles();
    this.loadAll();
  }

  loadVehicles(): void {
    this.vehiclesService.list().subscribe({
      next: (page) => this.vehicles.set(page.content ?? []),
      error: () => this.vehicles.set([]),
    });
  }

  loadAll(): void {
    this.maintenanceService.byStatus(this.status()).subscribe({
      next: (page) => this.maintenances.set(page.content ?? []),
      error: (error) => this.error.set(error?.error?.message ?? 'Chargement des maintenances impossible.'),
    });
    this.maintenanceService.upcoming().subscribe({ next: (items) => this.upcoming.set(items ?? []), error: () => this.upcoming.set([]) });
    this.maintenanceService.overdue().subscribe({ next: (items) => this.overdue.set(items ?? []), error: () => this.overdue.set([]) });
  }

  setStatus(status: MaintenanceStatus): void {
    this.status.set(status);
    this.loadAll();
  }

  schedule(): void {
    if (this.form.invalid) {
      return;
    }
    const value = this.form.getRawValue();
    this.saving.set(true);
    this.maintenanceService
      .schedule(value.vehicleId, {
        type: value.type,
        description: value.description,
        scheduledDate: value.scheduledDate,
        estimatedCost: value.estimatedCost || undefined,
        garageName: value.garageName || undefined,
        garageContact: value.garageContact || undefined,
        technicianName: value.technicianName || undefined,
        notes: value.notes || undefined,
      })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.message.set('Maintenance planifiee.');
          this.form.reset({ type: 'PREVENTIVE', estimatedCost: 0 });
          this.loadAll();
        },
        error: (error) => this.error.set(error?.error?.message ?? 'Planification impossible.'),
      });
  }

  start(item: Maintenance): void {
    this.maintenanceService.start(item.id).subscribe({
      next: () => {
        this.message.set('Maintenance demarree.');
        this.loadAll();
      },
      error: (error) => this.error.set(error?.error?.message ?? 'Demarrage impossible.'),
    });
  }

  prepareCompletion(item: Maintenance): void {
    this.completionTarget.set(item);
    this.completionForm.patchValue({ mileageAtMaintenance: item.mileageAtMaintenance ?? 0, actualCost: item.estimatedCost ?? 0 });
  }

  complete(): void {
    const target = this.completionTarget();
    if (!target || this.completionForm.invalid) {
      return;
    }
    this.maintenanceService.complete(target.id, this.completionForm.getRawValue()).subscribe({
      next: () => {
        this.message.set('Maintenance cloturee.');
        this.completionTarget.set(null);
        this.loadAll();
      },
      error: (error) => this.error.set(error?.error?.message ?? 'Cloture impossible.'),
    });
  }

  cancel(item: Maintenance): void {
    const reason = window.prompt('Motif d annulation', 'Annule depuis Fleet Manager');
    if (!reason) {
      return;
    }
    this.maintenanceService.cancel(item.id, reason).subscribe({
      next: () => {
        this.message.set('Maintenance annulee.');
        this.loadAll();
      },
      error: (error) => this.error.set(error?.error?.message ?? 'Annulation impossible.'),
    });
  }

  vehiclePlate(vehicleId: string): string {
    return this.vehicles().find((vehicle) => vehicle.id === vehicleId)?.plateNumber ?? vehicleId.slice(0, 8);
  }

  statusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }
}
