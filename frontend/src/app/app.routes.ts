import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { AppShellComponent } from './layout/app-shell.component';
import { AuthLayoutComponent } from './features/auth/auth-layout.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { LoginComponent } from './features/auth/login.component';
import { RegisterComponent } from './features/auth/register.component';
import { FeaturePlaceholderComponent } from './features/placeholder/feature-placeholder.component';

export const routes: Routes = [
  {
    path: 'auth',
    component: AuthLayoutComponent,
    canActivate: [guestGuard],
    children: [
      { path: 'login', component: LoginComponent },
      { path: 'register', component: RegisterComponent },
      { path: '', pathMatch: 'full', redirectTo: 'login' },
    ],
  },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      {
        path: 'customers',
        component: FeaturePlaceholderComponent,
        data: {
          title: 'Clients',
          eyebrow: 'Portefeuille client',
          description: 'Gestion des entreprises, validations, statuts et recherche avancee.',
        },
      },
      {
        path: 'vehicles',
        component: FeaturePlaceholderComponent,
        data: {
          title: 'Vehicules',
          eyebrow: 'Parc automobile',
          description: 'Inventaire de la flotte, affectation client, statuts, assurances et entretiens a prevoir.',
        },
      },
      {
        path: 'maintenance',
        component: FeaturePlaceholderComponent,
        data: {
          title: 'Maintenance',
          eyebrow: 'Suivi technique',
          description: 'Planification, demarrage, cloture, annulation et alertes retard/a venir.',
        },
      },
      {
        path: 'documents',
        component: FeaturePlaceholderComponent,
        data: {
          title: 'Documents',
          eyebrow: 'Dossier flotte',
          description: 'Depot, recherche, telechargement et suivi des documents arrivant a expiration.',
        },
      },
      {
        path: 'payments',
        component: FeaturePlaceholderComponent,
        data: {
          title: 'Paiements',
          eyebrow: 'Facturation',
          description: 'Factures, paiements, abonnements et historique des transactions.',
        },
      },
      {
        path: 'notifications',
        component: FeaturePlaceholderComponent,
        data: {
          title: 'Notifications',
          eyebrow: 'Alertes',
          description: 'Notifications par destinataire, compteur non lu et marquage lecture.',
        },
      },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
