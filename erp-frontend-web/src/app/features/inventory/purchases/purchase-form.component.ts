import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-purchase-form',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Nova Ordem de Compra" description="Criação de ordens de compra" icon="M12 6v6m0 0v6m0-6h6m-6 0H6" />`,
})
export class PurchaseFormComponent {}
