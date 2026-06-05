import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = localStorage.getItem('token');
  const needsPasswordChange = localStorage.getItem('needsPasswordChange') === 'true';

  if (!token) {
    router.navigate(['/login']);
    return false;
  }

  if (needsPasswordChange && state.url !== '/setup-password') {
    router.navigate(['/setup-password']);
    return false;
  }

  if (authService.isLoggedIn()) {
    return true;
  } else {
    router.navigate(['/login']);
    return false;
  }
};