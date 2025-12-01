// src/app/features/receipts/receipts.ts
import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Receipt } from '../../core/models/receipt.model';
import { ReceiptCardComponent } from './components/receipt-card/receipt-card';

@Component({
  selector: 'app-receipts',
  standalone: true,
  imports: [CommonModule, ReceiptCardComponent],
  templateUrl: './receipts.html',
  styleUrl: './receipts.scss'
})
export class Receipts implements OnInit {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  receipts: Receipt[] = [
    {
      id: '1',
      title: 'Recibo de Compra - Supermercado A',
      date: '26 de Julio, 2024',
      amount: 125.50,
      provider: 'Supermercado A',
      type: 'receipt'
    },
    {
      id: '2',
      title: 'Factura de Servicios - Electricidad',
      date: '20 de Julio, 2024',
      amount: 89.75,
      provider: 'Empresa Eléctrica XYZ',
      type: 'invoice'
    },
    {
      id: '3',
      title: 'Recibo de Caja - Restaurante El Sabor',
      date: '15 de Julio, 2024',
      amount: 45.00,
      provider: 'Restaurante El Sabor',
      type: 'receipt'
    }
  ];

  constructor() {}

  ngOnInit(): void {
    // Inicialización del componente
  }

  triggerFileUpload(): void {
    this.fileInput.nativeElement.click();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      console.log('Archivo seleccionado:', file.name);
      console.log('Tamaño:', (file.size / 1024).toFixed(2), 'KB');
      console.log('Tipo:', file.type);
      
      // Aquí iría la lógica para subir el archivo al servidor
      alert(`Archivo "${file.name}" listo para subir.\n(En producción se subiría al servidor)`);
      
      // Limpiar el input para permitir seleccionar el mismo archivo de nuevo
      input.value = '';
    }
  }
}