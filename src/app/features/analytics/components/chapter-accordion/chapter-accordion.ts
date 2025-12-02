// src/app/features/analytics/components/chapter-accordion/chapter-accordion.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalyticsChapter } from '../../../../core/models/analytics.model';

@Component({
  selector: 'app-chapter-accordion',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './chapter-accordion.html',
  styleUrl: './chapter-accordion.scss'
})
export class ChapterAccordion {
  @Input() chapter!: AnalyticsChapter;
  @Output() toggle = new EventEmitter<string>();

  onToggle(): void {
    this.toggle.emit(this.chapter.id);
  }
}