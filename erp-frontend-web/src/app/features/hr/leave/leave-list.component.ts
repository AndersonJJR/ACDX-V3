import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-leave-list',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Férias e Licenças" description="Gerenciamento de férias e licenças" icon="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />`,
})
export class LeaveListComponent {}
