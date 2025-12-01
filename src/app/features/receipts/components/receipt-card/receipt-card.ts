import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-receipt-card',
  templateUrl: './receipt-card.component.html',
  styleUrls: ['./receipt-card.component.scss'],
})
export class ReceiptCardComponent {
  @Input() title!: string;
  @Input() date!: string;
  @Input() amount!: string;
  @Input() provider!: string;
}
