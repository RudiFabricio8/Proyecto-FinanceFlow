// src/app/features/notifications/components/alert-card/alert-card.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Notification, NotificationAction } from '../../../../core/models/notification.model';

@Component({
  selector: 'app-alert-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './alert-card.html',
  styleUrls: ['./alert-card.scss']
})
export class AlertCardComponent {
  @Input() alert!: Notification;
  @Output() dismiss = new EventEmitter<string>();
  @Output() action = new EventEmitter<{ notification: Notification; action: NotificationAction }>();

  onDismiss(): void {
    this.dismiss.emit(this.alert.id);
  }

  onAction(action: NotificationAction): void {
    this.action.emit({ notification: this.alert, action });
  }

  getPrimaryActionLabel(): string | null {
    if (!this.alert || !this.alert.actions || this.alert.actions.length === 0) return null;
    const primary = this.alert.actions.find(a => a.isPrimary);
    return primary ? primary.label : this.alert.actions[this.alert.actions.length - 1].label;
  }

  getBorderClass(): string {
    const map: Record<string,string> = {
      URGENT: 'border-urgent',
      ACTION_REQUIRED: 'border-action',
      INFO: 'border-info',
      REMINDER: 'border-reminder'
    };
    return map[this.alert.severity] || '';
  }

  getTitleClass(): string {
    const map: Record<string,string> = {
      URGENT: 'text-urgent',
      ACTION_REQUIRED: 'text-action',
      INFO: 'text-info',
      REMINDER: 'text-reminder'
    };
    return map[this.alert.severity] || '';
  }
}