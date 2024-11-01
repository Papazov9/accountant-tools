import {Component, OnInit} from '@angular/core';
import {CurrencyPipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {PricingPlan} from "../home/pricing-section/pricing-section.component";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {LoadingService} from "../services/loading.service";
import {UserProfile, UserService} from "../services/user.service";
import {ToggleService} from "../services/toggle.service";
import Swal from 'sweetalert2';
import {loadStripe} from "@stripe/stripe-js";

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
  isCurrentUserAcknowledge: boolean = false;
  currentUser: UserProfile | null = null;

  constructor(private http: HttpClient, private router: Router, private loadingService: LoadingService, private userService: UserService, private toggleService: ToggleService) {
  }

  ngOnInit() {
    this.loadingService.showOverlay();
    this.userService.fetchUserProfile();
    this.userService.userProfile$.subscribe({
      next: (userProfile) => {
        if (userProfile) {
          console.log(userProfile);
          this.currentUser = userProfile;
          this.isCurrentUserAcknowledge = userProfile.isAcknowledge;
        } else {
          console.log("User not logged in!");
        }
        this.loadPricing();
      },
      error: (error) => {
        console.error();
        this.loadingService.hideOverlay();
      },
      complete: () => this.loadingService.hideOverlay()
    })

  }

  loadPricing() {
    this.http.get<PricingPlan[]>('/assets/pricing-plans.json').subscribe({
      next: (data) => {
        this.subscriptionPlans = data;
        console.log(data);
      },
      error: (error) => {
        this.loadingService.hideOverlay();
        this.router.navigate(['dashboard']);
        this.loadingService.hideOverlay();
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
      Swal.fire({
        title: 'Confirmation',
        text: `Confirm that you want to proceed to checkout with the selected ${this.selectedPlan.title} option which costs ${this.selectedPlan.price} EUR?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#584ca6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes, proceed!',
      }).then(result => {
        if (result.isConfirmed) {
          this.toggleService.hideSubscriptions();
          this.router.navigate(['/checkout'], {state: {plan: this.selectedPlan, user: this.currentUser}});
        }
      });
    }
  }

  closeModal() {
    if (this.isCurrentUserAcknowledge) {
      this.toggleService.hideSubscriptions()
      return;
    }
    Swal.fire({
      title: 'Confirmation',
      text: 'Why settle for less? Unlock the full power of our premium plans and get more comparisons, advanced features, and top-tier support. Or stick with just 1 limited free comparison—your choice! Ready to maximize your potential?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#584ca6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Yes...',
      background: '#ffffff'
    }).then(result => {
      if (result.isConfirmed) {
        this.userService.acceptFreeTrial().subscribe({
          next: (response) => {
            this.toggleService.hideSubscriptions();
          },
          error: (error) => {
            console.error();
          }
        });
      }
    })
  }
}
