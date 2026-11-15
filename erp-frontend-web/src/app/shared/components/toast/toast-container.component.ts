import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastComponent } from './toast.component';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule, ToastComponent],
  template: `
    <div class="fixed top-4 right-4 z-[100] flex flex-col gap-2 w-full max-w-sm sm:max-w-md pointer-events-none">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="pointer-events-auto">
          <app-toast [toast]="toast" />
        </div>
      }
    </div>
  `,
})
export class ToastContainerComponent {
  toastService = inject(ToastService);
}