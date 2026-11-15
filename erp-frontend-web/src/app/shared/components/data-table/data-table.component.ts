import {
  Component,
  input,
  output,
  signal,
  computed,
  effect,
  OnInit,
  OnDestroy,
  inject,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { formatNumberBR } from '../../../formatters/brazilian-formatters';

export interface Column<T = any> {
  key: string;
  label: string;
  type?: 'text' | 'number' | 'currency' | 'date' | 'datetime' | 'boolean' | 'badge' | 'actions' | 'custom';
  width?: string;
  minWidth?: string;
  maxWidth?: string;
  sortable?: boolean;
  filterable?: boolean;
  align?: 'left' | 'center' | 'right';
  formatter?: (value: any, row: T) => string;
  badgeConfig?: BadgeConfig;
  sticky?: boolean;
}

export interface BadgeConfig {
  success?: string | string[];
  warning?: string | string[];
  danger?: string | string[];
  info?: string | string[];
  default?: string;
}

export interface ActionButton<T = any> {
  label: string;
  icon?: string;
  class?: string;
  disabled?: (row: T) => boolean;
  hidden?: (row: T) => boolean;
  click: (row: T, event: Event) => void;
}

export interface SortState {
  column: string;
  direction: 'asc' | 'desc';
}

export interface FilterState {
  column: string;
  value: string;
}

export interface PageEvent {
  pageIndex: number;
  pageSize: number;
  length: number;
}

export interface DataTableConfig<T = any> {
  columns: Column<T>[];
  data: T[];
  totalItems: number;
  pageIndex: number;
  pageSize: number;
  pageSizeOptions: number[];
  sorting?: SortState;
  filters?: FilterState[];
  selection?: SelectionConfig<T>;
  loading?: boolean;
  emptyMessage?: string;
  trackBy?: (index: number, item: T) => any;
  rowClass?: (row: T) => string;
  rowStyle?: (row: T) => Record<string, string>;
}

export interface SelectionConfig<T = any> {
  enabled: boolean;
  multiple?: boolean;
  selectedRows: T[];
  onSelectionChange: (rows: T[]) => void;
  getRowId: (row: T) => string;
}

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './data-table.component.html',
  styleUrl: './data-table.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DataTableComponent<T = any> implements OnInit, OnDestroy {
  config = input.required<DataTableConfig<T>>();

  pageChange = output<PageEvent>();
  sortChange = output<SortState>();
  filterChange = output<FilterState[]>();
  rowClick = output<{ row: T; event: Event }>();
  actionClick = output<{ action: string; row: T; event: Event }>();

  private resizeObserver?: ResizeObserver;

  // Internal state
  private _sortState = signal<SortState | null>(null);
  private _filters = signal<FilterState[]>([]);
  private _columnWidths = signal<Record<string, number>>({});
  private _dragState = signal<{ column: string; startX: number; startWidth: number } | null>(null);

  readonly sortState = computed(() => this._sortState() ?? this.config().sorting ?? null);
  readonly filters = computed(() => this._filters().length > 0 ? this._filters() : this.config().filters ?? []);
  readonly columnWidths = computed(() => this._columnWidths());

  readonly displayedColumns = computed(() => this.config().columns.map((c) => c.key));
  readonly hasSelection = computed(() => this.config().selection?.enabled ?? false);

  // Track by function
  readonly trackByFn = computed(() => this.config().trackBy ?? ((index: number, item: T) => index));

  ngOnInit(): void {
    this.initColumnWidths();
    this.setupResizeObserver();
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
  }

  private initColumnWidths(): void {
    const columns = this.config().columns;
    const widths: Record<string, number> = {};
    columns.forEach((col) => {
      if (col.width) {
        widths[col.key] = parseInt(col.width, 10);
      }
    });
    this._columnWidths.set(widths);
  }

  private setupResizeObserver(): void {
    if (typeof ResizeObserver !== 'undefined') {
      this.resizeObserver = new ResizeObserver(() => {
        // Trigger change detection for sticky columns
      });
    }
  }

  getColumnStyle(column: Column<T>): Record<string, string> {
    const styles: Record<string, string> = {};
    const width = this._columnWidths()[column.key];

    if (width) {
      styles['width'] = `${width}px`;
      styles['min-width'] = `${width}px`;
      styles['max-width'] = `${width}px`;
    } else if (column.width) {
      styles['width'] = column.width;
    }
    if (column.minWidth) styles['min-width'] = column.minWidth;
    if (column.maxWidth) styles['max-width'] = column.maxWidth;
    if (column.sticky) {
      styles['position'] = 'sticky';
      styles['z-index'] = '1';
    }
    return styles;
  }

  getHeaderStyle(column: Column<T>): Record<string, string> {
    const styles = this.getColumnStyle(column);
    styles['text-align'] = column.align || 'left';
    return styles;
  }

  getCellStyle(column: Column<T>): Record<string, string> {
    const styles: Record<string, string> = {};
    styles['text-align'] = column.align || 'left';
    if (column.sticky) {
      styles['position'] = 'sticky';
      styles['background'] = 'white';
      styles['z-index'] = '1';
    }
    return styles;
  }

  getRowClasses(row: T): string {
    const rowClass = this.config().rowClass?.(row) || '';
    const selection = this.config().selection;
    const isSelected = selection?.enabled && selection.selectedRows.some((r) => selection.getRowId(r) === selection.getRowId(row));
    return `${rowClass} ${isSelected ? 'selected' : ''}`.trim();
  }

  getRowStyles(row: T): Record<string, string> {
    return this.config().rowStyle?.(row) || {};
  }

  formatValue(value: any, column: Column<T>, row: T): string {
    if (value === null || value === undefined || value === '') {
      return '—';
    }

    if (column.formatter) {
      return column.formatter(value, row);
    }

    switch (column.type) {
      case 'number':
        return formatNumberBR(value, 2);
      case 'currency':
        return formatNumberBR(value, 2);
      case 'date':
        return value instanceof Date ? value.toLocaleDateString('pt-BR') : String(value);
      case 'datetime':
        return value instanceof Date ? value.toLocaleString('pt-BR') : String(value);
      case 'boolean':
        return value ? 'Sim' : 'Não';
      default:
        return String(value);
    }
  }

  getBadgeClass(value: string, column: Column<T>): string {
    const config = column.badgeConfig;
    if (!config) return 'badge-neutral';

    const successValues = Array.isArray(config.success) ? config.success : config.success ? [config.success] : [];
    const warningValues = Array.isArray(config.warning) ? config.warning : config.warning ? [config.warning] : [];
    const dangerValues = Array.isArray(config.danger) ? config.danger : config.danger ? [config.danger] : [];
    const infoValues = Array.isArray(config.info) ? config.info : config.info ? [config.info] : [];

    if (successValues.includes(value)) return 'badge-success';
    if (warningValues.includes(value)) return 'badge-warning';
    if (dangerValues.includes(value)) return 'badge-danger';
    if (infoValues.includes(value)) return 'badge-primary';
    return 'badge-neutral';
  }

  onSort(column: Column<T>): void {
    if (!column.sortable) return;

    const currentSort = this.sortState();
    let direction: 'asc' | 'desc' = 'asc';

    if (currentSort && currentSort.column === column.key) {
      direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
    }

    this._sortState.set({ column: column.key, direction });
    this.sortChange.emit({ column: column.key, direction });
  }

  getSortIcon(column: Column<T>): string {
    const sort = this.sortState();
    if (!sort || sort.column !== column.key) {
      return 'M7 16l4-4m0 0l4 4m-4-4v12';
    }
    return sort.direction === 'asc'
      ? 'M5 15l7-7 7 7'
      : 'M19 9l-7 7-7-7';
  }

  onFilterChange(column: Column<T>, value: string): void {
    const filters = this._filters().filter((f) => f.column !== column.key);
    if (value.trim()) {
      filters.push({ column: column.key, value: value.trim() });
    }
    this._filters.set(filters);
    this.filterChange.emit(filters);
  }

  clearFilters(): void {
    this._filters.set([]);
    this.filterChange.emit([]);
  }

  hasActiveFilters(): boolean {
    return this.filters().length > 0;
  }

  onPageChange(event: PageEvent): void {
    this.pageChange.emit(event);
  }

  onPageSizeChange(pageSize: number): void {
    this.pageChange.emit({
      pageIndex: 0,
      pageSize,
      length: this.config().totalItems,
    });
  }

  onRowClick(row: T, event: Event): void {
    this.rowClick.emit({ row, event });
  }

  onActionClick(action: string, row: T, event: Event): void {
    event.stopPropagation();
    this.actionClick.emit({ action, row, event });
  }

  isRowSelected(row: T): boolean {
    const selection = this.config().selection;
    if (!selection?.enabled) return false;
    return selection.selectedRows.some((r) => selection.getRowId(r) === selection.getRowId(row));
  }

  onRowSelectionChange(row: T, event: Event): void {
    const selection = this.config().selection;
    if (!selection?.enabled) return;

    const isSelected = this.isRowSelected(row);
    let newSelection: T[];

    if (selection.multiple) {
      newSelection = isSelected
        ? selection.selectedRows.filter((r) => selection.getRowId(r) !== selection.getRowId(row))
        : [...selection.selectedRows, row];
    } else {
      newSelection = isSelected ? [] : [row];
    }

    selection.onSelectionChange(newSelection);
    event.stopPropagation();
  }

  onSelectAll(event: Event): void {
    const selection = this.config().selection;
    if (!selection?.enabled || !selection.multiple) return;

    const allSelected = this.config().data.every((row) => this.isRowSelected(row));
    const newSelection = allSelected ? [] : [...this.config().data];
    selection.onSelectionChange(newSelection);
    event.stopPropagation();
  }

  areAllRowsSelected(): boolean {
    const selection = this.config().selection;
    if (!selection?.enabled || !selection.multiple || this.config().data.length === 0) return false;
    return this.config().data.every((row) => this.isRowSelected(row));
  }

  areSomeRowsSelected(): boolean {
    const selection = this.config().selection;
    if (!selection?.enabled || !selection.multiple) return false;
    return this.config().data.some((row) => this.isRowSelected(row)) && !this.areAllRowsSelected();
  }

  onColumnResizeStart(column: Column<T>, event: MouseEvent): void {
    event.preventDefault();
    event.stopPropagation();
    const th = (event.target as HTMLElement).closest('th');
    if (!th) return;

    this._dragState.set({
      column: column.key,
      startX: event.clientX,
      startWidth: th.offsetWidth,
    });

    document.addEventListener('mousemove', this.onColumnResizeMove);
    document.addEventListener('mouseup', this.onColumnResizeEnd);
  }

  onColumnResizeMove = (event: MouseEvent): void => {
    const dragState = this._dragState();
    if (!dragState) return;

    const deltaX = event.clientX - dragState.startX;
    const newWidth = Math.max(80, dragState.startWidth + deltaX);

    this._columnWidths.update((widths) => ({
      ...widths,
      [dragState.column]: newWidth,
    }));
  };

  onColumnResizeEnd = (): void => {
    this._dragState.set(null);
    document.removeEventListener('mousemove', this.onColumnResizeMove);
    document.removeEventListener('mouseup', this.onColumnResizeEnd);
  };

  getPageInfo(): string {
    const { pageIndex, pageSize, totalItems } = this.config();
    const start = pageIndex * pageSize + 1;
    const end = Math.min((pageIndex + 1) * pageSize, totalItems);
    return `${start}–${end} de ${formatNumberBR(totalItems, 0)}`;
  }
}