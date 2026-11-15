import {
  Component,
  input,
  output,
  signal,
  computed,
  ContentChildren,
  QueryList,
  AfterContentInit,
  OnDestroy,
} from '@angular/core';
import { CommonModule } from '@angular/common';

export interface TabConfig {
  id: string;
  label: string;
  icon?: string;
  disabled?: boolean;
  badge?: string | number;
  badgeVariant?: 'primary' | 'success' | 'warning' | 'danger';
}

@Component({
  selector: 'app-tab',
  standalone: true,
  imports: [CommonModule],
  template: `
    <ng-content></ng-content>
  `,
})
export class TabComponent {
  id = input.required<string>();
  label = input.required<string>();
  icon = input<string>('');
  disabled = input(false);
  badge = input<string | number>('');
  badgeVariant = input<'primary' | 'success' | 'warning' | 'danger'>('primary');
}

@Component({
  selector: 'app-tab-set',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tab-set.component.html',
  styleUrl: './tab-set.component.scss',
})
export class TabSetComponent implements AfterContentInit, OnDestroy {
  @ContentChildren(TabComponent) tabs!: QueryList<TabComponent>;

  activeTabId = input<string>('');
  variant = input<'line' | 'pills' | 'enclosed'>('line');
  lazyLoad = input(false);
  fullWidth = input(false);

  tabChange = output<string>();

  private _activeTabId = signal<string>('');
  private _initialized = false;

  readonly activeId = computed(() => this.activeTabId() || this._activeTabId());
  readonly activeTab = computed(() => this.tabs.find((t) => t.id() === this.activeId()));
  readonly tabsList = computed(() => this.tabs.toArray());

  ngAfterContentInit(): void {
    this._initialized = true;
    this.syncActiveTab();

    this.tabs.changes.subscribe(() => {
      this.syncActiveTab();
    });
  }

  ngOnDestroy(): void {}

  private syncActiveTab(): void {
    if (!this._initialized) return;

    const currentActive = this.activeId();
    const tabs = this.tabsList();

    if (!currentActive || !tabs.some((t) => t.id() === currentActive)) {
      const firstEnabled = tabs.find((t) => !t.disabled());
      if (firstEnabled) {
        this._activeTabId.set(firstEnabled.id());
      }
    }
  }

  selectTab(tabId: string): void {
    const tab = this.tabsList().find((t) => t.id() === tabId);
    if (tab && !tab.disabled()) {
      this._activeTabId.set(tabId);
      this.tabChange.emit(tabId);
    }
  }

  getBadgeClass(variant: string): string {
    const classes: Record<string, string> = {
      primary: 'badge-primary',
      success: 'badge-success',
      warning: 'badge-warning',
      danger: 'badge-danger',
    };
    return classes[variant] || classes.primary;
  }

  getTabClasses(tab: TabComponent): string {
    const base = 'tab-btn';
    const active = this.activeId() === tab.id() ? 'active' : '';
    const disabled = tab.disabled() ? 'disabled' : '';
    return `${base} ${active} ${disabled}`.trim();
  }

  getVariantClasses(): string {
    const variants: Record<string, string> = {
      line: 'tab-line',
      pills: 'tab-pills',
      enclosed: 'tab-enclosed',
    };
    return variants[this.variant()] || variants.line;
  }
}