import { Routes } from '@angular/router';

export const PROJECTS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./projects-list.component').then(m => m.ProjectsListComponent) },
  { path: 'new', loadComponent: () => import('./project-form.component').then(m => m.ProjectFormComponent) },
  { path: ':id', loadComponent: () => import('./project-detail.component').then(m => m.ProjectDetailComponent) },
];
