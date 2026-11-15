import { Injectable, signal, computed, effect } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { ToastService } from '../../shared/components/toast/toast.service';

export interface User {
  id: string;
  name: string;
  email: string;
  avatar?: string;
  roles: string[];
  permissions: string[];
  companies: Company[];
  currentCompanyId?: string;
}

export interface Company {
  id: string;
  name: string;
  tradeName: string;
  cnpj: string;
  enabledModules: string[];
  settings: CompanySettings;
}

export interface CompanySettings {
  timezone: string;
  currency: string;
  dateFormat: string;
  fiscalYearStart: number; // month 1-12
}

export interface BootInfo {
  user: User;
  company: Company;
  menus: MenuItem[];
}

export interface MenuItem {
  key: string;
  label: string;
  icon?: string;
  route?: string;
  permission?: string;
  children?: MenuItem[];
  order: number;
}

export interface LoginRequest {
  email: string;
  password: string;
  companyId?: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  bootInfo: BootInfo;
}

export interface RefreshTokenResponse {
  accessToken: string;
  expiresIn: number;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);

  private readonly _user = signal<User | null>(null);
  private readonly _company = signal<Company | null>(null);
  private readonly _menus = signal<MenuItem[]>([]);
  private readonly _accessToken = signal<string | null>(null);
  private readonly _refreshToken = signal<string | null>(null);
  private readonly _isLoading = signal(false);
  private readonly _bootInfo = signal<BootInfo | null>(null);

  readonly user = computed(() => this._user());
  readonly company = computed(() => this._company());
  readonly menus = computed(() => this._menus());
  readonly accessToken = computed(() => this._accessToken());
  readonly isAuthenticated = computed(() => !!this._accessToken() && !!this._user());
  readonly isLoading = computed(() => this._isLoading());
  readonly bootInfo = computed(() => this._bootInfo());

  readonly currentCompany = computed(() => {
    const user = this._user();
    const companyId = user?.currentCompanyId;
    if (!companyId) return this._company();
    return this._user()?.companies.find((c) => c.id === companyId) ?? this._company();
  });

  readonly permissions = computed(() => this._user()?.permissions ?? []);
  readonly roles = computed(() => this._user()?.roles ?? []);
  readonly enabledModules = computed(() => this._company()?.enabledModules ?? []);

  constructor() {
    // Restaura tokens do localStorage na inicialização
    this.restoreSession();

    // Effect para persistir tokens
    effect(() => {
      const token = this._accessToken();
      const refresh = this._refreshToken();
      if (token && refresh) {
        localStorage.setItem('access_token', token);
        localStorage.setItem('refresh_token', refresh);
      } else {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
      }
    });
  }

  private restoreSession(): void {
    const accessToken = localStorage.getItem('access_token');
    const refreshToken = localStorage.getItem('refresh_token');

    if (accessToken && refreshToken) {
      this._accessToken.set(accessToken);
      this._refreshToken.set(refreshToken);
      // Tenta obter boot info com token salvo
      this.fetchBootInfo().catch(() => this.clearSession());
    }
  }

  async login(credentials: LoginRequest): Promise<boolean> {
    this._isLoading.set(true);

    try {
      const response = await this.http
        .post<LoginResponse>(`${environment.apiUrl}/auth/login`, credentials)
        .toPromise();

      if (response) {
        this.setSession(response);
        return true;
      }
      return false;
    } catch (error: any) {
      this.toastService.showProblemDetail(error?.error);
      return false;
    } finally {
      this._isLoading.set(false);
    }
  }

  async logout(): Promise<void> {
    this._isLoading.set(true);

    try {
      const refreshToken = this._refreshToken();
      if (refreshToken) {
        await this.http.post(`${environment.apiUrl}/auth/logout`, { refreshToken }).toPromise();
      }
    } catch {
      // Ignora erros de logout no servidor
    } finally {
      this.clearSession();
      this.router.navigate(['/login']);
      this._isLoading.set(false);
    }
  }

  async refreshAccessToken(): Promise<boolean> {
    const refreshToken = this._refreshToken();
    if (!refreshToken) return false;

    try {
      const response = await this.http
        .post<RefreshTokenResponse>(`${environment.apiUrl}/auth/refresh`, { refreshToken })
        .toPromise();

      if (response) {
        this._accessToken.set(response.accessToken);
        return true;
      }
      return false;
    } catch {
      this.clearSession();
      this.router.navigate(['/login']);
      return false;
    }
  }

  async fetchBootInfo(): Promise<void> {
    try {
      const bootInfo = await this.http.get<BootInfo>(`${environment.apiUrl}/auth/boot`).toPromise();
      if (bootInfo) {
        this._bootInfo.set(bootInfo);
        this._user.set(bootInfo.user);
        this._company.set(bootInfo.company);
        this._menus.set(bootInfo.menus);
      }
    } catch (error: any) {
      this.toastService.showProblemDetail(error?.error);
      throw error;
    }
  }

  async switchCompany(companyId: string): Promise<boolean> {
    this._isLoading.set(true);

    try {
      const response = await this.http
        .post<LoginResponse>(`${environment.apiUrl}/auth/switch-company`, { companyId })
        .toPromise();

      if (response) {
        this.setSession(response);
        return true;
      }
      return false;
    } catch (error: any) {
      this.toastService.showProblemDetail(error?.error);
      return false;
    } finally {
      this._isLoading.set(false);
    }
  }

  private setSession(response: LoginResponse): void {
    this._accessToken.set(response.accessToken);
    this._refreshToken.set(response.refreshToken);
    this._bootInfo.set(response.bootInfo);
    this._user.set(response.bootInfo.user);
    this._company.set(response.bootInfo.company);
    this._menus.set(response.bootInfo.menus);
  }

  private clearSession(): void {
    this._accessToken.set(null);
    this._refreshToken.set(null);
    this._user.set(null);
    this._company.set(null);
    this._menus.set([]);
    this._bootInfo.set(null);
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
  }

  getPermissions(): string[] {
    return this._user()?.permissions ?? [];
  }

  getRoles(): string[] {
    return this._user()?.roles ?? [];
  }

  getEnabledModules(): string[] {
    return this._company()?.enabledModules ?? [];
  }

  hasPermission(permission: string): boolean {
    const permissions = this.getPermissions();
    return permissions.includes(permission) || permissions.includes('*');
  }

  hasRole(role: string): boolean {
    return this.getRoles().includes(role);
  }

  hasAnyRole(roles: string[]): boolean {
    const userRoles = this.getRoles();
    return roles.some((r) => userRoles.includes(r));
  }

  isModuleEnabled(moduleKey: string): boolean {
    return this.getEnabledModules().includes(moduleKey);
  }

  getMenuItems(): MenuItem[] {
    return this._menus();
  }

  getMenuItemsForModule(moduleKey: string): MenuItem[] {
    return this._menus().filter((menu) => {
      if (menu.permission && !this.hasPermission(menu.permission)) return false;
      if (menu.children) {
        return menu.children.some((child) => !child.permission || this.hasPermission(child.permission));
      }
      return true;
    });
  }
}