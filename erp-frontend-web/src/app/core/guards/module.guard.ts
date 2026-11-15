import { inject } from '@angular/core';
import { CanActivateFn, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const moduleGuard = (moduleKey: string): CanActivateFn => {
  return (route: ActivatedRouteSnapshot) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      router.navigate(['/login'], { queryParams: { returnUrl: route.url.join('/') } });
      return false;
    }

    const enabledModules = authService.getEnabledModules();
    const hasModule = enabledModules.includes(moduleKey);

    if (!hasModule) {
      router.navigate(['/dashboard'], { queryParams: { error: 'module_disabled', module: moduleKey } });
      return false;
    }

    return true;
  };
};