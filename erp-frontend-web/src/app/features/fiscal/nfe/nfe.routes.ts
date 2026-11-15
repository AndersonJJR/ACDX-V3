import { Routes } from '@angular/router';

export const NFE_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./nfe-list.component').then(m => m.NfeListComponent) },
  { path: 'new', loadComponent: () => import('./nfe-form.component').then(m => m.NfeFormComponent) },
  { path: ':id', loadComponent: () => import('./nfe-detail.component').then(m => m.NfeDetailComponent) },
];
