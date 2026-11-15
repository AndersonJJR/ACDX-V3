import { Routes } from '@angular/router';

export const DEPARTMENTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./departments-list.component').then(m => m.DepartmentsListComponent),
  },
  {
    path: 'new',
    loadComponent: () => import('./department-form.component').then(m => m.DepartmentFormComponent),
  },
  {
    path: ':id',
    loadComponent: () => import('./department-detail.component').then(m => m.DepartmentDetailComponent),
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./department-form.component').then(m => m.DepartmentFormComponent),
  },
];
