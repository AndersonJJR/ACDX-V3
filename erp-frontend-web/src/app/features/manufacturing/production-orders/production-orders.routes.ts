import { Routes } from '@angular/router';

export const PRODUCTION_ORDERS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./production-orders-list.component').then(m => m.ProductionOrdersListComponent) },
  { path: 'new', loadComponent: () => import('./production-order-form.component').then(m => m.ProductionOrderFormComponent) },
  { path: ':id', loadComponent: () => import('./production-order-detail.component').then(m => m.ProductionOrderDetailComponent) },
];
