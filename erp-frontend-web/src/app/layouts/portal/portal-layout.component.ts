import {
  Component,
  input,
  output,
  computed,
  signal,
  inject,
  HostListener,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastContainerComponent } from '../../../shared/components/toast/toast-container.component';
import { BtnComponent } from '../../../shared/components/ui/btn/btn.component';

export interface PortalLayoutConfig {
  title?: string;
  showLogo?: boolean;
  logoUrl?: string;
  primaryColor?: string;
  secondaryColor?: string;
}

@Component({
  selector: 'app-portal-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, ToastContainerComponent, BtnComponent],
  templateUrl: './portal-layout.component.html',
  styleUrl: './portal-layout.component.scss',
})
export class PortalLayoutComponent {
  private authService = inject(AuthService);

  config = input<PortalLayoutConfig>({});

  logout = output<void>();

  private _userMenuOpen = signal(false);

  readonly user = computed(() => this.authService.user());
  readonly company = computed(() => this.authService.company());
  readonly userMenuOpen = computed(() => this._userMenuOpen());

  readonly primaryColor = computed(() => this.config().primaryColor || '#0039A6');
  readonly secondaryColor = computed(() => this.config().secondaryColor || '#1db833');
  readonly currentYear = new Date().getFullYear();

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-menu') && this._userMenuOpen()) {
      this._userMenuOpen.set(false);
    }
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscapeKey(event: KeyboardEvent): void {
    this._userMenuOpen.set(false);
  }

  onLogout(): void {
    this._userMenuOpen.set(false);
    this.logout.emit();
  }

  toggleUserMenu(): void {
    this._userMenuOpen.update(v => !v);
  }

  getCompanyLogo(): string {
    return this.config().logoUrl || '';
  }

  getCompanyName(): string {
    return this.company()?.tradeName || this.company()?.name || this.config().title || 'Portal';
  }
}