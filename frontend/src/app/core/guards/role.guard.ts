import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '@core/auth/auth.service';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const roles = route.data?.['roles'] as string[];

  if (!roles?.length) return true;

  const userRole = authService.currentUser()?.systemRole;
  if (userRole && roles.includes(userRole)) {
    return true;
  }

  return router.createUrlTree(['/error/403']);
};
