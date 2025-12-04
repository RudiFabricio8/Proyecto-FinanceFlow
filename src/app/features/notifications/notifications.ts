// src/app/features/notifications/notifications.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Notification, NotificationAction } from '../../core/models/notification.model';
import { NotificationsService } from '../../core/services/notifications.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.html',
  styleUrls: ['./notifications.scss']
})
export class Notifications implements OnInit {
  notifications: Notification[] = [];
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
        this.notifications = notifications;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading notifications', err);
        this.loading = false;
      }
    });
  }

  getSeverityClass(severity: string): string {
    switch (severity) {
      case 'URGENT': return 'severity-urgent';
      case 'ACTION_REQUIRED': return 'severity-action';
      case 'INFO': return 'severity-info';
      case 'REMINDER': return 'severity-reminder';
      default: return '';
    }
  }

  getRelativeTime(dateString: string): string {
    const now = new Date();
    const date = new Date(dateString);
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`;
    
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    
    const diffDays = Math.floor(diffHours / 24);
    return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
  }

  handleAction(notification: Notification, action: NotificationAction): void {
    console.log(`Action: ${action.action} on notification:`, notification.id);
    
    switch (action.action) {
      case 'DISMISS':
        this.dismissNotification(notification.id);
        break;
      case 'INVESTIGATE':
        // Navigate to relevant page or open modal
        console.log('Navigate to investigation page');
        break;
      case 'PROCESS_PAYMENT':
        // Open payment processing
        console.log('Open payment processing');
        break;
      case 'VIEW_REPORT':
        this.router.navigate(['/receipts']);
        break;
      case 'VIEW_TAX_CALENDAR':
        // Navigate to tax calendar
        console.log('Open tax calendar');
        break;
      default:
        console.log('Custom action');
    }
  }

  dismissNotification(id: string): void {
    this.notificationsService.dismissNotification(id).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(n => n.id !== id);
      },
      error: (err) => console.error('Error dismissing notification', err)
    });
  }
}