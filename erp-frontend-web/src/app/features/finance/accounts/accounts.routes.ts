import { Routes } from '@angular/router';

export const ACCOUNTS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./accounts-list.component').then(m => m.AccountsListComponent) },
  { path: 'new', loadComponent: () => import('./account-form.component').then(m => m.AccountFormComponent) },
  { path: ':id', loadComponent: () => import('./account-detail.component').then(m => m.AccountDetailComponent) },
  { path: ':id/edit', loadComponent: () => import('./account-form.component').then(m => m.AccountFormComponent) },
];
