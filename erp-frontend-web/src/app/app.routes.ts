import { Routes } from '@angular/router';
import { authGuard, moduleGuard, roleGuard } from './core/guards';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },

  // Auth
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },

  // Dashboard
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
  },

  // Finance Module
  {
    path: 'finance',
    canActivate: [authGuard, moduleGuard('finance')],
    children: [
      {
        path: 'chart-of-accounts',
        loadComponent: () => import('./features/finance/chart-of-accounts/chart-of-accounts.component').then((m) => m.ChartOfAccountsComponent),
        data: { breadcrumb: 'Plano de Contas' },
      },
      {
        path: 'journal-entries',
        loadComponent: () => import('./features/finance/journal-entries/journal-entries.component').then((m) => m.JournalEntriesComponent),
        data: { breadcrumb: 'Lançamentos Contábeis' },
      },
      {
        path: 'fiscal-periods',
        loadComponent: () => import('./features/finance/fiscal-periods/fiscal-periods.component').then((m) => m.FiscalPeriodsComponent),
        data: { breadcrumb: 'Períodos Fiscais' },
      },
      {
        path: 'bank-reconciliation',
        loadComponent: () => import('./features/finance/bank-reconciliation/bank-reconciliation.component').then((m) => m.BankReconciliationComponent),
        data: { breadcrumb: 'Conciliação Bancária' },
      },
      {
        path: 'assets',
        loadComponent: () => import('./features/finance/assets/assets.component').then((m) => m.AssetsComponent),
        data: { breadcrumb: 'Ativos Fixos' },
      },
      {
        path: 'reports',
        children: [
          {
            path: 'trial-balance',
            loadComponent: () => import('./features/finance/reports/trial-balance/trial-balance.component').then((m) => m.TrialBalanceComponent),
            data: { breadcrumb: 'Balancete' },
          },
          {
            path: 'balance-sheet',
            loadComponent: () => import('./features/finance/reports/balance-sheet/balance-sheet.component').then((m) => m.BalanceSheetComponent),
            data: { breadcrumb: 'Balanço Patrimonial' },
          },
          {
            path: 'pl',
            loadComponent: () => import('./features/finance/reports/pl/pl.component').then((m) => m.PLComponent),
            data: { breadcrumb: 'DRE' },
          },
          {
            path: 'cash-flow',
            loadComponent: () => import('./features/finance/reports/cash-flow/cash-flow.component').then((m) => m.CashFlowComponent),
            data: { breadcrumb: 'Fluxo de Caixa' },
          },
        ],
      },
    ],
  },

  // Inventory Module
  {
    path: 'inventory',
    canActivate: [authGuard, moduleGuard('inventory')],
    children: [
      {
        path: 'items',
        loadComponent: () => import('./features/inventory/items/items.component').then((m) => m.ItemsComponent),
        data: { breadcrumb: 'Itens' },
      },
      {
        path: 'warehouses',
        loadComponent: () => import('./features/inventory/warehouses/warehouses.component').then((m) => m.WarehousesComponent),
        data: { breadcrumb: 'Armazéns' },
      },
      {
        path: 'stock-balance',
        loadComponent: () => import('./features/inventory/stock-balance/stock-balance.component').then((m) => m.StockBalanceComponent),
        data: { breadcrumb: 'Saldo de Estoque' },
      },
      {
        path: 'valuation',
        loadComponent: () => import('./features/inventory/valuation/valuation.component').then((m) => m.ValuationComponent),
        data: { breadcrumb: 'Valoração' },
      },
      {
        path: 'purchasing',
        children: [
          {
            path: 'orders',
            loadComponent: () => import('./features/inventory/purchasing/orders/purchase-orders.component').then((m) => m.PurchaseOrdersComponent),
            data: { breadcrumb: 'Ordens de Compra' },
          },
          {
            path: 'receipts',
            loadComponent: () => import('./features/inventory/purchasing/receipts/receipts.component').then((m) => m.ReceiptsComponent),
            data: { breadcrumb: 'Recebimentos' },
          },
          {
            path: 'invoices',
            loadComponent: () => import('./features/inventory/purchasing/invoices/purchase-invoices.component').then((m) => m.PurchaseInvoicesComponent),
            data: { breadcrumb: 'Notas Fiscais de Entrada' },
          },
          {
            path: 'three-way-match',
            loadComponent: () => import('./features/inventory/purchasing/three-way-match/three-way-match.component').then((m) => m.ThreeWayMatchComponent),
            data: { breadcrumb: 'Conferência 3 Vias' },
          },
        ],
      },
      {
        path: 'reposting',
        loadComponent: () => import('./features/inventory/reposting/reposting.component').then((m) => m.RepostingComponent),
        data: { breadcrumb: 'Repostagem' },
      },
    ],
  },

  // Sales Module
  {
    path: 'sales',
    canActivate: [authGuard, moduleGuard('sales')],
    children: [
      {
        path: 'crm',
        loadComponent: () => import('./features/sales/crm/crm.component').then((m) => m.CRMComponent),
        data: { breadcrumb: 'CRM' },
      },
      {
        path: 'quotations',
        loadComponent: () => import('./features/sales/quotations/quotations.component').then((m) => m.QuotationsComponent),
        data: { breadcrumb: 'Cotações' },
      },
      {
        path: 'orders',
        loadComponent: () => import('./features/sales/orders/sales-orders.component').then((m) => m.SalesOrdersComponent),
        data: { breadcrumb: 'Pedidos de Venda' },
      },
      {
        path: 'deliveries',
        loadComponent: () => import('./features/sales/deliveries/deliveries.component').then((m) => m.DeliveriesComponent),
        data: { breadcrumb: 'Entregas' },
      },
      {
        path: 'invoices',
        loadComponent: () => import('./features/sales/invoices/sales-invoices.component').then((m) => m.SalesInvoicesComponent),
        data: { breadcrumb: 'Notas Fiscais de Saída' },
      },
      {
        path: 'commissions',
        loadComponent: () => import('./features/sales/commissions/commissions.component').then((m) => m.CommissionsComponent),
        data: { breadcrumb: 'Comissões' },
      },
    ],
  },

  // Manufacturing Module
  {
    path: 'manufacturing',
    canActivate: [authGuard, moduleGuard('manufacturing')],
    children: [
      {
        path: 'bom',
        loadComponent: () => import('./features/manufacturing/bom/bom.component').then((m) => m.BOMComponent),
        data: { breadcrumb: 'Estrutura de Produto (BOM)' },
      },
      {
        path: 'production-orders',
        loadComponent: () => import('./features/manufacturing/production-orders/production-orders.component').then((m) => m.ProductionOrdersComponent),
        data: { breadcrumb: 'Ordens de Produção' },
      },
      {
        path: 'work-orders',
        loadComponent: () => import('./features/manufacturing/work-orders/work-orders.component').then((m) => m.WorkOrdersComponent),
        data: { breadcrumb: 'Ordens de Serviço' },
      },
      {
        path: 'subcontracting',
        loadComponent: () => import('./features/manufacturing/subcontracting/subcontracting.component').then((m) => m.SubcontractingComponent),
        data: { breadcrumb: 'Terceirização' },
      },
      {
        path: 'projects',
        loadComponent: () => import('./features/manufacturing/projects/projects.component').then((m) => m.ProjectsComponent),
        data: { breadcrumb: 'Projetos' },
      },
      {
        path: 'quality',
        loadComponent: () => import('./features/manufacturing/quality/quality.component').then((m) => m.QualityComponent),
        data: { breadcrumb: 'Qualidade' },
      },
    ],
  },

  // HR Module
  {
    path: 'hr',
    canActivate: [authGuard, moduleGuard('hr')],
    children: [
      {
        path: 'employees',
        loadComponent: () => import('./features/hr/employees/employees.component').then((m) => m.EmployeesComponent),
        data: { breadcrumb: 'Colaboradores' },
      },
      {
        path: 'time-tracking',
        loadComponent: () => import('./features/hr/time-tracking/time-tracking.component').then((m) => m.TimeTrackingComponent),
        data: { breadcrumb: 'Ponto Eletrônico' },
      },
      {
        path: 'leave',
        loadComponent: () => import('./features/hr/leave/leave.component').then((m) => m.LeaveComponent),
        data: { breadcrumb: 'Férias e Licenças' },
      },
      {
        path: 'payroll',
        loadComponent: () => import('./features/hr/payroll/payroll.component').then((m) => m.PayrollComponent),
        data: { breadcrumb: 'Folha de Pagamento' },
      },
      {
        path: 'payslips',
        loadComponent: () => import('./features/hr/payslips/payslips.component').then((m) => m.PayslipsComponent),
        data: { breadcrumb: 'Contracheques' },
      },
      {
        path: 'audit-logs',
        loadComponent: () => import('./features/hr/audit-logs/hraudit-logs.component').then((m) => m.HRAuditLogsComponent),
        data: { breadcrumb: 'Auditoria RH' },
      },
    ],
  },

  // Admin Module
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard('ADMIN')],
    children: [
      {
        path: 'companies',
        loadComponent: () => import('./features/admin/companies/companies.component').then((m) => m.CompaniesComponent),
        data: { breadcrumb: 'Empresas' },
      },
      {
        path: 'users',
        loadComponent: () => import('./features/admin/users/users.component').then((m) => m.UsersComponent),
        data: { breadcrumb: 'Usuários' },
      },
      {
        path: 'roles-permissions',
        loadComponent: () => import('./features/admin/roles-permissions/roles-permissions.component').then((m) => m.RolesPermissionsComponent),
        data: { breadcrumb: 'Perfis e Permissões' },
      },
      {
        path: 'numbering-series',
        loadComponent: () => import('./features/admin/numbering-series/numbering-series.component').then((m) => m.NumberingSeriesComponent),
        data: { breadcrumb: 'Séries Numéricas' },
      },
      {
        path: 'fiscal-config',
        loadComponent: () => import('./features/admin/fiscal-config/fiscal-config.component').then((m) => m.FiscalConfigComponent),
        data: { breadcrumb: 'Configuração Fiscal' },
      },
      {
        path: 'audit-logs',
        loadComponent: () => import('./features/admin/audit-logs/admin-audit-logs.component').then((m) => m.AdminAuditLogsComponent),
        data: { breadcrumb: 'Logs de Auditoria' },
      },
    ],
  },

  // Portal
  {
    path: 'portal',
    canActivate: [authGuard],
    children: [
      {
        path: 'customer',
        loadComponent: () => import('./features/portal/customer/customer-portal.component').then((m) => m.CustomerPortalComponent),
        data: { breadcrumb: 'Portal do Cliente' },
      },
      {
        path: 'supplier',
        loadComponent: () => import('./features/portal/supplier/supplier-portal.component').then((m) => m.SupplierPortalComponent),
        data: { breadcrumb: 'Portal do Fornecedor' },
      },
    ],
  },

  // Fallback
  { path: '**', redirectTo: '/dashboard' },
];