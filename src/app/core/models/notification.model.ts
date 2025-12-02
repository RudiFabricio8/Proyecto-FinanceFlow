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