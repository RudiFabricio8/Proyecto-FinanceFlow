import { Injectable } from '@angular/core';
import { Observable, of, throwError, delay } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private storageKey = 'auth_token';

  login(email: string, password: string): Observable<string> {
    if (!email || !password) return throwError(() => new Error('Credenciales inválidas'));
    const token = 'demo-token';
    localStorage.setItem(this.storageKey, token);
    return of(token).pipe(delay(300));
  }

  register(company: string, email: string, pass: string): Observable<string> {
    if (!company || !email || !pass) return throwError(() => new Error('Datos incompletos'));
    const token = 'demo-token';
    localStorage.setItem(this.storageKey, token);
    return of(token).pipe(delay(300));
  }

  logout(): void { localStorage.removeItem(this.storageKey); }
  isLoggedIn(): boolean { return !!localStorage.getItem(this.storageKey); }
}