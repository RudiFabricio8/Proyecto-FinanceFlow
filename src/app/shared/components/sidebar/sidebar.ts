// src/app/shared/components/sidebar/sidebar.ts
import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MenuItem } from '../../../core/models/menu-item.model';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss'
})
export class Sidebar {
  @Output() sidebarHover = new EventEmitter<boolean>();

  menuItems: MenuItem[] = [
    {
      icon: 'payments',
      label: 'Gestión de nómina',
      route: '/payroll'
    },
    {
      icon: 'receipt_long',
      label: 'Recibos y documentos',
      route: '/receipts'
    },
    {
      icon: 'analytics',
      label: 'Reportes y análisis',
      route: '/analytics'
    },
    {
      icon: 'notifications',
      label: 'Notificaciones',
      route: '/notifications'
    },
    {
      icon: 'admin_panel_settings',
      label: 'Administración y configuración',
      route: '/admin'
    }
  ];

  footerItems: MenuItem[] = [
    {
      icon: 'help_outline',
      label: 'Ayuda',
      route: '/help'
    },
    {
      icon: 'logout',
      label: 'Cerrar Sesión',
      route: '/logout'
    }
  ];

  onMouseEnter(): void {
    this.sidebarHover.emit(true);
  }

  onMouseLeave(): void {
    this.sidebarHover.emit(false);
  }

  onMenuItemClick(item: MenuItem): void {
    console.log('Navegando a:', item.route);
  }
}