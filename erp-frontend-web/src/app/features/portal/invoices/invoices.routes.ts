import { Routes } from '@angular/router';

export const PORTAL_INVOICES_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./invoices-list.component').then(m => m.PortalInvoicesListComponent) },
  { path: ':id', loadComponent: () => import('./invoice-detail.component').then(m => m.PortalInvoiceDetailComponent) },
];
