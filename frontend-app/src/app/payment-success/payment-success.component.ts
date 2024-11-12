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
  templateUrl: './payment-success.component.html',
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
    this.http.get<{ status: string, message: string}>(`${this.STRIPE_API_PAYMENT_STATUS}?sessionId=${this.sessionId}`)
      .subscribe({
        next: (response) => {
          if (response && response.status === 'succeeded') {
            console.log(response.message);
            this.loadingService.hideOverlay();
          }else {
            console.log(response.message);
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
