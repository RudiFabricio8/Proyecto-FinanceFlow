import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const hasToken = !!localStorage.getItem('ff_token');
  if (!hasToken) {
    router.navigate(['/']);
    return false;
  }
  return true;
};