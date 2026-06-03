import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-auth-layout',
  imports: [RouterOutlet],
  template: `
    <main class="auth-page">
      <section class="auth-hero">
        <div class="brand-mark">FM</div>
        <div>
          <p class="eyebrow">Fleet Manager</p>
          <h1>Fleet Manager</h1>
          <p class="intro">
            Gerez votre flotte, vos clients, vos documents et vos paiements depuis un espace simple et centralise.
          </p>
        </div>
      </section>
      <section class="auth-panel">
        <router-outlet />
      </section>
    </main>
  `,
})
export class AuthLayoutComponent {}
