import { Routes } from '@angular/router';

export const WAREHOUSES_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./warehouses-list.component').then(m => m.WarehousesListComponent) },
  { path: 'new', loadComponent: () => import('./warehouse-form.component').then(m => m.WarehouseFormComponent) },
  { path: ':id', loadComponent: () => import('./warehouse-detail.component').then(m => m.WarehouseDetailComponent) },
];
