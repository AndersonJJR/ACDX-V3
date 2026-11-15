import {
  Component,
  input,
  output,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute, NavigationEnd } from '@angular/router';
import { filter, map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';

export interface BreadcrumbItem {
  label: string;
  url?: string;
  icon?: string;
  disabled?: boolean;
}

@Component({
  selector: 'app-breadcrumb',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './breadcrumb.component.html',
  styleUrl: './breadcrumb.component.scss',
})
export class BreadcrumbComponent implements OnInit {
  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);

  items = input<BreadcrumbItem[]>([]);
  autoGenerate = input(true);
  separator = input<string>('/');
  maxItems = input<number>(5);
  showHome = input(true);
  homeLabel = input('Início');
  homeIcon = input('M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z');

  homeClick = output<void>();
  itemClick = output<BreadcrumbItem>();

  private breadcrumbs = signal<BreadcrumbItem[]>([]);
  readonly displayedBreadcrumbs = computed(() => this.getDisplayBreadcrumbs());

  ngOnInit(): void {
    if (this.autoGenerate()) {
      this.generateBreadcrumbs();
      this.router.events.pipe(
        filter((event) => event instanceof NavigationEnd)
      ).subscribe(() => {
        this.generateBreadcrumbs();
      });
    }
  }

  private generateBreadcrumbs(): void {
    const items: BreadcrumbItem[] = [];

    if (this.showHome()) {
      items.push({
        label: this.homeLabel(),
        url: '/dashboard',
        icon: this.homeIcon(),
      });
    }

    let route = this.activatedRoute.root;
    let url = '';

    while (route.firstChild) {
      route = route.firstChild;
      const routeData = route.snapshot.data;
      const label = routeData['breadcrumb'] as string;

      if (label) {
        url += `/${route.snapshot.url.map((s) => s.path).join('/')}`;
        items.push({
          label,
          url: url || undefined,
        });
      }
    }

    this.breadcrumbs.set(items);
  }

  private getDisplayBreadcrumbs(): BreadcrumbItem[] {
    const manualItems = this.items();
    const autoItems = this.breadcrumbs();
    const allItems = [...manualItems, ...autoItems];

    if (allItems.length <= this.maxItems()) {
      return allItems;
    }

    // Show first, separator, and last items
    const first = allItems[0];
    const last = allItems[allItems.length - 1];
    const middleStart = Math.max(1, allItems.length - this.maxItems() + 1);
    const middle = allItems.slice(middleStart, allItems.length - 1);

    return [first, { label: '...', disabled: true }, ...middle, last];
  }

  onHomeClick(event: Event): void {
    event.preventDefault();
    this.homeClick.emit();
    this.router.navigate(['/dashboard']);
  }

  onItemClick(item: BreadcrumbItem, event: Event): void {
    if (item.disabled || !item.url) return;
    event.preventDefault();
    this.itemClick.emit(item);
    this.router.navigateByUrl(item.url!);
  }

  getSeparatorIcon(): string {
    return 'M9 5l7 7-7 7';
  }
}