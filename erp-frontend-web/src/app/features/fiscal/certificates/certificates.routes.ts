import { Routes } from '@angular/router';

export const CERTIFICATES_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./certificates-list.component').then(m => m.CertificatesListComponent) },
  { path: 'new', loadComponent: () => import('./certificate-form.component').then(m => m.CertificateFormComponent) },
];
