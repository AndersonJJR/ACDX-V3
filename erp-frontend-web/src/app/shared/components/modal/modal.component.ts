import {
  Component,
  input,
  output,
  signal,
  computed,
  effect,
  HostListener,
  ElementRef,
  ViewChild,
  inject,
  OnInit,
  OnDestroy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FocusTrap } from '@angular/cdk/a11y';

export type ModalSize = 'sm' | 'md' | 'lg' | 'xl' | 'full';

export interface ModalConfig {
  title?: string;
  description?: string;
  size?: ModalSize;
  closable?: boolean;
  closeOnBackdropClick?: boolean;
  closeOnEscape?: boolean;
  showHeader?: boolean;
  showFooter?: boolean;
  headerActions?: ModalAction[];
  footerActions?: ModalAction[];
}

export interface ModalAction {
  label: string;
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  icon?: string;
  disabled?: boolean;
  loading?: boolean;
  action: () => void | Promise<void>;
}

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.scss',
})
export class ModalComponent implements OnInit, OnDestroy {
  private elementRef = inject(ElementRef);
  private focusTrap = inject(FocusTrap);

  isOpen = input.required<boolean>();
  config = input<ModalConfig>({});

  closed = output<void>();
  actionClick = output<{ action: ModalAction; index: number }>();

  @ViewChild('modalContainer') modalContainer?: ElementRef<HTMLDivElement>;
  @ViewChild('focusTrap') focusTrapElement?: ElementRef<HTMLElement>;

  private _isAnimating = signal(false);
  private _previousActiveElement: HTMLElement | null = null;
  private _bodyOverflow = '';

  readonly isVisible = computed(() => this.isOpen() || this._isAnimating());
  readonly sizeClass = computed(() => this.getSizeClass(this.config().size || 'md'));
  readonly showHeader = computed(() => this.config().showHeader !== false && (this.config().title || this.config().description));
  readonly showFooter = computed(() => this.config().showFooter !== false && this.config().footerActions && this.config().footerActions!.length > 0);

  ngOnInit(): void {
    effect(() => {
      if (this.isOpen()) {
        this.open();
      } else {
        this.close();
      }
    });
  }

  ngOnDestroy(): void {
    this.restoreBodyScroll();
  }

  private open(): void {
    this._isAnimating.set(true);
    this._previousActiveElement = document.activeElement as HTMLElement;
    this._bodyOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    document.body.classList.add('modal-open');

    // Focus trap will be activated after view init
    setTimeout(() => {
      this.focusTrapElement?.nativeElement?.focus();
      this._isAnimating.set(false);
    }, 0);
  }

  private close(): void {
    this._isAnimating.set(true);
    setTimeout(() => {
      this.restoreBodyScroll();
      this._isAnimating.set(false);
      this.closed.emit();
    }, 200);
  }

  private restoreBodyScroll(): void {
    document.body.style.overflow = this._bodyOverflow;
    document.body.classList.remove('modal-open');
    this._previousActiveElement?.focus();
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget && this.config().closeOnBackdropClick !== false) {
      this.closeModal();
    }
  }

  onCloseClick(): void {
    if (this.config().closable !== false) {
      this.closeModal();
    }
  }

  closeModal(): void {
    // This would need a way to communicate back to parent
    // For now, emit closed event
    this.closed.emit();
  }

  onActionClick(action: ModalAction, index: number): void {
    if (action.disabled || action.loading) return;
    this.actionClick.emit({ action, index });
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscapeKey(event: KeyboardEvent): void {
    if (this.isOpen() && this.config().closeOnEscape !== false) {
      this.closeModal();
    }
  }

  @HostListener('document:keydown.tab', ['$event'])
  onTabKey(event: KeyboardEvent): void {
    if (!this.isOpen()) return;
    // Focus trap handles this
  }

  getSizeClass(size: ModalSize): string {
    const classes: Record<ModalSize, string> = {
      sm: 'max-w-sm',
      md: 'max-w-md',
      lg: 'max-w-lg',
      xl: 'max-w-2xl',
      full: 'max-w-[90vw]',
    };
    return classes[size] || classes.md;
  }

  getFocusableElements(): HTMLElement[] {
    const container = this.modalContainer?.nativeElement;
    if (!container) return [];

    const focusableSelectors = [
      'button:not([disabled])',
      '[href]',
      'input:not([disabled])',
      'select:not([disabled])',
      'textarea:not([disabled])',
      '[tabindex]:not([tabindex="-1"])',
    ].join(',');

    return Array.from(container.querySelectorAll<HTMLElement>(focusableSelectors));
  }
}