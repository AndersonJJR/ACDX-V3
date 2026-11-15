import { Component, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';

export type CardVariant = 'default' | 'outline' | 'elevated';

@Component({
  selector: 'app-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './card.component.html',
  styleUrl: './card.component.scss',
})
export class CardComponent {
  variant = input<CardVariant>('default');
  padding = input<'none' | 'sm' | 'md' | 'lg'>('md');
  hover = input(false);

  readonly classes = computed(() => {
    const base = 'bg-white dark:bg-neutral-900 rounded-xl';
    const variants: Record<CardVariant, string> = {
      default: 'shadow-card border border-neutral-200 dark:border-neutral-800',
      outline: 'border-2 border-neutral-200 dark:border-neutral-700',
      elevated: 'shadow-lg border border-neutral-200 dark:border-neutral-800',
    };
    const paddings = {
      none: '',
      sm: 'p-4',
      md: 'p-6',
      lg: 'p-8',
    };
    const hover = this.hover() ? 'hover:shadow-lg transition-shadow duration-200 cursor-pointer' : '';

    return `${base} ${variants[this.variant()]} ${paddings[this.padding()]} ${hover}`;
  });
}