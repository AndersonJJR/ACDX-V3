import { Routes } from '@angular/router';

export const LEADS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./leads-list.component').then(m => m.LeadsListComponent) },
  { path: 'new', loadComponent: () => import('./lead-form.component').then(m => m.LeadFormComponent) },
  { path: ':id', loadComponent: () => import('./lead-detail.component').then(m => m.LeadDetailComponent) },
];
