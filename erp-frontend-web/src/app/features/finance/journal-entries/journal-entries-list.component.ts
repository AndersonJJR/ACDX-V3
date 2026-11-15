import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-journal-entries-list',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Lançamentos Contábeis" description="Gerenciamento de lançamentos contábeis" icon="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />`,
})
export class JournalEntriesListComponent {}
