// src/app/features/notifications/components/alert-card/alert-card.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Alert } from '../../../../core/models/notification.model';

@Component({
  selector: 'app-alert-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './alert-card.html',
  styleUrl: './alert-card.scss'
})
export class AlertCardComponent {
  @Input() alert!: Alert;
  @Output() dismiss = new EventEmitter<string>();
  @Output() action = new EventEmitter<Alert>();

  onDismiss(): void {
    this.dismiss.emit(this.alert.id);
  }

  onAction(): void {
    this.action.emit(this.alert);
  }

  getBorderClass(): string {
    const classes = {
      urgent: 'border-urgent',
      action: 'border-action',
      info: 'border-info',
      reminder: 'border-reminder'
    };
    return classes[this.alert.priority];
  }

  getTitleClass(): string {
    const classes = {
      urgent: 'text-urgent',
      action: 'text-action',
      info: 'text-info',
      reminder: 'text-reminder'
    };
    return classes[this.alert.priority];
  }
}