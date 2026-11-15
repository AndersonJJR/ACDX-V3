import {
  Component,
  input,
  output,
  signal,
  computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { BtnComponent } from '../../ui/btn/btn.component';

export type ReportFormat = 'pdf' | 'excel' | 'csv' | 'html';

export interface ReportViewerConfig {
  title: string;
  subtitle?: string;
  showToolbar?: boolean;
  showExportButtons?: boolean;
  availableFormats?: ReportFormat[];
  filename?: string;
  loading?: boolean;
}

@Component({
  selector: 'app-report-viewer',
  standalone: true,
  imports: [CommonModule, BtnComponent],
  templateUrl: './report-viewer.component.html',
  styleUrl: './report-viewer.component.scss',
})
export class ReportViewerComponent {
  config = input.required<ReportViewerConfig>();
  reportData = input<any>(null);

  export = output<ReportFormat>();
  print = output<void>();
  refresh = output<void>();

  readonly showToolbar = computed(() => this.config().showToolbar !== false);
  readonly showExportButtons = computed(() => this.config().showExportButtons !== false);
  readonly availableFormats = computed(() => this.config().availableFormats || ['pdf', 'excel', 'csv']);
  readonly filename = computed(() => this.config().filename || `relatorio-${new Date().toISOString().split('T')[0]}`);
  readonly isLoading = computed(() => this.config().loading || false);

  getFormatLabel(format: ReportFormat): string {
    const labels: Record<ReportFormat, string> = {
      pdf: 'PDF',
      excel: 'Excel',
      csv: 'CSV',
      html: 'HTML',
    };
    return labels[format];
  }

  getFormatIcon(format: ReportFormat): string {
    const icons: Record<ReportFormat, string> = {
      pdf: 'M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z',
      excel: 'M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z',
      csv: 'M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z',
      html: 'M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z',
    };
    return icons[format];
  }

  onExport(format: ReportFormat): void {
    this.export.emit(format);
  }

  onPrint(): void {
    this.print.emit();
  }

  onRefresh(): void {
    this.refresh.emit();
  }
}