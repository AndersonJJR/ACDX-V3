import { inject } from '@angular/core';
import {
  HttpInterceptorFn,
  HttpRequest,
  HttpHandlerFn,
} from '@angular/common/http';
import { Observable, finalize } from 'rxjs';
import { LoadingService } from '../services/loading.service';

export const loadingInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<any> => {
  const loadingService = inject(LoadingService);

  // Não mostra loading para requisições com header 'X-Skip-Loading'
  if (req.headers.has('X-Skip-Loading')) {
    return next(req);
  }

  // Não mostra loading para requisições de polling/heartbeat
  if (isPollingRequest(req.url)) {
    return next(req);
  }

  loadingService.start();

  return next(req).pipe(
    finalize(() => {
      loadingService.stop();
    })
  );
};

function isPollingRequest(url: string): boolean {
  const pollingPatterns = [
    '/notifications/poll',
    '/health',
    '/ping',
    '/websocket/status',
  ];
  return pollingPatterns.some((pattern) => url.includes(pattern));
}