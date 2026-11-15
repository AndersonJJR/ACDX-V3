import { Injectable, signal, computed, inject } from '@angular/core';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class TenantService {
  private readonly authService = inject(AuthService);

  private readonly _tenantId = signal<string | null>(null);
  private readonly _tenantSlug = signal<string | null>(null);

  readonly tenantId = computed(() => this._tenantId() ?? this.authService.company()?.id ?? null);
  readonly tenantSlug = computed(() => this._tenantSlug() ?? this.authService.company()?.cnpj?.replace(/[^\d]/g, '') ?? null);

  setTenant(tenantId: string, slug?: string): void {
    this._tenantId.set(tenantId);
    if (slug) this._tenantSlug.set(slug);
  }

  clearTenant(): void {
    this._tenantId.set(null);
    this._tenantSlug.set(null);
  }

  getTenantHeader(): string | null {
    return this.tenantId();
  }
}