import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private _isAuth = signal<boolean>(!!localStorage.getItem('ff_token'));
  isAuthenticated = this._isAuth.asReadonly();

  constructor(private router: Router) {}

  login(email: string, password: string): Observable<{ token: string }> {
    if (email && password) {
      const token = 'ff_' + Math.random().toString(36).substr(2, 9);
      return of({ token }).pipe(
        delay(500),
      );
    }
    return throwError(() => new Error('Email o contraseña inválidos'));
  }

  register(company: string, email: string, password: string): Observable<{ token: string }> {
    if (company && email && password) {
      const token = 'ff_' + Math.random().toString(36).substr(2, 9);
      return of({ token }).pipe(
        delay(500),
      );
    }
    return throwError(() => new Error('Datos inválidos'));
  }

  setToken(token: string): void {
    localStorage.setItem('ff_token', token);
    this._isAuth.set(true);
  }

  logout() {
    localStorage.removeItem('ff_token');
    this._isAuth.set(false);
    this.router.navigate(['/']);
  }
}
