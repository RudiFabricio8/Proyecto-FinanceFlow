import { Component, EventEmitter, Output } from '@angular/core';
import { NgIf } from '@angular/common';
import { LogoutModalComponent } from '../logout-modal/logout-modal';

@Component({
  selector: 'app-logout-button',
  standalone: true,
  imports: [NgIf, LogoutModalComponent],
  template: `
    <button type="button" class="logout-button" (click)="openModal()">Cerrar sesión</button>

    <app-logout-modal
      *ngIf="showModal"
      (confirm)="handleConfirm()"
      (cancel)="closeModal()"
    ></app-logout-modal>
  `,
  styleUrl: './logout-button.scss',
})
export class LogoutButtonComponent {
  @Output() logout = new EventEmitter<void>();

  showModal = false;

  openModal(): void {
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  handleConfirm(): void {
    this.showModal = false;
    this.logout.emit();
  }
}


