// Notification types and severity levels
export type NotificationType = 'PAYROLL_DISCREPANCY' | 'INVOICE_OVERDUE' | 'DOCUMENT_UPLOAD' | 'TAX_DEADLINE' | 'SYSTEM';
export type NotificationSeverity = 'URGENT' | 'ACTION_REQUIRED' | 'INFO' | 'REMINDER';

export interface NotificationAction {
  label: string;
  action: 'DISMISS' | 'INVESTIGATE' | 'PROCESS_PAYMENT' | 'VIEW_REPORT' | 'VIEW_TAX_CALENDAR' | 'CUSTOM';
  isPrimary: boolean;
}

export interface Notification {
  id: string;
  organizationId: string;
  title: string;
  message: string;
  type: NotificationType;
  severity: NotificationSeverity;
  isRead: boolean;
  createdAt: string;
  actions: NotificationAction[];
  relatedEntityId?: string; // Optional link to related document, period, etc.
}

export interface CreateNotificationRequest {
  title: string;
  message: string;
  type: NotificationType;
  severity: NotificationSeverity;
}