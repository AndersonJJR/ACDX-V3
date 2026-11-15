import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-nfe-list',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="NFe" description="Gerenciamento de Notas Fiscais Eletrônicas" icon="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />`,
})
export class NfeListComponent {}
