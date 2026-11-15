import {
  Component,
  input,
  output,
  computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { BreadcrumbComponent } from '../breadcrumb/breadcrumb.component';
import { BreadcrumbItem } from '../breadcrumb/breadcrumb.component';

export interface PageHeaderAction {
  label: string;
  icon?: string;
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  disabled?: boolean;
  loading?: boolean;
  click: () => void | Promise<void>;
}

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule, BreadcrumbComponent],
  templateUrl: './page-header.component.html',
  styleUrl: './page-header.component.scss',
})
export class PageHeaderComponent {
  title = input.required<string>();
  subtitle?: string;
  description = input<string>('');
  breadcrumbs = input<BreadcrumbItem[]>([]);
  actions = input<PageHeaderAction[]>([]);
  showBreadcrumb = input(true);
  showActions = input(true);

  actionClick = output<PageHeaderAction>();

  readonly hasDescription = computed(() => !!this.description());
  readonly hasActions = computed(() => this.showActions() && this.actions().length > 0);
}