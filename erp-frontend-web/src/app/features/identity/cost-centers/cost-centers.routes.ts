import { Routes } from '@angular/router';

export const COST_CENTERS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./cost-centers-list.component').then(m => m.CostCentersListComponent),
  },
  {
    path: 'new',
    loadComponent: () => import('./cost-center-form.component').then(m => m.CostCenterFormComponent),
  },
  {
    path: ':id',
    loadComponent: () => import('./cost-center-detail.component').then(m => m.CostCenterDetailComponent),
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./cost-center-form.component').then(m => m.CostCenterFormComponent),
  },
];
