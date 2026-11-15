import {
  Component,
  input,
  output,
  signal,
  computed,
  effect,
  HostListener,
  ViewChild,
  ElementRef,
  inject,
  OnInit,
  OnDestroy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FocusTrap } from '@angular/cdk/a11y';

export type DrawerPosition = 'left' | 'right';
export type DrawerSize = 'sm' | 'md' | 'lg' | 'xl' | 'full';

export interface DrawerConfig {
  title?: string;
  description?: string;
  position?: DrawerPosition;
  size?: DrawerSize;
  closable?: boolean;
  closeOnBackdropClick?: boolean;
  closeOnEscape?: boolean;
  showHeader?: boolean;
  showFooter?: boolean;
  headerActions?: DrawerAction[];
  footerActions?: DrawerAction[];
}

export interface DrawerAction {
  label: string;
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  icon?: string;
  disabled?: boolean;
  loading?: boolean;
  action: () => void | Promise<void>;
}

@Component({
  selector: 'app-drawer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './drawer.component.html',
  styleUrl: './drawer.component.scss',
})
export class DrawerComponent implements OnInit, OnDestroy {
  private focusTrap = inject(FocusTrap);

  isOpen = input.required<boolean>();
  config = input<DrawerConfig>({});

  closed = output<void>();
  actionClick = output<{ action: DrawerAction; index: number }>();

  @ViewChild('drawerContainer') drawerContainer?: ElementRef<HTMLDivElement>;
  @ViewChild('focusTrap') focusTrapElement?: ElementRef<HTMLElement>;

  private _isAnimating = signal(false);
  private _previousActiveElement: HTMLElement | null = null;
  private _bodyOverflow = '';

  readonly isVisible = computed(() => this.isOpen() || this._isAnimating());
  readonly position = computed(() => this.config().position || 'right');
  readonly sizeClass = computed(() => this.getSizeClass(this.config().size || 'md'));
  readonly positionClass = computed(() => this.position() === 'left' ? 'left-0' : 'right-0');
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
    document.body.classList.add('drawer-open');

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
    }, 300);
  }

  private restoreBodyScroll(): void {
    document.body.style.overflow = this._bodyOverflow;
    document.body.classList.remove('drawer-open');
    this._previousActiveElement?.focus();
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget && this.config().closeOnBackdropClick !== false) {
      this.closeDrawer();
    }
  }

  onCloseClick(): void {
    if (this.config().closable !== false) {
      this.closeDrawer();
    }
  }

  closeDrawer(): void {
    this.closed.emit();
  }

  onActionClick(action: DrawerAction, index: number): void {
    if (action.disabled || action.loading) return;
    this.actionClick.emit({ action, index });
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscapeKey(event: KeyboardEvent): void {
    if (this.isOpen() && this.config().closeOnEscape !== false) {
      this.closeDrawer();
    }
  }

  getSizeClass(size: DrawerSize): string {
    const classes: Record<DrawerSize, string> = {
      sm: 'w-72',
      md: 'w-96',
      lg: 'w-[32rem]',
      xl: 'w-[48rem]',
      full: 'w-full max-w-[90vw]',
    };
    return classes[size] || classes.md;
  }
}