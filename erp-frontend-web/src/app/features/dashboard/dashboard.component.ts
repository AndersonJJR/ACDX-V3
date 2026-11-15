import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { BtnComponent } from '../../shared/components/ui/btn/btn.component';
import { formatBRL } from '../../shared/formatters/brazilian-formatters';
import { CardComponent } from '../../shared/components/ui/card/card.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, BtnComponent, CardComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  private authService = inject(AuthService);

  readonly user = computed(() => this.authService.user());
  readonly company = computed(() => this.authService.company());
  readonly enabledModules = computed(() => this.authService.getEnabledModules());
  readonly permissions = computed(() => this.authService.getPermissions());

  // Sample stats - would come from API
  readonly stats = [
    { label: 'Faturamento do Mês', value: 'R$ 125.430,00', change: '+12.5%', trend: 'up' as const },
    { label: 'Pedidos Pendentes', value: '23', change: '-3', trend: 'down' as const },
    { label: 'Clientes Ativos', value: '1.234', change: '+8', trend: 'up' as const },
    { label: 'Produtos em Estoque', value: '567', change: '0', trend: 'stable' as const },
  ];

  readonly recentActivities = [
    { type: 'sale', description: 'Pedido #12345 confirmado', time: 'Há 5 min', amount: 'R$ 2.450,00' },
    { type: 'payment', description: 'Recebimento Cliente ABC', time: 'Há 15 min', amount: 'R$ 15.000,00' },
    { type: 'user', description: 'Novo usuário cadastrado: João Silva', time: 'Há 1 hora' },
    { type: 'alert', description: 'Estoque baixo: Produto XYZ', time: 'Há 2 horas' },
  ];

  getTrendClass(trend: 'up' | 'down' | 'stable'): string {
    switch (trend) {
      case 'up': return 'text-success-600';
      case 'down': return 'text-danger-600';
      default: return 'text-neutral-500';
    }
  }

  getTrendIcon(trend: 'up' | 'down' | 'stable'): string {
    switch (trend) {
      case 'up': return 'M5 10l7-7m0 0l7 7m-7-7v18';
      case 'down': return 'M19 14l-7 7m0 0l-7-7m7 7V5';
      default: return 'M5 12h14';
    }
  }

  getActivityIcon(type: string): string {
    switch (type) {
      case 'sale': return 'M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z';
      case 'payment': return 'M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z';
      case 'user': return 'M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z';
      case 'alert': return 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z';
      default: return 'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z';
    }
  }
}