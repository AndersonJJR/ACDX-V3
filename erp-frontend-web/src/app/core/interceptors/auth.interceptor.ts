import { Injectable, inject } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse,
  HttpInterceptorFn,
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject, catchError, switchMap, filter, take } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private readonly authService = inject(AuthService);
  private isRefreshing = false;
  private refreshTokenSubject = new BehaviorSubject<string | null>(null);

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Não adiciona token em requisições de auth
    if (this.isAuthRequest(req.url)) {
      return next.handle(req);
    }

    const accessToken = this.authService.accessToken();
    if (accessToken) {
      req = this.addToken(req, accessToken);
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && !this.isAuthRequest(req.url)) {
          return this.handle401Error(req, next);
        }
        return throwError(() => error);
      })
    );
  }

  private isAuthRequest(url: string): boolean {
    return url.includes('/auth/login') || url.includes('/auth/refresh') || url.includes('/auth/logout');
  }

  private addToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
    return req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  private handle401Error(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      return this.authService.refreshAccessToken().pipe(
        switchMap((success) => {
          this.isRefreshing = false;
          if (success) {
            const newToken = this.authService.accessToken();
            if (newToken) {
              this.refreshTokenSubject.next(newToken);
              return next.handle(this.addToken(req, newToken));
            }
          }
          this.authService.logout();
          return throwError(() => new Error('Token refresh failed'));
        }),
        catchError((err) => {
          this.isRefreshing = false;
          this.authService.logout();
          return throwError(() => err);
        })
      );
    } else {
      return this.refreshTokenSubject.pipe(
        filter((token) => token !== null),
        take(1),
        switchMap((token) => next.handle(this.addToken(req, token!)))
      );
    }
  }
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const interceptor = inject(AuthInterceptor);
  return interceptor.intercept(req, next);
};