import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-income-statement',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="DRE" description="Demonstração do Resultado do Exercício" icon="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />`,
})
export class IncomeStatementComponent {}
