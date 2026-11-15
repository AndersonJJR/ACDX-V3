import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-certificates-list',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Certificados Digitais" description="Gerenciamento de certificados digitais" icon="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />`,
})
export class CertificatesListComponent {}
