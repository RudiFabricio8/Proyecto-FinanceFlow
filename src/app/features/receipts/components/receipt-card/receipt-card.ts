import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-receipt-card',
  standalone: true,
  imports: [],
  templateUrl: './receipt-card.component.html',
  styleUrls: ['./receipt-card.component.scss'],
})
export class ReceiptCardComponent {
  @Input() title!: string;
  @Input() date!: string | null;
  @Input() amount!: string;
  @Input() provider!: string;
}
