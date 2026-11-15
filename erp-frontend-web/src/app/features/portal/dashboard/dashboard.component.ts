import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { CardComponent } from '../../../shared/components/ui/card/card.component';

@Component({
  selector: 'app-portal-dashboard',
  standalone: true,
  imports: [CommonModule, CardComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class PortalDashboardComponent {
  private authService = inject(AuthService);

  readonly user = computed(() => this.authService.user());
  readonly company = computed(() => this.authService.company());
}