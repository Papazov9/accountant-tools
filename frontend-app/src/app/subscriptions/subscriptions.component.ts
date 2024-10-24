import {Component, OnInit} from '@angular/core';
import {CurrencyPipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {PricingPlan} from "../home/pricing-section/pricing-section.component";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {LoadingService} from "../services/loading.service";
import {UserService} from "../services/user.service";
import {ToggleService} from "../services/toggle.service";

@Component({
  selector: 'app-subscriptions',
  standalone: true,
  imports: [
    NgIf,
    NgClass,
    NgForOf,
    CurrencyPipe
  ],
  templateUrl: './subscriptions.component.html',
  styleUrl: './subscriptions.component.css'
})
export class SubscriptionsComponent implements OnInit {
  subscriptionPlans: PricingPlan[] = [];
  selectedPlan: PricingPlan | null = null;

  constructor(private http: HttpClient, private router: Router, private loadingService: LoadingService, private userService: UserService, private toggleService: ToggleService) {
  }

  ngOnInit() {
    this.http.get<PricingPlan[]>('/assets/pricing-plans.json').subscribe({
      next: (data) => {
        this.loadingService.showOverlay();
        this.subscriptionPlans = data;
      },
      error: (error) => {
        this.loadingService.hideOverlay();;
        this.router.navigate(['dashboard']);
        console.log(error);
      },
      complete: () => this.loadingService.hideOverlay()
    });
  }

  selectPlan(plan: PricingPlan) {
    if (this.selectedPlan === plan) {
      this.selectedPlan = null;
    } else {
      this.selectedPlan = plan;
    }
  }

  proceedToPayment() {
    if (this.selectedPlan) {
      this.router.navigate(['/payment'], {queryParams: {plan: this.selectedPlan.title, price: this.selectedPlan.price}});
    }
  }

  closeModal() {
    this.toggleService.hideSubscriptions();
  }
}
