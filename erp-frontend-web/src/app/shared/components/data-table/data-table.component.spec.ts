import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DataTableComponent } from './data-table.component';
import { By } from '@angular/platform-browser';
import { Column, DataTableConfig } from './data-table.component';

interface TestRow {
  id: string;
  name: string;
  value: number;
  status: string;
  date: Date;
}

describe('DataTableComponent', () => {
  let component: DataTableComponent<TestRow>;
  let fixture: ComponentFixture<DataTableComponent<TestRow>>;

  const mockColumns: Column<TestRow>[] = [
    { key: 'id', label: 'ID', type: 'text', sortable: true, filterable: true },
    { key: 'name', label: 'Nome', type: 'text', sortable: true, filterable: true },
    { key: 'value', label: 'Valor', type: 'currency', sortable: true },
    { key: 'status', label: 'Status', type: 'badge', badgeConfig: { success: ['ativo'], danger: ['inativo'] } },
    { key: 'date', label: 'Data', type: 'date', sortable: true },
  ];

  const mockData: TestRow[] = [
    { id: '1', name: 'Item 1', value: 100.5, status: 'ativo', date: new Date('2024-01-15') },
    { id: '2', name: 'Item 2', value: 200.75, status: 'inativo', date: new Date('2024-02-20') },
    { id: '3', name: 'Item 3', value: 300.0, status: 'ativo', date: new Date('2024-03-10') },
  ];

  const mockConfig: DataTableConfig<TestRow> = {
    columns: mockColumns,
    data: mockData,
    totalItems: 3,
    pageIndex: 0,
    pageSize: 10,
    pageSizeOptions: [10, 20, 50],
    loading: false,
    selection: {
      enabled: true,
      multiple: true,
      selectedRows: [],
      onSelectionChange: jasmine.createSpy('onSelectionChange'),
      getRowId: (row) => row.id,
    },
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DataTableComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DataTableComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('config', mockConfig);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render table headers', () => {
    const headers = fixture.debugElement.queryAll(By.css('thead th'));
    expect(headers.length).toBe(mockColumns.length + 1); // +1 for selection checkbox
  });

  it('should render table rows', () => {
    const rows = fixture.debugElement.queryAll(By.css('tbody tr'));
    expect(rows.length).toBe(mockData.length);
  });

  it('should display data correctly', () => {
    const firstRowCells = fixture.debugElement.queryAll(By.css('tbody tr:first-child td'));
    // First cell is checkbox, then data cells
    expect(firstRowCells[1].nativeElement.textContent).toContain('1'); // ID
    expect(firstRowCells[2].nativeElement.textContent).toContain('Item 1'); // Name
  });

  it('should format currency values', () => {
    const valueCell = fixture.debugElement.query(By.css('tbody tr:first-child td:nth-child(4)'));
    expect(valueCell.nativeElement.textContent).toContain('100,50');
  });

  it('should render badge with correct class for active status', () => {
    const badge = fixture.debugElement.query(By.css('tbody tr:first-child .badge-success'));
    expect(badge).toBeTruthy();
    expect(badge.nativeElement.textContent).toContain('ativo');
  });

  it('should render badge with correct class for inactive status', () => {
    const badge = fixture.debugElement.query(By.css('tbody tr:nth-child(2) .badge-danger'));
    expect(badge).toBeTruthy();
    expect(badge.nativeElement.textContent).toContain('inativo');
  });

  it('should emit sortChange when column header clicked', () => {
    spyOn(component.sortChange, 'emit');
    const sortableHeader = fixture.debugElement.query(By.css('thead th button'));
    sortableHeader.nativeElement.click();
    fixture.detectChanges();

    expect(component.sortChange.emit).toHaveBeenCalledWith({
      column: 'id',
      direction: 'asc',
    });
  });

  it('should toggle sort direction on second click', () => {
    spyOn(component.sortChange, 'emit');
    const sortableHeader = fixture.debugElement.query(By.css('thead th button'));

    // First click - asc
    sortableHeader.nativeElement.click();
    fixture.detectChanges();

    // Second click - desc
    sortableHeader.nativeElement.click();
    fixture.detectChanges();

    expect(component.sortChange.emit).toHaveBeenCalledWith({
      column: 'id',
      direction: 'desc',
    });
  });

  it('should emit filterChange when filter input changes', () => {
    spyOn(component.filterChange, 'emit');
    const filterInput = fixture.debugElement.query(By.css('thead th input'));
    filterInput.nativeElement.value = 'test';
    filterInput.nativeElement.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(component.filterChange.emit).toHaveBeenCalledWith([
      { column: 'id', value: 'test' },
    ]);
  });

  it('should emit pageChange when page size changes', () => {
    spyOn(component.pageChange, 'emit');
    const pageSizeSelect = fixture.debugElement.query(By.css('select'));
    pageSizeSelect.nativeElement.value = '20';
    pageSizeSelect.nativeElement.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(component.pageChange.emit).toHaveBeenCalledWith({
      pageIndex: 0,
      pageSize: 20,
      length: 3,
    });
  });

  it('should show loading state', () => {
    const loadingConfig = { ...mockConfig, loading: true, data: [] };
    fixture.componentRef.setInput('config', loadingConfig);
    fixture.detectChanges();

    const loadingText = fixture.debugElement.query(By.css('tbody td'));
    expect(loadingText.nativeElement.textContent).toContain('Carregando...');
  });

  it('should show empty message when no data', () => {
    const emptyConfig = { ...mockConfig, data: [], totalItems: 0 };
    fixture.componentRef.setInput('config', emptyConfig);
    fixture.detectChanges();

    const emptyText = fixture.debugElement.query(By.css('tbody td'));
    expect(emptyText.nativeElement.textContent).toContain('Nenhum registro encontrado');
  });

  it('should select/deselect row on checkbox click', () => {
    const checkbox = fixture.debugElement.query(By.css('tbody tr:first-child input[type="checkbox"]'));
    checkbox.nativeElement.click();
    fixture.detectChanges();

    expect(mockConfig.selection?.onSelectionChange).toHaveBeenCalledWith([mockData[0]]);
  });

  it('should select all rows on header checkbox click', () => {
    const headerCheckbox = fixture.debugElement.query(By.css('thead th input[type="checkbox"]'));
    headerCheckbox.nativeElement.click();
    fixture.detectChanges();

    expect(mockConfig.selection?.onSelectionChange).toHaveBeenCalledWith(mockData);
  });

  it('should apply rowClass function', () => {
    const configWithRowClass = {
      ...mockConfig,
      rowClass: (row: TestRow) => row.status === 'ativo' ? 'active-row' : 'inactive-row',
    };
    fixture.componentRef.setInput('config', configWithRowClass);
    fixture.detectChanges();

    const firstRow = fixture.debugElement.query(By.css('tbody tr:first-child'));
    expect(firstRow.nativeElement.classList).toContain('active-row');
  });
});