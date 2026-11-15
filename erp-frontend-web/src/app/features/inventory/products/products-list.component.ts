import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-products-list',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Produtos" description="Gerenciamento de produtos e serviços" icon="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />`,
})
export class ProductsListComponent {}
