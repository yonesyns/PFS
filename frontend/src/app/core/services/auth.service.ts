import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '../models/auth.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly baseUrl = `${environment.apiUrl}/api/auth`;

  readonly currentUser = this.tokenStorage.user;

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.baseUrl}/login`, request).pipe(
      map((response) => response.data),
      tap((auth) => this.tokenStorage.saveSession(auth)),
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.baseUrl}/register`, request).pipe(
      map((response) => response.data),
      tap((auth) => this.tokenStorage.saveSession(auth)),
    );
  }

  loadCurrentUser(): Observable<User> {
    return this.http.get<ApiResponse<User>>(`${this.baseUrl}/me`).pipe(
      map((response) => response.data),
      tap((user) => this.tokenStorage.updateUser(user)),
    );
  }

  logout(): void {
    this.tokenStorage.clear();
  }
}
