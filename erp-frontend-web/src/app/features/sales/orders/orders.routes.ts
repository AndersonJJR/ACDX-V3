import { Routes } from '@angular/router';

export const ORDERS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./orders-list.component').then(m => m.OrdersListComponent) },
  { path: 'new', loadComponent: () => import('./order-form.component').then(m => m.OrderFormComponent) },
  { path: ':id', loadComponent: () => import('./order-detail.component').then(m => m.OrderDetailComponent) },
];
