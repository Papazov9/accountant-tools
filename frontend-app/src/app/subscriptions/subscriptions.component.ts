import {Component, OnInit} from '@angular/core';
import {CurrencyPipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {PricingPlan} from "../home/pricing-section/pricing-section.component";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {LoadingService} from "../services/loading.service";
import {UserService} from "../services/user.service";
import {ToggleService} from "../services/toggle.service";
import Swal from 'sweetalert2';

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

  constructor(private http: HttpClient, private router: Router, private loadingService: LoadingService, private userService: UserService, private toggleService: ToggleService) {
  }

  ngOnInit() {
    this.loadingService.showOverlay();
    this.userService.fetchUserProfile().subscribe({
      next: (userProfile) => {
        if (userProfile) {
          this.isCurrentUserAcknowledge = userProfile.isAcknowledge;
        }else {
          console.log("User not logged in!");
        }
        this.http.get<PricingPlan[]>('/assets/pricing-plans.json').subscribe({
          next: (data) => {
            this.subscriptionPlans = data;
          },
          error: (error) => {
            this.loadingService.hideOverlay();
            this.router.navigate(['dashboard']);
            console.log(error);
          }
        });
      },
      error: (error) => {
        console.error(error.error.message);
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
        title: 'Confirmation',
        text: `Confirm that you want to proceed to the payment of the selected ${this.selectedPlan.title} option which costs ${this.selectedPlan.price} EUR?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#584ca6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes, proceed!',
        background: '#ffffff'
      }).then(result => {
        if (result.isConfirmed && this.selectedPlan) {
          this.router.navigate(['/payment'], {queryParams: {plan: this.selectedPlan.title, price: this.selectedPlan.price}});
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
            console.log("Success: ", response);
            this.toggleService.hideSubscriptions()
          },
          error: (error) => {
            console.error(error);
          }
        });
      }
    })
  }
}
