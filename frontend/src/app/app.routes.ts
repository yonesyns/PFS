import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { AppShellComponent } from './layout/app-shell.component';
import { AuthLayoutComponent } from './features/auth/auth-layout.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { LoginComponent } from './features/auth/login.component';
import { RegisterComponent } from './features/auth/register.component';
import { CustomersComponent } from './features/customers/customers.component';
import { DocumentsComponent } from './features/documents/documents.component';
import { MaintenanceComponent } from './features/maintenance/maintenance.component';
import { NotificationsComponent } from './features/notifications/notifications.component';
import { PaymentsComponent } from './features/payments/payments.component';
import { VehiclesComponent } from './features/vehicles/vehicles.component';

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
      { path: 'customers', component: CustomersComponent },
      { path: 'vehicles', component: VehiclesComponent },
      { path: 'maintenance', component: MaintenanceComponent },
      { path: 'documents', component: DocumentsComponent },
      { path: 'payments', component: PaymentsComponent },
      { path: 'notifications', component: NotificationsComponent },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
