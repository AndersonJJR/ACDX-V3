import { inject } from '@angular/core';
import { CanActivateFn, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const permissionGuard = (permission: string): CanActivateFn => {
  return (route: ActivatedRouteSnapshot) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      router.navigate(['/login'], { queryParams: { returnUrl: route.url.join('/') } });
      return false;
    }

    const userPermissions = authService.getPermissions();
    const hasPermission = userPermissions.includes(permission) || userPermissions.includes('*');

    if (!hasPermission) {
      router.navigate(['/dashboard'], { queryParams: { error: 'forbidden', permission } });
      return false;
    }

    return true;
  };
};