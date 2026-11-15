import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-time-sheets',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Ponto Eletrônico" description="Gerenciamento de ponto eletrônico e folhas de ponto" icon="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />`,
})
export class TimeSheetsComponent {}
