import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReceiptCardComponent } from './components/receipt-card/receipt-card';

interface Receipt {
  title: string;
  date: string;
  amount: string;
  provider: string;
}

@Component({
  selector: 'app-receipts',
  templateUrl: './receipts.component.html',
  styleUrls: ['./receipts.component.scss'],
  standalone: true,
  imports: [CommonModule, ReceiptCardComponent],
})
export class Receipts implements OnInit {
  receipts: Receipt[] = [];

  ngOnInit() {
    // Datos de ejemplo para mostrar en las tarjetas
    this.receipts = [
      {
        title: 'Recibo de Compra - Supermercado A',
        date: '26 de Julio, 2024',
        amount: '$125.50',
        provider: 'Supermercado A',
      },
      {
        title: 'Factura de Servicios - Electricidad',
        date: '20 de Julio, 2024',
        amount: '$89.75',
        provider: 'Empresa Eléctrica XYZ',
      },
      {
        title: 'Recibo de Caja - Restaurante El Sabor',
        date: '15 de Julio, 2024',
        amount: '$45.00',
        provider: 'Restaurante El Sabor',
      },
    ];
  }

  uploadFile() {
    console.log('Subir archivo presionado');
  }
}

