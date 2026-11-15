import { Injectable, inject } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptorFn,
} from '@angular/common/http';
import { Observable, finalize } from 'rxjs';
import { LoadingService } from '../services/loading.service';

@Injectable()
export class LoadingInterceptor implements HttpInterceptor {
  private readonly loadingService = inject(LoadingService);

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Não mostra loading para requisições com header 'X-Skip-Loading'
    if (req.headers.has('X-Skip-Loading')) {
      return next.handle(req);
    }

    // Não mostra loading para requisições de polling/heartbeat
    if (this.isPollingRequest(req.url)) {
      return next.handle(req);
    }

    this.loadingService.start();

    return next.handle(req).pipe(
      finalize(() => {
        this.loadingService.stop();
      })
    );
  }

  private isPollingRequest(url: string): boolean {
    const pollingPatterns = [
      '/notifications/poll',
      '/health',
      '/ping',
      '/websocket/status',
    ];
    return pollingPatterns.some((pattern) => url.includes(pattern));
  }
}

export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  const interceptor = inject(LoadingInterceptor);
  return interceptor.intercept(req, next);
};