// Notification types and severities matching backend
export type NotificationType = 'PERIOD_CREATED' | 'CALCULATION_COMPLETED' | 'DOCUMENT_UPLOADED' | 'DEADLINE_APPROACHING';
export type NotificationSeverity = 'INFO' | 'WARNING' | 'ERROR' | 'SUCCESS';

export interface Notification {
  id: string;
  userId: string;
  title: string;
  type: NotificationType;
  severity: NotificationSeverity;
  message: string;
  isRead: boolean;
  createdAt: string;
}

// Legacy Alert interface (can be removed if not used)
export type AlertPriority = 'urgent' | 'action' | 'info' | 'reminder';

export interface Alert {
  id: string;
  priority: AlertPriority;
  title: string;
  message: string;
  timestamp: string;
  primaryAction: string;
  primaryActionLabel: string;
}