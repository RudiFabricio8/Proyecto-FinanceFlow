import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, map, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StorageService } from './storage.service';

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

  login(email: string, password: string): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap(response => {
        this.setToken(response.token);
        // We need to decode the token or fetch user details to save them.
        // For now, we'll save the email from the request.
        this.storageService.saveUser({ email, isAuthenticated: true });
      })
    );
  }

  register(company: string, email: string, password: string, fullName?: string, role?: string): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.apiUrl}/register`, { 
      companyName: company, 
      email, 
      password,
      fullName,
      role
    }).pipe(
      tap(response => {
        this.setToken(response.token);
        this.storageService.saveUser({ email, companyName: company, fullName, role, isAuthenticated: true });
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
