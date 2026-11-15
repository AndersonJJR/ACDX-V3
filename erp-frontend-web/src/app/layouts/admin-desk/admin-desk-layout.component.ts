import {
  Component,
  input,
  output,
  signal,
  computed,
  effect,
  inject,
  OnInit,
  OnDestroy,
  HostListener,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService, MenuItem } from '../../../core/services/auth.service';
import { ToastContainerComponent } from '../../../shared/components/toast/toast-container.component';
import { BtnComponent } from '../../../shared/components/ui/btn/btn.component';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

export interface AdminDeskLayoutConfig {
  sidebarCollapsed?: boolean;
  showNotifications?: boolean;
  showSearch?: boolean;
}

@Component({
  selector: 'app-admin-desk-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ToastContainerComponent,
    BtnComponent,
    BreadcrumbComponent,
    PageHeaderComponent,
  ],
  templateUrl: './admin-desk-layout.component.html',
  styleUrl: './admin-desk-layout.component.scss',
})
export class AdminDeskLayoutComponent implements OnInit, OnDestroy {
  private authService = inject(AuthService);
  private router = inject(Router);

  config = input<AdminDeskLayoutConfig>({});

  sidebarToggle = output<boolean>();
  notificationsToggle = output<void>();
  profileClick = output<void>();
  companySwitch = output<void>();
  logout = output<void>();

  private _sidebarCollapsed = signal(false);
  private _mobileSidebarOpen = signal(false);
  private _notificationsOpen = signal(false);
  private _userMenuOpen = signal(false);
  private _searchQuery = signal('');

  readonly sidebarCollapsed = computed(() => this._sidebarCollapsed() || this.config().sidebarCollapsed);
  readonly mobileSidebarOpen = computed(() => this._mobileSidebarOpen());
  readonly notificationsOpen = computed(() => this._notificationsOpen());
  readonly userMenuOpen = computed(() => this._userMenuOpen());
  readonly searchQuery = computed(() => this._searchQuery());
  readonly user = computed(() => this.authService.user());
  readonly company = computed(() => this.authService.company());
  readonly menus = computed(() => this.authService.getMenuItems());
  readonly isMobile = signal(false);

  ngOnInit(): void {
    this.checkMobile();
    window.addEventListener('resize', this.checkMobile.bind(this));

    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd)
    ).subscribe(() => {
      if (this._mobileSidebarOpen()) {
        this._mobileSidebarOpen.set(false);
      }
    });
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.checkMobile.bind(this));
  }

  private checkMobile(): void {
    this.isMobile.set(window.innerWidth < 1024);
    if (!this.isMobile()) {
      this._mobileSidebarOpen.set(false);
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-menu') && this._userMenuOpen()) {
      this._userMenuOpen.set(false);
    }
    if (!target.closest('.notifications-panel') && this._notificationsOpen()) {
      this._notificationsOpen.set(false);
    }
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscapeKey(event: KeyboardEvent): void {
    this._mobileSidebarOpen.set(false);
    this._notificationsOpen.set(false);
    this._userMenuOpen.set(false);
  }

  toggleSidebar(): void {
    if (this.isMobile()) {
      this._mobileSidebarOpen.update((v) => !v);
    } else {
      this._sidebarCollapsed.update((v) => !v);
      this.sidebarToggle.emit(!this._sidebarCollapsed());
    }
  }

  closeMobileSidebar(): void {
    this._mobileSidebarOpen.set(false);
  }

  toggleNotifications(): void {
    this._notificationsOpen.update((v) => !v);
    this.notificationsToggle.emit();
  }

  toggleUserMenu(): void {
    this._userMenuOpen.update((v) => !v);
  }

  onProfileClick(): void {
    this._userMenuOpen.set(false);
    this.profileClick.emit();
  }

  onCompanySwitch(): void {
    this._userMenuOpen.set(false);
    this.companySwitch.emit();
  }

  onLogout(): void {
    this._userMenuOpen.set(false);
    this.logout.emit();
  }

  onSearchChange(event: Event): void {
    this._searchQuery.set((event.target as HTMLInputElement).value);
  }

  getMenuItems(): MenuItem[] {
    return this.menus().filter((menu) => {
      if (menu.permission && !this.authService.hasPermission(menu.permission)) return false;
      if (menu.children) {
        return menu.children.some((child) => !child.permission || this.authService.hasPermission(child.permission));
      }
      return true;
    });
  }

  hasMenuChildren(menu: MenuItem): boolean {
    return !!menu.children && menu.children!.length > 0;
  }

  getVisibleChildren(menu: MenuItem): MenuItem[] {
    return menu.children?.filter((child) => !child.permission || this.authService.hasPermission(child.permission)) || [];
  }

  trackByMenuId(index: number, menu: MenuItem): string {
    return menu.key;
  }

  trackByChildId(index: number, child: MenuItem): string {
    return child.key;
  }

  getMenuIcon(menu: MenuItem): string {
    return menu.icon || 'M10 20l4-16m4 16l4-16m-4 16l-4-16m4 16l-4-16';
  }

  getChildIcon(child: MenuItem): string {
    return child.icon || 'M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122';
  }
}