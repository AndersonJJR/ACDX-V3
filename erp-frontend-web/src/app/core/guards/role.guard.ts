import { inject } from '@angular/core';
import { CanActivateFn, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard = (allowedRoles: string | string[]): CanActivateFn => {
  return (route: ActivatedRouteSnapshot) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      router.navigate(['/login'], { queryParams: { returnUrl: route.url.join('/') } });
      return false;
    }

    const userRoles = authService.getRoles();
    const roles = Array.isArray(allowedRoles) ? allowedRoles : [allowedRoles];
    const hasRole = roles.some((role) => userRoles.includes(role));

    if (!hasRole) {
      router.navigate(['/dashboard'], { queryParams: { error: 'forbidden', roles: roles.join(',') } });
      return false;
    }

    return true;
  };
};