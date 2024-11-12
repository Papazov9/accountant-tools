import { Component } from '@angular/core';
import {Router, RouterLink} from "@angular/router";
import {ToggleService} from "../services/toggle.service";

@Component({
  selector: 'app-payment-cancel',
  standalone: true,
  imports: [
    RouterLink
  ],
  templateUrl: './payment-cancel.component.html',
  styleUrl: './payment-cancel.component.css'
})
export class PaymentCancelComponent {
  constructor(private toggleService: ToggleService) {}


  goToSub(): void {
    this.toggleService.showSubscriptions();
  }
}
