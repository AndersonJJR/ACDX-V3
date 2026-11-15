import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PageHeaderComponent, PageHeaderAction } from '@shared/components/page-header/page-header.component';
import { CardComponent } from '@shared/components/ui/card/card.component';
import { BtnComponent } from '@shared/components/ui/btn/btn.component';
import { DataTableComponent, Column } from '@shared/components/data-table/data-table.component';
import { ToastService } from '@shared/components/toast/toast.service';

@Component({
  selector: 'app-subcontracting',
  standalone: true,
  imports: [CommonModule, PageHeaderComponent, CardComponent, BtnComponent, DataTableComponent],
  templateUrl: './subcontracting.component.html',
  styleUrl: './subcontracting.component.scss',
})
export class SubcontractingComponent {
  private toastService = inject(ToastService);

  readonly headerActions: PageHeaderAction[] = [
    { label: 'Novo', icon: 'M12 4v16m8-8H4', variant: 'primary', click: () => this.onNew() },
    { label: 'Importar', icon: 'M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12', variant: 'secondary', click: () => this.onImport() },
  ];

  columns: Column[] = [
    { key: 'id', label: 'ID', type: 'text', sortable: true },
    { key: 'name', label: 'Nome', type: 'text', sortable: true, filterable: true },
    { key: 'status', label: 'Status', type: 'badge', badgeConfig: { success: ['ativo'], warning: ['pendente'], danger: ['inativo', 'cancelado'] } },
    { key: 'createdAt', label: 'Criado em', type: 'datetime', sortable: true },
  ];

  data = [
    { id: '1', name: 'Exemplo 1', status: 'ativo', createdAt: new Date() },
    { id: '2', name: 'Exemplo 2', status: 'pendente', createdAt: new Date() },
    { id: '3', name: 'Exemplo 3', status: 'inativo', createdAt: new Date() },
  ];

  totalItems = 3;
  pageIndex = 0;
  pageSize = 10;
  pageSizeOptions = [10, 20, 50];

  onPageChange(event: any) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
  }

  onRowClick(row: any) {
    this.toastService.info('Em desenvolvimento', { message: `Visualizando ${row.name}` });
  }

  onNew() {
    this.toastService.info('Em desenvolvimento', { message: 'Novo' });
  }

  onImport() {
    this.toastService.info('Em desenvolvimento', { message: 'Importar' });
  }
}
