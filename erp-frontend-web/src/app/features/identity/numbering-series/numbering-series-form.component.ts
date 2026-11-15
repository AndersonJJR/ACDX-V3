import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-numbering-series-form',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Formulário de Série Numérica" description="Criação e edição de séries numéricas" icon="M12 6v6m0 0v6m0-6h6m-6 0H6" />`,
})
export class NumberingSeriesFormComponent {}
