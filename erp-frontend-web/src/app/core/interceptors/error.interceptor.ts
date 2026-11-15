import { Injectable, inject } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse,
  HttpInterceptorFn,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ToastService } from '../../shared/components/toast/toast.service';
import { Router } from '@angular/router';

export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  errors?: ValidationError[];
  [key: string]: any;
}

export interface ValidationError {
  field: string;
  message: string;
  code: string;
  rejectedValue?: any;
}

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  private readonly toastService = inject(ToastService);
  private readonly router = inject(Router);

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        this.handleError(error);
        return throwError(() => error);
      })
    );
  }

  private handleError(error: HttpErrorResponse): void {
    // Não mostra toast para erros de autenticação (tratados pelo auth interceptor)
    if (error.status === 401) {
      return;
    }

    // Não mostra toast para requisições canceladas
    if (error.status === 0 && error.name === 'HttpErrorResponse') {
      // Pode ser erro de rede
      if (!navigator.onLine) {
        this.toastService.error('Sem conexão', { message: 'Verifique sua conexão com a internet' });
      }
      return;
    }

    const problemDetail = error.error as ProblemDetail;

    // RFC 7807 ProblemDetail
    if (problemDetail && problemDetail.title) {
      this.toastService.showProblemDetail(problemDetail);
      return;
    }

    // Erros genéricos por status
    switch (error.status) {
      case 400:
        this.toastService.error('Requisição inválida', {
          message: problemDetail?.detail || 'Dados enviados são inválidos',
        });
        break;
      case 403:
        this.toastService.error('Acesso negado', {
          message: 'Você não tem permissão para realizar esta ação',
        });
        break;
      case 404:
        this.toastService.error('Não encontrado', {
          message: 'O recurso solicitado não foi encontrado',
        });
        break;
      case 409:
        this.toastService.error('Conflito', {
          message: problemDetail?.detail || 'Já existe um registro com esses dados',
        });
        break;
      case 422:
        this.toastService.error('Erro de validação', {
          message: this.formatValidationErrors(problemDetail?.errors),
          duration: 10000,
        });
        break;
      case 429:
        this.toastService.warning('Muitas requisições', {
          message: 'Aguarde um momento antes de tentar novamente',
        });
        break;
      case 500:
        this.toastService.error('Erro interno do servidor', {
          message: 'Nossa equipe foi notificada. Tente novamente em alguns instantes.',
        });
        break;
      case 503:
        this.toastService.error('Serviço indisponível', {
          message: 'O serviço está temporariamente indisponível. Tente novamente mais tarde.',
        });
        break;
      default:
        this.toastService.error('Erro inesperado', {
          message: problemDetail?.detail || error.message || 'Ocorreu um erro inesperado',
        });
    }
  }

  private formatValidationErrors(errors?: ValidationError[]): string {
    if (!errors || errors.length === 0) {
      return 'Dados inválidos';
    }

    if (errors.length === 1) {
      return `${errors[0].field}: ${errors[0].message}`;
    }

    return errors.slice(0, 3).map((e) => `${e.field}: ${e.message}`).join('; ') + (errors.length > 3 ? '...' : '');
  }
}

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const interceptor = inject(ErrorInterceptor);
  return interceptor.intercept(req, next);
};