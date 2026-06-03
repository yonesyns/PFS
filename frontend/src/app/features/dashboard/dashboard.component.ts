import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  template: `
    <section class="page-heading">
      <span class="section-kicker">Vue generale</span>
      <h1>Dashboard flotte</h1>
      <p>Suivez l'activite de votre flotte et les actions importantes depuis un seul espace.</p>
    </section>

    <div class="metric-grid">
      @for (metric of metrics; track metric.label) {
        <article class="metric-card">
          <span>{{ metric.label }}</span>
          <strong>{{ metric.value }}</strong>
          <small>{{ metric.note }}</small>
        </article>
      }
    </div>
  `,
})
export class DashboardComponent {
  readonly metrics = [
    { label: 'Clients', value: '--', note: 'Entreprises gerees' },
    { label: 'Vehicules', value: '--', note: 'Parc suivi' },
    { label: 'Factures', value: '--', note: 'Paiements et echeances' },
    { label: 'Maintenance', value: '--', note: 'Interventions a suivre' },
  ];
}
