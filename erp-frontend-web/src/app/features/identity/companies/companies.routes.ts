import { Routes } from '@angular/router';

export const COMPANIES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./companies-list.component').then(m => m.CompaniesListComponent),
  },
  {
    path: 'new',
    loadComponent: () => import('./company-form.component').then(m => m.CompanyFormComponent),
  },
  {
    path: ':id',
    loadComponent: () => import('./company-detail.component').then(m => m.CompanyDetailComponent),
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./company-form.component').then(m => m.CompanyFormComponent),
  },
];
