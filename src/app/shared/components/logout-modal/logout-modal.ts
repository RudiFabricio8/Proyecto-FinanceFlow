import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-logout-modal',
  standalone: true,
  templateUrl: './logout-modal.html',
  styleUrls: ['./logout-modal.scss'],
})
export class LogoutModalComponent {
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  onConfirm(): void {
    this.confirm.emit();
  }

  onCancel(): void {
    this.cancel.emit();
  }
}


