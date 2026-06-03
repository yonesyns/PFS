import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import {
  LucideBell,
  LucideCarFront,
  LucideCreditCard,
  LucideFileText,
  LucideLayoutDashboard,
  LucideLogOut,
  LucideMenu,
  LucideShieldCheck,
  LucideUsers,
  LucideWrench,
} from '@lucide/angular';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-shell',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    LucideBell,
    LucideCarFront,
    LucideCreditCard,
    LucideFileText,
    LucideLayoutDashboard,
    LucideLogOut,
    LucideMenu,
    LucideShieldCheck,
    LucideUsers,
    LucideWrench,
  ],
  template: `
    <div class="app-shell" [class.sidebar-open]="sidebarOpen()">
      <aside class="sidebar">
        <a class="brand" routerLink="/dashboard">
          <span class="brand-mark">FM</span>
          <span>
            <strong>Fleet Manager</strong>
            <small>Gestion de flotte</small>
          </span>
        </a>

        <nav class="nav-list" aria-label="Navigation principale">
          @for (item of navItems; track item.path) {
            <a [routerLink]="item.path" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: item.exact }">
              @switch (item.icon) {
                @case ('dashboard') {
                  <svg lucideLayoutDashboard size="18"></svg>
                }
                @case ('customers') {
                  <svg lucideUsers size="18"></svg>
                }
                @case ('vehicles') {
                  <svg lucideCarFront size="18"></svg>
                }
                @case ('maintenance') {
                  <svg lucideWrench size="18"></svg>
                }
                @case ('documents') {
                  <svg lucideFileText size="18"></svg>
                }
                @case ('payments') {
                  <svg lucideCreditCard size="18"></svg>
                }
                @case ('notifications') {
                  <svg lucideBell size="18"></svg>
                }
              }
              <span>{{ item.label }}</span>
            </a>
          }
        </nav>
      </aside>

      <div class="workspace">
        <header class="topbar">
          <button class="icon-button mobile-only" type="button" aria-label="Ouvrir le menu" (click)="toggleSidebar()">
            <svg lucideMenu size="20"></svg>
          </button>

          <div class="context-title">
            <span class="section-kicker">Espace flotte</span>
            <strong>Gestion operationnelle</strong>
          </div>

          <div class="user-chip">
            <svg lucideShieldCheck size="18"></svg>
            <span>{{ displayName() }}</span>
            <small>{{ auth.currentUser()?.role }}</small>
          </div>

          <button class="ghost-button" type="button" (click)="logout()">
            <svg lucideLogOut size="17"></svg>
            <span>Deconnexion</span>
          </button>
        </header>

        <main class="content">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
})
export class AppShellComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly sidebarOpen = signal(false);

  readonly navItems = [
    { label: 'Dashboard', path: '/dashboard', icon: 'dashboard', exact: true },
    { label: 'Clients', path: '/customers', icon: 'customers', exact: false },
    { label: 'Vehicules', path: '/vehicles', icon: 'vehicles', exact: false },
    { label: 'Maintenance', path: '/maintenance', icon: 'maintenance', exact: false },
    { label: 'Documents', path: '/documents', icon: 'documents', exact: false },
    { label: 'Paiements', path: '/payments', icon: 'payments', exact: false },
    { label: 'Notifications', path: '/notifications', icon: 'notifications', exact: false },
  ];

  readonly displayName = computed(() => {
    const user = this.auth.currentUser();
    return user ? `${user.firstName} ${user.lastName}` : 'Utilisateur';
  });

  toggleSidebar(): void {
    this.sidebarOpen.update((value) => !value);
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
