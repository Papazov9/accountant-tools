import { Component } from '@angular/core';
import {Router, RouterLink} from "@angular/router";
import {ToggleService} from "../services/toggle.service";

@Component({
  selector: 'app-payment-cancel',
  standalone: true,
  imports: [
    RouterLink
  ],
  template: `
    <div class="payment-status-container">
      <div class="status-icon cancel">
        <i class="fa fa-times-circle"></i>
      </div>
      <h1>Payment Canceled</h1>
      <p>Your payment process was canceled. Please try again or select another plan.</p>
      <button (click)="goToSub()">Return to Subscriptions</button>
      <button routerLink="/dashboard">Go to Dashboard</button>
    </div>
  `,
  styleUrl: './payment-cancel.component.css'
})
export class PaymentCancelComponent {
  constructor(private toggleService: ToggleService) {}


  goToSub(): void {
    this.toggleService.showSubscriptions();
  }
}
