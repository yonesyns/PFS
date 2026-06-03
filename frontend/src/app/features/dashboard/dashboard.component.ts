import { Component, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { CustomersService } from '../../core/services/customers.service';
import { DocumentsService } from '../../core/services/documents.service';
import { MaintenanceService } from '../../core/services/maintenance.service';
import { PaymentsService } from '../../core/services/payments.service';
import { VehiclesService } from '../../core/services/vehicles.service';

@Component({
  selector: 'app-dashboard',
  template: `
    <section class="page-heading">
      <span class="section-kicker">Vue generale</span>
      <h1>Dashboard flotte</h1>
      <p>Suivez l'activite de votre flotte et les actions importantes depuis un seul espace.</p>
    </section>

    @if (error()) {
      <p class="error-message">{{ error() }}</p>
    }

    <div class="metric-grid">
      @for (metric of metrics(); track metric.label) {
        <article class="metric-card">
          <span>{{ metric.label }}</span>
          <strong>{{ metric.value }}</strong>
          <small>{{ metric.note }}</small>
        </article>
      }
    </div>

    <section class="table-panel dashboard-panel">
      <div class="panel-header">
        <div>
          <span class="section-kicker">Priorites</span>
          <h2>Actions a surveiller</h2>
        </div>
        <button class="ghost-button" type="button" (click)="load()">Actualiser</button>
      </div>
      <div class="data-grid">
        <article class="mini-card">
          <strong>{{ pendingCustomers() }} client(s) en attente</strong>
          <span>Validez les comptes pour activer les operations.</span>
        </article>
        <article class="mini-card">
          <strong>{{ overdueMaintenance() }} maintenance(s) en retard</strong>
          <span>Priorisez les interventions critiques.</span>
        </article>
        <article class="mini-card">
          <strong>{{ expiringDocuments() }} document(s) a verifier</strong>
          <span>Controlez les echeances administratives.</span>
        </article>
      </div>
    </section>
  `,
})
export class DashboardComponent {
  private readonly customersService = inject(CustomersService);
  private readonly vehiclesService = inject(VehiclesService);
  private readonly paymentsService = inject(PaymentsService);
  private readonly maintenanceService = inject(MaintenanceService);
  private readonly documentsService = inject(DocumentsService);

  readonly metrics = signal([
    { label: 'Clients', value: '--', note: 'Entreprises gerees' },
    { label: 'Vehicules', value: '--', note: 'Parc suivi' },
    { label: 'Factures', value: '--', note: 'Paiements et echeances' },
    { label: 'Maintenance', value: '--', note: 'Interventions a suivre' },
  ]);
  readonly pendingCustomers = signal(0);
  readonly overdueMaintenance = signal(0);
  readonly expiringDocuments = signal(0);
  readonly error = signal<string | null>(null);

  constructor() {
    this.load();
  }

  load(): void {
    this.error.set(null);
    forkJoin({
      customers: this.customersService.list(),
      vehicles: this.vehiclesService.list(),
      invoices: this.paymentsService.invoices(),
      upcomingMaintenance: this.maintenanceService.upcoming(),
      overdueMaintenance: this.maintenanceService.overdue(),
      expiringDocuments: this.documentsService.expiring(),
    }).subscribe({
      next: ({ customers, vehicles, invoices, upcomingMaintenance, overdueMaintenance, expiringDocuments }) => {
        this.metrics.set([
          { label: 'Clients', value: String(customers.totalElements ?? customers.content.length), note: 'Entreprises gerees' },
          { label: 'Vehicules', value: String(vehicles.totalElements ?? vehicles.content.length), note: 'Parc suivi' },
          { label: 'Factures', value: String(invoices.totalElements ?? invoices.content.length), note: 'Paiements et echeances' },
          { label: 'Maintenance', value: String(upcomingMaintenance.length), note: 'Interventions a suivre' },
        ]);
        this.pendingCustomers.set(customers.content.filter((customer) => customer.status === 'PENDING').length);
        this.overdueMaintenance.set(overdueMaintenance.length);
        this.expiringDocuments.set(expiringDocuments.length);
      },
      error: (error) => this.error.set(error?.error?.message ?? 'Chargement du tableau de bord impossible.'),
    });
  }
}
