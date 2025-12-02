// src/app/features/analytics/analytics.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalyticsChapter } from '../../core/models/analytics.model';
import { ChapterAccordion } from './components/chapter-accordion/chapter-accordion';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, ChapterAccordion],
  templateUrl: './analytics.html',
  styleUrl: './analytics.scss'
})
export class Analytics implements OnInit {
  chapters: AnalyticsChapter[] = [];

  ngOnInit(): void {
    this.chapters = [
      {
        id: '1',
        title: 'Capítulo 1: Entendiendo el Costo Total de Nómina',
        description: 'El costo total de nómina es un indicador fundamental para la salud financiera de la empresa. Aquí, desglosamos las cifras que componen este gasto vital.',
        isExpanded: false
      },
      {
        id: '2',
        title: 'Capítulo 2: El Balance entre Deducciones y Percepciones',
        description: 'Comprender la relación entre lo que los empleados ganan y lo que se les retiene es vital para asegurar la transparencia y el cumplimiento.',
        isExpanded: false
      },
      {
        id: '3',
        title: 'Capítulo 3: Eficiencia y Cumplimiento en la Gestión de Nómina',
        description: 'La eficiencia en el procesamiento de la nómina y el cumplimiento normativo son esenciales para evitar errores y sanciones.',
        isExpanded: false
      }
    ];
  }

  toggleChapter(id: string): void {
    this.chapters = this.chapters.map(ch => ({
      ...ch,
      isExpanded: ch.id === id ? !ch.isExpanded : ch.isExpanded
    }));
  }
}