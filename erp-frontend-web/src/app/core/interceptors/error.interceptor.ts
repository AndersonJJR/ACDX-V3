import { inject } from '@angular/core';
import {
  HttpInterceptorFn,
  HttpRequest,
  HttpHandlerFn,
  HttpErrorResponse,
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

export const errorInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<any> => {
  const toastService = inject(ToastService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      handleError(error, toastService, router);
      return throwError(() => error);
    })
  );
};

function handleError(error: HttpErrorResponse, toastService: ToastService, router: Router): void {
  // Não mostra toast para erros de autenticação (tratados pelo auth interceptor)
  if (error.status === 401) {
    return;
  }

  // Não mostra toast para requisições canceladas
  if (error.status === 0 && error.name === 'HttpErrorResponse') {
    // Pode ser erro de rede
    if (!navigator.onLine) {
      toastService.error('Sem conexão', { message: 'Verifique sua conexão com a internet' });
    }
    return;
  }

  const problemDetail = error.error as ProblemDetail;

  // RFC 7807 ProblemDetail
  if (problemDetail && problemDetail.title) {
    toastService.showProblemDetail(problemDetail);
    return;
  }

  // Erros genéricos por status
  switch (error.status) {
    case 400:
      toastService.error('Requisição inválida', {
        message: problemDetail?.detail || 'Dados enviados são inválidos',
      });
      break;
    case 403:
      toastService.error('Acesso negado', {
        message: 'Você não tem permissão para realizar esta ação',
      });
      break;
    case 404:
      toastService.error('Não encontrado', {
        message: 'O recurso solicitado não foi encontrado',
      });
      break;
    case 409:
      toastService.error('Conflito', {
        message: problemDetail?.detail || 'Já existe um registro com esses dados',
      });
      break;
    case 422:
      toastService.error('Erro de validação', {
        message: formatValidationErrors(problemDetail?.errors),
        duration: 10000,
      });
      break;
    case 429:
      toastService.warning('Muitas requisições', {
        message: 'Aguarde um momento antes de tentar novamente',
      });
      break;
    case 500:
      toastService.error('Erro interno do servidor', {
        message: 'Nossa equipe foi notificada. Tente novamente em alguns instantes.',
      });
      break;
    case 503:
      toastService.error('Serviço indisponível', {
        message: 'O serviço está temporariamente indisponível. Tente novamente mais tarde.',
      });
      break;
    default:
      toastService.error('Erro inesperado', {
        message: problemDetail?.detail || error.message || 'Ocorreu um erro inesperado',
      });
  }
}

function formatValidationErrors(errors?: ValidationError[]): string {
  if (!errors || errors.length === 0) {
    return 'Dados inválidos';
  }

  if (errors.length === 1) {
    return `${errors[0].field}: ${errors[0].message}`;
  }

  return errors.slice(0, 3).map((e) => `${e.field}: ${e.message}`).join('; ') + (errors.length > 3 ? '...' : '');
}