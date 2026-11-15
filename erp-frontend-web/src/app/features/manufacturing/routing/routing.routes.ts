import { Routes } from '@angular/router';

export const ROUTING_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./routing-list.component').then(m => m.RoutingListComponent) },
  { path: 'new', loadComponent: () => import('./routing-form.component').then(m => m.RoutingFormComponent) },
  { path: ':id', loadComponent: () => import('./routing-detail.component').then(m => m.RoutingDetailComponent) },
];
