import { inject } from '@angular/core';
import {
  HttpInterceptorFn,
  HttpRequest,
  HttpHandlerFn,
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { TenantService } from '../services/tenant.service';

export const tenantInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<any> => {
  const tenantService = inject(TenantService);

  // Não adiciona tenant em requisições de auth
  if (isAuthRequest(req.url)) {
    return next(req);
  }

  // Não adiciona se já tiver o header
  if (req.headers.has('X-Tenant-ID')) {
    return next(req);
  }

  const tenantId = tenantService.getTenantHeader();
  let authReq = req;
  if (tenantId) {
    authReq = req.clone({
      setHeaders: {
        'X-Tenant-ID': tenantId,
      },
    });
  }

  return next(authReq);
};

function isAuthRequest(url: string): boolean {
  return url.includes('/auth/');
}