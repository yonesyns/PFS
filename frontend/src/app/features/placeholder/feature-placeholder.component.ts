import { Component, input } from '@angular/core';

@Component({
  selector: 'app-feature-placeholder',
  template: `
    <section class="page-heading">
      <span class="section-kicker">{{ eyebrow() }}</span>
      <h1>{{ title() }}</h1>
      <p>{{ description() }}</p>
    </section>

    <div class="work-panel">
      <h2>Module en preparation</h2>
      <p>
        Cette section sera completee avec les tableaux, formulaires, filtres et actions utiles pour votre gestion
        quotidienne.
      </p>
    </div>
  `,
})
export class FeaturePlaceholderComponent {
  readonly title = input.required<string>();
  readonly eyebrow = input('Module');
  readonly description = input('Fonctionnalite backend a connecter.');
}
