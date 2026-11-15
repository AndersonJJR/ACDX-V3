import { Component } from '@angular.core';
import { PlaceholderComponent } from '@shared/components/placeholder/placeholder.component';

@Component({
  selector: 'app-projects-list',
  standalone: true,
  imports: [PlaceholderComponent],
  template: `<app-placeholder title="Projetos" description="Gerenciamento de projetos" icon="M9 5H7a2 2 0 012-2h10a2 2 0 012 2v12a2 2 0 01-2 2H7a2 2 0 01-2-2V7z" />`,
})
export class ProjectsListComponent {}
