import { Injectable, inject } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptorFn,
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { TenantService } from '../services/tenant.service';

@Injectable()
export class TenantInterceptor implements HttpInterceptor {
  private readonly tenantService = inject(TenantService);

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Não adiciona tenant em requisições de auth
    if (this.isAuthRequest(req.url)) {
      return next.handle(req);
    }

    // Não adiciona se já tiver o header
    if (req.headers.has('X-Tenant-ID')) {
      return next.handle(req);
    }

    const tenantId = this.tenantService.getTenantHeader();
    if (tenantId) {
      req = req.clone({
        setHeaders: {
          'X-Tenant-ID': tenantId,
        },
      });
    }

    return next.handle(req);
  }

  private isAuthRequest(url: string): boolean {
    return url.includes('/auth/');
  }
}

export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
  const interceptor = inject(TenantInterceptor);
  return interceptor.intercept(req, next);
};