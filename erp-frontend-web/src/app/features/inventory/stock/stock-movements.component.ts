import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-stock-movements',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Movimentações de Estoque" description="Registro e histórico de movimentações" icon="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />`,
})
export class StockMovementsComponent {}
