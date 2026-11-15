import { Injectable, signal, computed, inject } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: string;
  type: ToastType;
  title: string;
  message?: string;
  duration?: number;
  closable?: boolean;
  action?: {
    label: string;
    callback: () => void;
  };
}

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private readonly _toasts = signal<Toast[]>([]);

  readonly toasts = computed(() => this._toasts());

  private generateId(): string {
    return `toast-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  private addToast(type: ToastType, title: string, options?: Partial<Omit<Toast, 'id' | 'type'>>): string {
    const id = this.generateId();
    const toast: Toast = {
      id,
      type,
      title,
      message: options?.message,
      duration: options?.duration ?? 5000,
      closable: options?.closable ?? true,
      action: options?.action,
    };

    this._toasts.update((toasts) => [...toasts, toast]);

    // Auto-dismiss
    if (toast.duration && toast.duration > 0) {
      setTimeout(() => this.remove(id), toast.duration);
    }

    return id;
  }

  success(title: string, options?: Partial<Omit<Toast, 'id' | 'type'>>): string {
    return this.addToast('success', title, options);
  }

  error(title: string, options?: Partial<Omit<Toast, 'id' | 'type'>>): string {
    return this.addToast('error', title, { duration: 8000, ...options });
  }

  warning(title: string, options?: Partial<Omit<Toast, 'id' | 'type'>>): string {
    return this.addToast('warning', title, { duration: 6000, ...options });
  }

  info(title: string, options?: Partial<Omit<Toast, 'id' | 'type'>>): string {
    return this.addToast('info', title, options);
  }

  remove(id: string): void {
    this._toasts.update((toasts) => toasts.filter((t) => t.id !== id));
  }

  clear(): void {
    this._toasts.set([]);
  }

  // Método para mostrar erros do RFC 7807 ProblemDetail
  showProblemDetail(problem: any): string {
    if (!problem) {
      return this.error('Erro desconhecido');
    }

    const title = problem.title || 'Erro';
    const detail = problem.detail || problem.message || 'Ocorreu um erro inesperado';

    // Se tiver erros de validação, mostra o primeiro
    if (problem.errors && Array.isArray(problem.errors) && problem.errors.length > 0) {
      const firstError = problem.errors[0];
      const errorMsg = firstError.detail || firstError.message || JSON.stringify(firstError);
      return this.error(title, { message: errorMsg, duration: 10000 });
    }

    return this.error(title, { message: detail, duration: 8000 });
  }
}