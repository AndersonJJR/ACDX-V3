import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-production-orders-list',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Ordens de Produção" description="Gerenciamento de ordens de produção" icon="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />`,
})
export class ProductionOrdersListComponent {}
