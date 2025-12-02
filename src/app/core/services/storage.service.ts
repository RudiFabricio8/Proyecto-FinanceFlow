import { Injectable } from '@angular/core';
import { User } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class StorageService {
  private readonly KEYS = {
    IS_AUTH: 'isAuthenticated',
    EMAIL: 'userEmail',
    COMPANY: 'companyName',
    REMEMBER: 'rememberMe'
  };

  saveUser(user: User, rememberMe: boolean = false): void {
    localStorage.setItem(this.KEYS.IS_AUTH, 'true');
    localStorage.setItem(this.KEYS.EMAIL, user.email);
    
    if (user.companyName) {
      localStorage.setItem(this.KEYS.COMPANY, user.companyName);
    }
    
    if (rememberMe) {
      localStorage.setItem(this.KEYS.REMEMBER, 'true');
    }
  }

  getUser(): User | null {
    const isAuthenticated = localStorage.getItem(this.KEYS.IS_AUTH) === 'true';
    const email = localStorage.getItem(this.KEYS.EMAIL);
    const companyName = localStorage.getItem(this.KEYS.COMPANY);

    if (isAuthenticated && email) {
      return {
        email,
        companyName: companyName || undefined,
        isAuthenticated: true
      };
    }

    return null;
  }

  clearUser(): void {
    localStorage.removeItem(this.KEYS.IS_AUTH);
    localStorage.removeItem(this.KEYS.EMAIL);
    localStorage.removeItem(this.KEYS.COMPANY);
    localStorage.removeItem(this.KEYS.REMEMBER);
  }
}