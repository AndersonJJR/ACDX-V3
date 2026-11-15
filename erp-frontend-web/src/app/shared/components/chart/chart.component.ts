import {
  Component,
  input,
  output,
  ViewChild,
  ElementRef,
  OnInit,
  OnDestroy,
  OnChanges,
  SimpleChanges,
  signal,
  computed,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  Chart,
  ChartConfiguration,
  ChartData,
  ChartOptions,
  ChartType,
  registerables,
} from 'chart.js';

Chart.register(...registerables);

export type ChartTypeOption = 'bar' | 'line' | 'pie' | 'doughnut' | 'radar' | 'polarArea';

export interface ChartDataset {
  label: string;
  data: (number | null)[];
  backgroundColor?: string | string[];
  borderColor?: string | string[];
  borderWidth?: number;
  fill?: boolean;
  tension?: number;
  pointRadius?: number;
  pointHoverRadius?: number;
}

export interface ChartConfig {
  type: ChartTypeOption;
  data: {
    labels: string[];
    datasets: ChartDataset[];
  };
  options?: ChartOptions;
  plugins?: any[];
}

@Component({
  selector: 'app-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './chart.component.html',
  styleUrl: './chart.component.scss',
})
export class ChartComponent implements OnInit, OnDestroy, OnChanges {
  config = input.required<ChartConfig>();
  width = input<number>(400);
  height = input<number>(300);
  responsive = input(true);
  maintainAspectRatio = input(true);

  chartClick = output<{ event: Event; elements: any[] }>();
  chartHover = output<{ event: Event; elements: any[] }>();

  @ViewChild('canvas') canvasRef!: ElementRef<HTMLCanvasElement>;

  private chartInstance: Chart | null = null;
  private _isDestroyed = false;

  readonly canvasId = computed(() => `chart-${Math.random().toString(36).substr(2, 9)}`);

  ngOnInit(): void {
    this.createChart();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['config'] && this.chartInstance) {
      this.updateChart();
    }
  }

  ngOnDestroy(): void {
    this._isDestroyed = true;
    this.destroyChart();
  }

  private createChart(): void {
    if (this._isDestroyed || !this.canvasRef) return;

    const ctx = this.canvasRef.nativeElement.getContext('2d');
    if (!ctx) return;

    const chartConfig = this.config();
    const mergedOptions = this.getMergedOptions(chartConfig.options);

    this.chartInstance = new Chart(ctx, {
      type: chartConfig.type,
      data: chartConfig.data as ChartData,
      options: mergedOptions,
      plugins: chartConfig.plugins,
    });
  }

  private updateChart(): void {
    if (!this.chartInstance) return;

    const chartConfig = this.config();
    this.chartInstance.data = chartConfig.data as ChartData;
    this.chartInstance.config.type = chartConfig.type;
    this.chartInstance.options = this.getMergedOptions(chartConfig.options);
    this.chartInstance.update();
  }

  private destroyChart(): void {
    if (this.chartInstance) {
      this.chartInstance.destroy();
      this.chartInstance = null;
    }
  }

  private getMergedOptions(userOptions?: ChartOptions): ChartOptions {
    const defaultOptions: ChartOptions = {
      responsive: this.responsive(),
      maintainAspectRatio: this.maintainAspectRatio(),
      interaction: {
        mode: 'index',
        intersect: false,
      },
      plugins: {
        legend: {
          display: true,
          position: 'top',
          labels: {
            usePointStyle: true,
            padding: 20,
            font: {
              family: 'Inter, system-ui, sans-serif',
              size: 12,
            },
            color: '#525252',
          },
        },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          titleFont: {
            family: 'Inter, system-ui, sans-serif',
            size: 13,
            weight: '600',
          },
          bodyFont: {
            family: 'Inter, system-ui, sans-serif',
            size: 12,
          },
          padding: 12,
          cornerRadius: 8,
          displayColors: true,
          callbacks: {
            label: (context) => {
              let label = context.dataset.label || '';
              if (label) label += ': ';
              if (context.parsed.y !== null) {
                label += new Intl.NumberFormat('pt-BR').format(context.parsed.y);
              }
              return label;
            },
          },
        },
      },
      scales: {
        x: {
          grid: {
            display: false,
            drawBorder: false,
          },
          ticks: {
            font: {
              family: 'Inter, system-ui, sans-serif',
              size: 11,
            },
            color: '#737373',
          },
        },
        y: {
          beginAtZero: true,
          grid: {
            color: 'rgba(0, 0, 0, 0.05)',
            drawBorder: false,
          },
          ticks: {
            font: {
              family: 'Inter, system-ui, sans-serif',
              size: 11,
            },
            color: '#737373',
            callback: (value) => {
              return new Intl.NumberFormat('pt-BR').format(value as number);
            },
          },
        },
      },
    };

    // Merge user options with defaults
    return this.deepMerge(defaultOptions, userOptions || {});
  }

  private deepMerge(target: any, source: any): any {
    const result = { ...target };
    for (const key of Object.keys(source)) {
      if (source[key] && typeof source[key] === 'object' && !Array.isArray(source[key])) {
        result[key] = this.deepMerge(target[key] || {}, source[key]);
      } else {
        result[key] = source[key];
      }
    }
    return result;
  }

  onCanvasClick(event: MouseEvent): void {
    if (!this.chartInstance) return;
    const elements = this.chartInstance.getElementsAtEventForMode(event, 'nearest', { intersect: true }, false);
    this.chartClick.emit({ event, elements });
  }

  onCanvasHover(event: MouseEvent): void {
    if (!this.chartInstance) return;
    const elements = this.chartInstance.getElementsAtEventForMode(event, 'nearest', { intersect: true }, false);
    this.chartHover.emit({ event, elements });
  }

  // Public methods
  getChartInstance(): Chart | null {
    return this.chartInstance;
  }

  updateData(data: ChartData): void {
    if (this.chartInstance) {
      this.chartInstance.data = data;
      this.chartInstance.update();
    }
  }

  updateOptions(options: ChartOptions): void {
    if (this.chartInstance) {
      this.chartInstance.options = this.getMergedOptions(options);
      this.chartInstance.update();
    }
  }

  resize(): void {
    this.chartInstance?.resize();
  }

  toBase64Image(): string | null {
    return this.chartInstance?.toBase64Image() || null;
  }

  destroy(): void {
    this.destroyChart();
  }
}