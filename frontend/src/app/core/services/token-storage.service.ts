import { Injectable, signal } from '@angular/core';
import { AuthResponse, User } from '../models/auth.model';

const ACCESS_TOKEN_KEY = 'fleet_access_token';
const REFRESH_TOKEN_KEY = 'fleet_refresh_token';
const USER_KEY = 'fleet_user';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  readonly user = signal<User | null>(this.readUser());

  get accessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  get refreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  get isAuthenticated(): boolean {
    return Boolean(this.accessToken && this.user());
  }

  saveSession(auth: AuthResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, auth.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, auth.refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify(auth.user));
    this.user.set(auth.user);
  }

  updateUser(user: User): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.user.set(user);
  }

  clear(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.user.set(null);
  }

  private readUser(): User | null {
    const rawUser = localStorage.getItem(USER_KEY);
    if (!rawUser) {
      return null;
    }

    try {
      return JSON.parse(rawUser) as User;
    } catch {
      localStorage.removeItem(USER_KEY);
      return null;
    }
  }
}
