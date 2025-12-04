// src/app/features/notifications/notifications.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Alert, Notification, NotificationSeverity } from '../../core/models/notification.model';
import { NotificationsService } from '../../core/services/notifications.service';
import { AlertCardComponent } from './components/alert-card/alert-card';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, AlertCardComponent],
  templateUrl: './notifications.html',
  styleUrl: './notifications.scss'
})
export class Notifications implements OnInit {
  alerts: Alert[] = [];
  loading = true;

  constructor(
    private router: Router,
    private notificationsService: NotificationsService
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.loading = true;
    this.notificationsService.listNotifications().subscribe({
      next: (notifications) => {
        this.alerts = notifications.map(n => this.mapToAlert(n));
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading notifications', err);
        this.loading = false;
      }
    });
  }

  dismissAlert(id: string): void {
    this.notificationsService.markAsRead(id).subscribe({
      next: () => {
        this.alerts = this.alerts.filter(a => a.id !== id);
      },
      error: (err) => console.error('Error marking notification as read', err)
    });
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

    const route = actionRoutes[alertItem.primaryAction || ''] || '/dashboard';
    
    if (route) {
      this.router.navigate([route]);
    }
  }

  private mapToAlert(notification: Notification): Alert {
    return {
      id: notification.id,
      priority: this.mapSeverityToPriority(notification.severity),
      title: notification.title,
      message: notification.message,
      timestamp: new Date(notification.createdAt).toLocaleString(),
      primaryAction: 'investigate', // Default action, could be derived from type
      primaryActionLabel: 'Ver Detalles'
    };
  }

  private mapSeverityToPriority(severity: NotificationSeverity): 'urgent' | 'action' | 'info' | 'reminder' {
    switch (severity) {
      case 'ERROR': return 'urgent';
      case 'WARNING': return 'action';
      case 'INFO': return 'info';
      case 'SUCCESS': return 'info';
      default: return 'info';
    }
  }
}