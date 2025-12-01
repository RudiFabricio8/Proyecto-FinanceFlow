import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-logout-modal',
  templateUrl: './logout-modal.component.html',
  styleUrls: ['./logout-modal.component.scss'],
  standalone: true,
  imports: [CommonModule],
})
export class LogoutModalComponent implements OnInit {
  isOpen = false;
  currentUser = 'Usuario'; // placeholder; obtén del servicio de auth si existe

  constructor(private router: Router) {}

  ngOnInit() {
    // Puedes obtener el usuario del localStorage o un servicio de autenticación
    const user = localStorage.getItem('currentUser') || 'Usuario';
    this.currentUser = user;
  }

  openModal() {
    this.isOpen = true;
  }

  closeModal() {
    this.isOpen = false;
  }

  confirmLogout() {
    // Limpiar localStorage o ejecutar logout
    localStorage.removeItem('currentUser');
    localStorage.removeItem('authToken');
    
    // Redirigir a la página principal o login
    this.router.navigate(['/']);
    this.closeModal();
  }
}
