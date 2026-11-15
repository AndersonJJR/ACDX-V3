import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-nfe-form',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Emissão de NFe" description="Emissão e gerenciamento de Notas Fiscais Eletrônicas" icon="M12 6v6m0 0v6m0-6h6m-6 0H6" />`,
})
export class NfeFormComponent {}
