import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="form-header">
      <span class="section-kicker">Connexion</span>
      <h2>Acceder a votre espace</h2>
      <p>Connectez-vous pour suivre votre flotte et gerer vos operations au quotidien.</p>
    </div>

    <form class="auth-form" [formGroup]="form" (ngSubmit)="submit()">
      <label>
        Email
        <input type="email" formControlName="email" placeholder="admin@fleet.com" autocomplete="email" />
      </label>

      <label>
        Mot de passe
        <input type="password" formControlName="password" placeholder="Admin123!" autocomplete="current-password" />
      </label>

      @if (error()) {
        <p class="error-message">{{ error() }}</p>
      }

      <button class="primary-button" type="submit" [disabled]="form.invalid || loading()">
        {{ loading() ? 'Connexion...' : 'Se connecter' }}
      </button>
    </form>

    <p class="switch-link">Pas encore de compte ? <a routerLink="/auth/register">Creer un compte</a></p>
  `,
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email: ['admin@fleet.com', [Validators.required, Validators.email]],
    password: ['Admin123!', [Validators.required]],
  });

  submit(): void {
    if (this.form.invalid) {
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.authService
      .login(this.form.getRawValue())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => this.router.navigateByUrl('/dashboard'),
        error: (error) => this.error.set(error?.error?.message ?? 'Connexion impossible pour le moment.'),
      });
  }
}
