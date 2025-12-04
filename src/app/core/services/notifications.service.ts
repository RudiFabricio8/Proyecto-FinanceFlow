import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  private apiUrl = `${environment.apiUrl}/notifications`;

  constructor(private http: HttpClient) {}

  listNotifications(): Observable<Notification[]> {
    // Return mock notifications with priority and actions
    const mockNotifications: Notification[] = [
      {
        id: '1',
        organizationId: 'org-1',
        title: 'Urgent: Payroll Discrepancy Detected',
        message: 'A significant variance in payroll processing for Q3 has been identified. Immediate review required.',
        type: 'PAYROLL_DISCREPANCY',
        severity: 'URGENT',
        isRead: false,
        createdAt: new Date().toISOString(),
        actions: [
          { label: 'Dismiss', action: 'DISMISS', isPrimary: false },
          { label: 'Investigate', action: 'INVESTIGATE', isPrimary: true }
        ]
      },
      {
        id: '2',
        organizationId: 'org-1',
        title: 'Action Required: Invoice Payment Overdue',
        message: 'Invoice #INV-2024-00123 from "Tech Solutions Inc." is 3 days overdue.',
        type: 'INVOICE_OVERDUE',
        severity: 'ACTION_REQUIRED',
        isRead: false,
        createdAt: new Date(Date.now() - 5 * 60000).toISOString(), // 5 minutes ago
        actions: [
          { label: 'Dismiss', action: 'DISMISS', isPrimary: false },
          { label: 'Process Payment', action: 'PROCESS_PAYMENT', isPrimary: true }
        ]
      },
      {
        id: '3',
        organizationId: 'org-1',
        title: 'New Document Upload: Q2 Financial Report',
        message: 'The latest quarterly financial report has been uploaded and is ready for review.',
        type: 'DOCUMENT_UPLOAD',
        severity: 'INFO',
        isRead: false,
        createdAt: new Date(Date.now() - 30 * 60000).toISOString(), // 30 minutes ago
        actions: [
          { label: 'Dismiss', action: 'DISMISS', isPrimary: false },
          { label: 'View Report', action: 'VIEW_REPORT', isPrimary: true }
        ]
      },
      {
        id: '4',
        organizationId: 'org-1',
        title: 'Reminder: Upcoming Tax Deadline',
        message: 'The deadline for submitting Q4 estimated taxes is approaching (15 days left).',
        type: 'TAX_DEADLINE',
        severity: 'REMINDER',
        isRead: false,
        createdAt: new Date(Date.now() - 60 * 60000).toISOString(), // 1 hour ago
        actions: [
          { label: 'Dismiss', action: 'DISMISS', isPrimary: false },
          { label: 'View Tax Calendar', action: 'VIEW_TAX_CALENDAR', isPrimary: true }
        ]
      }
    ];

    return of(mockNotifications);
  }

  markAsRead(notificationId: string): Observable<Notification> {
    return this.http.put<Notification>(`${this.apiUrl}/${notificationId}/read`, {});
  }

  dismissNotification(notificationId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${notificationId}`);
  }
}
