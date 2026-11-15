import { Component, computed, inject, input, signal, HostListener, OnInit, OnDestroy, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, Toast, ToastType } from './toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.scss',
})
export class ToastComponent implements OnInit, OnDestroy {
  private toastService = inject(ToastService);

  toast = input.required<Toast>();

  isVisible = signal(true);
  progress = signal(100);
  private progressInterval?: ReturnType<typeof setInterval>;

  ngOnInit(): void {
    const duration = this.toast().duration;
    if (duration && duration > 0) {
      const steps = 100;
      const interval = duration / steps;
      let currentProgress = 100;

      this.progressInterval = setInterval(() => {
        currentProgress -= 100 / steps;
        this.progress.set(Math.max(0, currentProgress));
        if (currentProgress <= 0) {
          this.dismiss();
        }
      }, interval);
    }
  }

  ngOnDestroy(): void {
    if (this.progressInterval) {
      clearInterval(this.progressInterval);
    }
  }

  dismiss(): void {
    this.isVisible.set(false);
    // Aguarda animação de saída antes de remover
    setTimeout(() => {
      this.toastService.remove(this.toast().id);
    }, 300);
  }

  executeAction(): void {
    const action = this.toast().action;
    if (action) {
      action.callback();
      this.dismiss();
    }
  }

  @HostListener('mouseenter')
  onMouseEnter(): void {
    if (this.progressInterval) {
      clearInterval(this.progressInterval);
      this.progressInterval = undefined;
    }
  }

  @HostListener('mouseleave')
  onMouseLeave(): void {
    const duration = this.toast().duration;
    if (duration && duration > 0 && this.isVisible()) {
      const remainingProgress = this.progress();
      if (remainingProgress > 0) {
        const steps = Math.ceil(remainingProgress);
        const interval = (duration * remainingProgress / 100) / steps;
        let currentProgress = remainingProgress;

        this.progressInterval = setInterval(() => {
          currentProgress -= 100 / (duration / interval);
          this.progress.set(Math.max(0, currentProgress));
          if (currentProgress <= 0) {
            this.dismiss();
          }
        }, interval);
      }
    }
  }

  getIcon(): string {
    const icons: Record<ToastType, string> = {
      success: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
      error: 'M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z',
      warning: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z',
      info: 'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
    };
    return icons[this.toast().type] || icons.info;
  }

  getIconClass(): string {
    const classes: Record<ToastType, string> = {
      success: 'text-success-600 dark:text-success-400',
      error: 'text-danger-600 dark:text-danger-400',
      warning: 'text-warning-600 dark:text-warning-400',
      info: 'text-primary-600 dark:text-primary-400',
    };
    return classes[this.toast().type] || classes.info;
  }

  getBgClass(): string {
    const classes: Record<ToastType, string> = {
      success: 'bg-success-50 dark:bg-success-900/20 border-success-200 dark:border-success-800',
      error: 'bg-danger-50 dark:bg-danger-900/20 border-danger-200 dark:border-danger-800',
      warning: 'bg-warning-50 dark:bg-warning-900/20 border-warning-200 dark:border-warning-800',
      info: 'bg-primary-50 dark:bg-primary-900/20 border-primary-200 dark:border-primary-800',
    };
    return classes[this.toast().type] || classes.info;
  }
}