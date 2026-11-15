import { Routes } from '@angular/router';

export const PORTAL_TICKETS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./tickets-list.component').then(m => m.PortalTicketsListComponent) },
  { path: 'new', loadComponent: () => import('./ticket-form.component').then(m => m.PortalTicketFormComponent) },
  { path: ':id', loadComponent: () => import('./ticket-detail.component').then(m => m.PortalTicketDetailComponent) },
];
