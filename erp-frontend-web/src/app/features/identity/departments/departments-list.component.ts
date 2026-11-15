import { Component } from '@angular/core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-departments-list',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Departamentos" description="Gerenciamento de departamentos" icon="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5m-9 0H3m2 0h5" />`,
})
export class DepartmentsListComponent {}
