import { Component, OnInit, AfterViewInit, ElementRef, ViewChild, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-analytics-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="chart-wrapper">
      <canvas #chartCanvas></canvas>
    </div>
  `,
  styles: [`
    .chart-wrapper {
      background: var(--bg-secondary, #1a1a2e);
      border-radius: 12px;
      padding: 1.5rem;
      margin-top: 1rem;
      min-height: 250px;
    }
    canvas {
      max-height: 250px;
    }
  `]
})
export class AnalyticsChartComponent implements AfterViewInit, OnChanges {
  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;
  @Input() chartType: 'bar' | 'doughnut' | 'line' = 'bar';
  @Input() labels: string[] = [];
  @Input() data: number[] = [];
  @Input() title: string = '';
  @Input() colors: string[] = ['#6c5ce7', '#00cec9', '#fdcb6e', '#e17055'];

  private chart: Chart | null = null;

  ngAfterViewInit(): void {
    this.createChart();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.chart && (changes['data'] || changes['labels'])) {
      this.updateChart();
    }
  }

  private createChart(): void {
    if (!this.chartCanvas?.nativeElement) return;
    
    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    const config: any = {
      type: this.chartType,
      data: {
        labels: this.labels.length ? this.labels : ['Sin datos'],
        datasets: [{
          label: this.title,
          data: this.data.length ? this.data : [0],
          backgroundColor: this.colors,
          borderColor: this.chartType === 'line' ? this.colors[0] : this.colors,
          borderWidth: this.chartType === 'line' ? 3 : 1,
          tension: 0.4,
          fill: this.chartType === 'line'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: this.chartType === 'doughnut',
            position: 'bottom',
            labels: {
              color: '#a0a0a0',
              padding: 15
            }
          },
          title: {
            display: !!this.title,
            text: this.title,
            color: '#ffffff',
            font: { size: 14, weight: 'bold' }
          }
        },
        scales: this.chartType !== 'doughnut' ? {
          y: {
            beginAtZero: true,
            grid: { color: 'rgba(255,255,255,0.1)' },
            ticks: { color: '#a0a0a0' }
          },
          x: {
            grid: { display: false },
            ticks: { color: '#a0a0a0' }
          }
        } : undefined
      }
    };

    this.chart = new Chart(ctx, config);
  }

  private updateChart(): void {
    if (!this.chart) return;
    
    this.chart.data.labels = this.labels.length ? this.labels : ['Sin datos'];
    this.chart.data.datasets[0].data = this.data.length ? this.data : [0];
    this.chart.update();
  }
}
