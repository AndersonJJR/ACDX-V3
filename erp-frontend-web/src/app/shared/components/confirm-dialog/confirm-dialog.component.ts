import { Component, input, output, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent, ModalConfig, ModalAction } from '../modal/modal.component';

export interface ConfirmDialogConfig {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  confirmVariant?: 'primary' | 'danger' | 'success' | 'warning';
  icon?: string;
  iconColor?: 'primary' | 'danger' | 'success' | 'warning';
  size?: 'sm' | 'md';
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, ModalComponent],
  templateUrl: './confirm-dialog.component.html',
  styleUrl: './confirm-dialog.component.scss',
})
export class ConfirmDialogComponent {
  isOpen = input.required<boolean>();
  config = input.required<ConfirmDialogConfig>();

  confirmed = output<void>();
  cancelled = output<void>();
  closed = output<void>();

  private _resolve?: (value: boolean) => void;

  readonly modalConfig = computed<ModalConfig>(() => ({
    title: this.config().title,
    size: this.config().size || 'sm',
    closable: true,
    closeOnBackdropClick: true,
    closeOnEscape: true,
    showFooter: true,
    footerActions: [
      {
        label: this.config().cancelLabel || 'Cancelar',
        variant: 'secondary',
        action: () => this.onCancel(),
      },
      {
        label: this.config().confirmLabel || 'Confirmar',
        variant: this.config().confirmVariant || 'primary',
        action: () => this.onConfirm(),
      },
    ],
  }));

  getIcon(): string {
    const icons: Record<string, string> = {
      default: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z',
      danger: 'M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z',
      success: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
      warning: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z',
      info: 'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
      question: 'M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
    };
    return icons[this.config().icon || 'default'];
  }

  getIconColorClass(): string {
    const colors: Record<string, string> = {
      primary: 'text-primary-600 dark:text-primary-400',
      danger: 'text-danger-600 dark:text-danger-400',
      success: 'text-success-600 dark:text-success-400',
      warning: 'text-warning-600 dark:text-warning-400',
    };
    return colors[this.config().iconColor || 'default'] || colors.primary;
  }

  onConfirm(): void {
    this.confirmed.emit();
    this._resolve?.(true);
    this._resolve = undefined;
  }

  onCancel(): void {
    this.cancelled.emit();
    this._resolve?.(false);
    this._resolve = undefined;
  }

  onClosed(): void {
    this.closed.emit();
    if (this._resolve) {
      this._resolve(false);
      this._resolve = undefined;
    }
  }

  // Promise-based API
  async confirm(): Promise<boolean> {
    return new Promise((resolve) => {
      this._resolve = resolve;
    });
  }
}