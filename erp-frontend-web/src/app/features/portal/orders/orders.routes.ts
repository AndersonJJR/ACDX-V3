import { Routes } from '@angular/router';

export const PORTAL_ORDERS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./orders-list.component').then(m => m.PortalOrdersListComponent) },
  { path: ':id', loadComponent: () => import('./order-detail.component').then(m => m.PortalOrderDetailComponent) },
];
