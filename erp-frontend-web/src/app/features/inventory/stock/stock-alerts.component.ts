import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-stock-alerts',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Alertas de Estoque" description="Alertas de estoque mínimo e máximo" icon="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />`,
})
export class StockAlertsComponent {}
