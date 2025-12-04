import { Injectable } from '@angular/core';
import { User } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class StorageService {
  private readonly KEYS = {
    IS_AUTH: 'isAuthenticated',
    EMAIL: 'userEmail',
    ORGANIZATION: 'organizationName',
    REMEMBER: 'rememberMe'
  };

  saveUser(user: User, rememberMe: boolean = false): void {
    localStorage.setItem(this.KEYS.IS_AUTH, 'true');
    localStorage.setItem(this.KEYS.EMAIL, user.email);
    
    if (user.organizationName) {
      localStorage.setItem(this.KEYS.ORGANIZATION, user.organizationName);
    }
    
    if (rememberMe) {
      localStorage.setItem(this.KEYS.REMEMBER, 'true');
    }
  }

  getUser(): Partial<User> | null {
    const isAuthenticated = localStorage.getItem(this.KEYS.IS_AUTH) === 'true';
    const email = localStorage.getItem(this.KEYS.EMAIL);
    const organizationName = localStorage.getItem(this.KEYS.ORGANIZATION);

    if (isAuthenticated && email) {
      return {
        email,
        organizationName: organizationName || undefined,
        isAuthenticated: true
      };
    }

    return null;
  }

  clearUser(): void {
    localStorage.removeItem(this.KEYS.IS_AUTH);
    localStorage.removeItem(this.KEYS.EMAIL);
    localStorage.removeItem(this.KEYS.ORGANIZATION);
    localStorage.removeItem(this.KEYS.REMEMBER);
  }
}