import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-nfce-list',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="NFCe" description="Gerenciamento de Notas Fiscais de Consumidor Eletrônicas" icon="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />`,
})
export class NfceListComponent {}
