import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StorageService } from './storage.service';
import { AuthResponse, User } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private _isAuth = signal<boolean>(false);
  isAuthenticated = this._isAuth.asReadonly();

  constructor(
    private http: HttpClient,
    private router: Router,
    private storageService: StorageService
  ) {
    this._isAuth.set(!!this.storageService.getUser()?.isAuthenticated);
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap(response => {
        this.setToken(response.token);
        // Store user and organization data from backend response
        const user: User = {
          id: response.user.id,
          email: response.user.email,
          fullName: response.user.fullName,
          role: response.user.role,
          organizationId: response.organization.id,
          organizationName: response.organization.name,
          isAuthenticated: true
        };
        this.storageService.saveUser(user);
        this._isAuth.set(true);
      })
    );
  }

  register(organizationName: string, email: string, password: string, fullName: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, { 
      organizationName, 
      email, 
      password,
      fullName
    }).pipe(
      tap(response => {
        this.setToken(response.token);
        // Store user and organization data from backend response
        const user: User = {
          id: response.user.id,
          email: response.user.email,
          fullName: response.user.fullName,
          role: response.user.role,
          organizationId: response.organization.id,
          organizationName: response.organization.name,
          isAuthenticated: true
        };
        this.storageService.saveUser(user);
        this._isAuth.set(true);
      })
    );
  }

  setToken(token: string): void {
    localStorage.setItem('ff_token', token);
    this._isAuth.set(true);
  }

  logout() {
    localStorage.removeItem('ff_token');
    this.storageService.clearUser();
    this._isAuth.set(false);
    this.router.navigate(['/']);
  }
}
