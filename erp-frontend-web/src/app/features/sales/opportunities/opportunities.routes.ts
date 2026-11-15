import { Routes } from '@angular/router';

export const OPPORTUNITIES_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./opportunities-list.component').then(m => m.OpportunitiesListComponent) },
  { path: 'new', loadComponent: () => import('./opportunity-form.component').then(m => m.OpportunityFormComponent) },
  { path: ':id', loadComponent: () => import('./opportunity-detail.component').then(m => m.OpportunityDetailComponent) },
];
