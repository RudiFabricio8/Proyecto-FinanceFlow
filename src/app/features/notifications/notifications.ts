// src/app/features/notifications/notifications.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Alert } from '../../core/models/notification.model';
import { AlertCardComponent } from './components/alert-card/alert-card';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, AlertCardComponent],
  templateUrl: './notifications.html',
  styleUrl: './notifications.scss'
})
export class Notifications implements OnInit {
  alerts: Alert[] = [
    {
      id: '1',
      priority: 'urgent',
      title: 'Urgente: Discrepancia en Nómina Detectada',
      message: 'Se ha identificado una variación significativa en el procesamiento de nómina para el Q3. Se requiere revisión inmediata.',
      timestamp: 'Justo ahora',
      primaryAction: 'investigate',
      primaryActionLabel: 'Investigar'
    },
    {
      id: '2',
      priority: 'action',
      title: 'Acción Requerida: Pago de Factura Vencido',
      message: 'La factura #INV-2024-00123 de "Tech Solutions Inc." está vencida por 3 días.',
      timestamp: 'Hace 5 minutos',
      primaryAction: 'processPayment',
      primaryActionLabel: 'Procesar Pago'
    },
    {
      id: '3',
      priority: 'info',
      title: 'Nueva Carga de Documento: Reporte Financiero Q2',
      message: 'El último reporte financiero trimestral ha sido cargado y está listo para revisión.',
      timestamp: 'Hace 30 minutos',
      primaryAction: 'viewReport',
      primaryActionLabel: 'Ver Reporte'
    },
    {
      id: '4',
      priority: 'reminder',
      title: 'Recordatorio: Fecha Límite de Impuestos Próxima',
      message: 'La fecha límite para presentar impuestos estimados del Q4 se acerca (15 días restantes).',
      timestamp: 'Hace 1 hora',
      primaryAction: 'viewCalendar',
      primaryActionLabel: 'Ver Calendario Fiscal'
    },
    {
      id: '5',
      priority: 'reminder',
      title: 'Actualización del Sistema: Nueva Función Implementada',
      message: 'Se han implementado exitosamente nuevas funciones de categorización de gastos.',
      timestamp: 'Ayer',
      primaryAction: 'learnMore',
      primaryActionLabel: 'Saber Más'
    }
  ];

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Inicialización del componente
  }

  dismissAlert(id: string): void {
    console.log('Descartando alerta:', id);
    this.alerts = this.alerts.filter(a => a.id !== id);
  }

  handleAction(alertItem: Alert): void {
  console.log('Acción ejecutada:', alertItem.primaryAction, 'para alerta:', alertItem.id);
  
  // Mapeo de acciones a rutas
  const actionRoutes: { [key: string]: string } = {
    'investigate': '/dashboard',
    'processPayment': '/receipts',
    'viewReport': '/analytics',
    'viewCalendar': '/admin',
    'learnMore': '/help'
  };

  const route = actionRoutes[alertItem.primaryAction];
  
  if (route) {
    // Mostrar mensaje antes de navegar
    window.alert(`Navegando a ${route} para: ${alertItem.title}`);
    
    // Navegar a la ruta
    this.router.navigate([route]);
  } else {
    window.alert('Esta funcionalidad estará disponible próximamente');
  }
}
}