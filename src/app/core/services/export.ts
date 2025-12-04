import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { Transaction } from '../models/transaction.model';

@Injectable({
  providedIn: 'root'
})
export class ExportService {

  constructor() { }

  // Exportar a Excel
  exportToExcel(data: any[], filename: string = 'export'): void {
    // Crear un nuevo libro de trabajo
    const worksheet = XLSX.utils.json_to_sheet(data);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Data');

    // Generar el archivo Excel
    XLSX.writeFile(workbook, `${filename}_${this.getDateString()}.xlsx`);
  }

  // Exportar a PDF
  exportToPDF(data: any[], columns: string[], filename: string = 'export'): void {
    const doc = new jsPDF();

    doc.setFontSize(18);
    doc.text(filename, 14, 22);

    // Preparar datos para la tabla
    const headers = [columns];
    const rows = data.map(item => columns.map(col => item[col] || ''));

    // Generar tabla
    autoTable(doc, {
      head: headers,
      body: rows,
      startY: 30,
      theme: 'grid',
      styles: { fontSize: 10 },
      headStyles: { fillColor: [25, 64, 7] }
    });

    // Guardar PDF
    doc.save(`${filename}_${this.getDateString()}.pdf`);
  }

  // Exportar transacciones a Excel
  exportTransactionsToExcel(transactions: Transaction[]): void {
    const dataToExport = transactions.map(t => ({
      'Fecha': t.date,
      'Descripción': t.description,
      'Categoría': t.category,
      'Monto': t.type === 'INCOME' ? `+$${t.amount.toFixed(2)}` : `-$${t.amount.toFixed(2)}`
    }));

    this.exportToExcel(dataToExport, 'Transacciones');
  }

  // Exportar transacciones a PDF
  exportTransactionsToPDF(transactions: Transaction[]): void {
    const dataToExport = transactions.map(t => ({
      date: t.date,
      description: t.description,
      category: t.category,
      amount: t.type === 'INCOME' ? `+$${t.amount.toFixed(2)}` : `-$${t.amount.toFixed(2)}`
    }));

    this.exportToPDF(
      dataToExport,
      ['date', 'description', 'category', 'amount'],
      'Transacciones'
    );
  }

  // Obtener fecha formateada
  private getDateString(): string {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}${month}${day}`;
  }
}