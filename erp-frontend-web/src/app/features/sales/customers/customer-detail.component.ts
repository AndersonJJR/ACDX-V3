import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Detalhes do Cliente" description="Visualização detalhada do cliente" icon="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />`,
})
export class CustomerDetailComponent {}
