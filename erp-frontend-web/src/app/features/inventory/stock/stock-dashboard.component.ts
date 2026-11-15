import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-stock-dashboard',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Estoque" description="Dashboard de controle de estoque" icon="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />`,
})
export class StockDashboardComponent {}
