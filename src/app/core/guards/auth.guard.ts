import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const token = localStorage.getItem('auth_token');
  if (token && token.length > 0) return true;
  router.navigateByUrl('/login');
  return false;
};