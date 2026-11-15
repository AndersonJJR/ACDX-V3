import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-payroll-form',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Processamento de Folha" description="Processamento e geração de folha de pagamento" icon="M12 6v6m0 0v6m0-6h6m-6 0H6" />`,
})
export class PayrollFormComponent {}
