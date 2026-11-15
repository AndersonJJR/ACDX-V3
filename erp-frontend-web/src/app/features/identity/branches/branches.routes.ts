import { Routes } from '@angular/router';

export const BRANCHES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./branches-list.component').then(m => m.BranchesListComponent),
  },
  {
    path: 'new',
    loadComponent: () => import('./branch-form.component').then(m => m.BranchFormComponent),
  },
  {
    path: ':id',
    loadComponent: () => import('./branch-detail.component').then(m => m.BranchDetailComponent),
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./branch-form.component').then(m => m.BranchFormComponent),
  },
];
