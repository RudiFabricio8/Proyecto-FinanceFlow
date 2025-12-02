import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatCard } from '../../../../core/models/stat-card.model';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stat-card.html',
  styleUrl: './stat-card.scss'
})
export class StatCardComponent {
  @Input() card!: StatCard;
}