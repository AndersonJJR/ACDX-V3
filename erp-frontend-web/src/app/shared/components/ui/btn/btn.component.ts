import {
  Component,
  input,
  output,
  computed,
  HostListener,
} from '@angular/core';
import { CommonModule } from '@angular/common';

export type BtnVariant = 'primary' | 'secondary' | 'danger' | 'success' | 'warning' | 'ghost' | 'outline';
export type BtnSize = 'sm' | 'md' | 'lg';

@Component({
  selector: 'app-btn',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './btn.component.html',
  styleUrl: './btn.component.scss',
})
export class BtnComponent {
  variant = input<BtnVariant>('primary');
  size = input<BtnSize>('md');
  disabled = input(false);
  loading = input(false);
  type = input<'button' | 'submit' | 'reset'>('button');
  fullWidth = input(false);
  icon = input<string>('');
  iconPosition = input<'left' | 'right'>('left');
  ariaLabel = input<string>('');

  click = output<Event>();

  readonly classes = computed(() => {
    const base = 'inline-flex items-center justify-center gap-2 font-medium transition-all duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:opacity-50 disabled:pointer-events-none';
    const variants: Record<BtnVariant, string> = {
      primary: 'bg-primary-600 text-white hover:bg-primary-700 focus-visible:ring-primary-500',
      secondary: 'bg-neutral-100 text-neutral-900 hover:bg-neutral-200 focus-visible:ring-neutral-500 dark:bg-neutral-800 dark:text-neutral-100 dark:hover:bg-neutral-700',
      danger: 'bg-danger-600 text-white hover:bg-danger-700 focus-visible:ring-danger-500',
      success: 'bg-success-600 text-white hover:bg-success-700 focus-visible:ring-success-500',
      warning: 'bg-warning-600 text-white hover:bg-warning-700 focus-visible:ring-warning-500',
      ghost: 'bg-transparent hover:bg-neutral-100 dark:hover:bg-neutral-800 focus-visible:ring-neutral-500',
      outline: 'border border-neutral-300 bg-transparent hover:bg-neutral-50 dark:hover:bg-neutral-800 focus-visible:ring-neutral-500 dark:border-neutral-600',
    };
    const sizes: Record<BtnSize, string> = {
      sm: 'px-3 py-1.5 text-xs gap-1.5',
      md: 'px-4 py-2.5 text-sm gap-2',
      lg: 'px-6 py-3 text-base gap-2',
    };
    const width = this.fullWidth() ? 'w-full' : '';

    return `${base} ${variants[this.variant()]} ${sizes[this.size()]} ${width}`;
  });

  onClick(event: Event): void {
    if (this.disabled() || this.loading()) return;
    this.click.emit(event);
  }

  @HostListener('keydown.enter', ['$event'])
  @HostListener('keydown.space', ['$event'])
  onKeyDown(event: KeyboardEvent): void {
    if (this.disabled() || this.loading()) return;
    event.preventDefault();
    this.click.emit(event as unknown as Event);
  }
}