import {Component, OnInit} from '@angular/core';
import {CurrencyPipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {Router} from "@angular/router";
import {LoadingService} from "../services/loading.service";
import {UserProfile, UserService} from "../services/user.service";
import {ToggleService} from "../services/toggle.service";
import Swal from 'sweetalert2';
import {plans} from "../models";
import {PricingPlan} from "../home/pricing-section/pricing-section.component";

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
  selectedPlan: PricingPlan | null | undefined = null;
  isCurrentUserAcknowledge: boolean = false;
  currentUser: UserProfile | null = null;

  constructor(private router: Router, private loadingService: LoadingService, private userService: UserService, private toggleService: ToggleService) {
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
        this.subscriptionPlans = plans;
        this.loadingService.hideOverlay()
      },
      error: (error) => {
        console.error(error);
        this.loadingService.hideOverlay();
      },
      complete: () => this.loadingService.hideOverlay()
    })

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
        title: $localize`Confirmation`,
        text: $localize`Confirm that you want to proceed to checkout with the selected ${this.selectedPlan.title} option which costs ${this.selectedPlan.price} EUR?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#584ca6',
        cancelButtonColor: '#d33',
        confirmButtonText: $localize`Yes, proceed!`,
        cancelButtonText: $localize`Cancel`
      }).then(result => {
        if (result.isConfirmed) {
          this.toggleService.hideSubscriptions();
          this.router.navigate(['/invoice-comparison/checkout'], {state: {plan: this.selectedPlan, user: this.currentUser}});
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
      title: $localize`Confirmation`,
      text: $localize`Why settle for less? Unlock the full power of our premium plans and get more comparisons, advanced features, and top-tier support. Or stick with just 1 limited free comparison—your choice! Ready to maximize your potential?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#584ca6',
      cancelButtonColor: '#d33',
      confirmButtonText: $localize`Yes`,
      cancelButtonText: $localize`Cancel`,
      background: '#ffffff'
    }).then(result => {
      if (result.isConfirmed) {
        this.userService.acceptFreeTrial().subscribe({
          next: () => {
            this.toggleService.hideSubscriptions();
          },
          error: () => {
            console.error();
          }
        });
      }
    })
  }
}
