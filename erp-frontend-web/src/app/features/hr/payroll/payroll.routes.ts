import { Routes } from '@angular/router';

export const PAYROLL_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./payroll-list.component').then(m => m.PayrollListComponent) },
  { path: 'new', loadComponent: () => import('./payroll-form.component').then(m => m.PayrollFormComponent) },
  { path: ':id', loadComponent: () => import('./payroll-detail.component').then(m => m.PayrollDetailComponent) },
];
