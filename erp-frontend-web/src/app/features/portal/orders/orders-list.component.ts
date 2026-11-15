import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-portal-orders-list',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Meus Pedidos" description="Visualização dos meus pedidos" icon="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />`,
})
export class PortalOrdersListComponent {}
