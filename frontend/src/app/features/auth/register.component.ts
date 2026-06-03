import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/auth.model';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="form-header">
      <span class="section-kicker">Utilisateur</span>
      <h2>Creer un compte</h2>
      <p>Ajoutez un utilisateur pour collaborer sur la gestion de votre flotte.</p>
    </div>

    <form class="auth-form" [formGroup]="form" (ngSubmit)="submit()">
      <div class="two-columns">
        <label>
          Prenom
          <input type="text" formControlName="firstName" autocomplete="given-name" />
        </label>
        <label>
          Nom
          <input type="text" formControlName="lastName" autocomplete="family-name" />
        </label>
      </div>

      <label>
        Email
        <input type="email" formControlName="email" autocomplete="email" />
      </label>

      <label>
        Mot de passe
        <input type="password" formControlName="password" autocomplete="new-password" />
      </label>

      <label>
        Role
        <select formControlName="role">
          @for (role of roles; track role) {
            <option [value]="role">{{ role }}</option>
          }
        </select>
      </label>

      <label>
        ID client associe
        <input type="text" formControlName="customerId" placeholder="Optionnel" />
      </label>

      @if (error()) {
        <p class="error-message">{{ error() }}</p>
      }

      <button class="primary-button" type="submit" [disabled]="form.invalid || loading()">
        {{ loading() ? 'Creation...' : 'Creer le compte' }}
      </button>
    </form>

    <p class="switch-link">Deja inscrit ? <a routerLink="/auth/login">Se connecter</a></p>
  `,
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly roles: UserRole[] = ['CUSTOMER_USER', 'CUSTOMER_ADMIN', 'ADMIN'];
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    firstName: ['', [Validators.required, Validators.maxLength(80)]],
    lastName: ['', [Validators.required, Validators.maxLength(80)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(100)]],
    role: ['CUSTOMER_USER' as UserRole, [Validators.required]],
    customerId: [''],
  });

  submit(): void {
    if (this.form.invalid) {
      return;
    }

    const value = this.form.getRawValue();
    const request = {
      ...value,
      customerId: value.customerId || undefined,
    };

    this.loading.set(true);
    this.error.set(null);

    this.authService
      .register(request)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => this.router.navigateByUrl('/dashboard'),
        error: (error) => this.error.set(error?.error?.message ?? 'Creation impossible pour le moment.'),
      });
  }
}
