import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {LoadingService} from "../services/loading.service";

@Component({
  selector: 'app-payment-success',
  standalone: true,
  imports: [
    RouterLink
  ],
  template: `
    <div class="payment-status-container">
      <div class="status-icon success">
        <i class="fa fa-check-circle"></i>
      </div>
      <h1>Payment Successful</h1>
      <p>Thank you for your purchase! Your subscription has been upgraded.</p>
      <button routerLink="/dashboard">Return to Dashboard</button>
    </div>
  `,
  styleUrl: './payment-success.component.css'
})
export class PaymentSuccessComponent implements OnInit {

  sessionId: string | null = null;
  private STRIPE_API_PAYMENT_STATUS: string = 'http://localhost:8080/api/stripe/payment-status';
  constructor(private http: HttpClient, private route: ActivatedRoute, private router: Router, private loadingService: LoadingService) {
  }

  ngOnInit() {
    this.sessionId = this.route.snapshot.queryParams['sessionId'];
    if (this.sessionId) {
      this.checkPaymentStatus();
    } else {
      this.router.navigate(['/cancel']);
    }
  }

  checkPaymentStatus() {
    this.loadingService.showOverlay();
    this.http.get<{ status: string}>(`${this.STRIPE_API_PAYMENT_STATUS}?sessionId=${this.sessionId}`)
      .subscribe({
        next: (response) => {
          if (response && response.status === 'succeeded') {
            this.loadingService.hideOverlay();
          }else {
            this.router.navigate(['/cancel']);
            this.loadingService.hideOverlay();
          }
        },
        error: () => {
          this.loadingService.hideOverlay();
          this.router.navigate(['/cancel']);
        }
      });
  }
}
