import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-chart-placeholder',
  standalone: true,
  imports: [],
  templateUrl: './chart-placeholder.html',
  styleUrl: './chart-placeholder.scss'
})
export class ChartPlaceholderComponent {
  @Input() title: string = '';
}