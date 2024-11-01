import {Component, OnInit} from '@angular/core';
import {loadStripe} from "@stripe/stripe-js";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {CurrencyPipe, NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [
    FormsModule,
    CurrencyPipe,
    NgIf,
    NgForOf,
    ReactiveFormsModule
  ],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css'
})
export class CheckoutComponent implements OnInit {
  checkoutForm!: FormGroup;
  selectedPlan: any;
  private STRIPE_API_URL = 'http://localhost:8080/api/stripe/create-checkout-session';
  private stripePromise = loadStripe('pk_test_51QBLeXFWqYIjorXPmniB1m4qZDE6T5sNGjBYbbsTE57yqQytbkwCDwqFDjN47uKKw6fChTU8VIXiJwK49jY0KjBj00o96lorNi');

  constructor(
    private http: HttpClient,
    private router: Router,
    private fb: FormBuilder
  ) {
    const navigation = this.router.getCurrentNavigation();
    const state = navigation?.extras.state as { plan: any };
    this.selectedPlan = state?.plan;
  }

  ngOnInit(): void {
    this.checkoutForm = this.fb.group({
      contactEmail: ['', [Validators.required, Validators.email]],
      paymentMethod: ['card', Validators.required],
      companyName: [''],
      billingAddress: [''],
      vatNumber: ['']
    });

    // Update form validators based on payment method
    this.checkoutForm.get('paymentMethod')?.valueChanges.subscribe((paymentMethod: string) => {
      if (paymentMethod === 'bank') {
        this.checkoutForm.get('companyName')?.setValidators([Validators.required]);
        this.checkoutForm.get('billingAddress')?.setValidators([Validators.required]);
      } else {
        this.checkoutForm.get('companyName')?.clearValidators();
        this.checkoutForm.get('billingAddress')?.clearValidators();
      }
      this.checkoutForm.get('companyName')?.updateValueAndValidity();
      this.checkoutForm.get('billingAddress')?.updateValueAndValidity();
    });
  }

  submitCheckoutForm(): void {
    if (this.checkoutForm.invalid) {
      return;
    }

    const checkoutData = {
      title: this.selectedPlan?.title,
      price: this.selectedPlan?.price,
      ...this.checkoutForm.value
    };

    this.http.post<{ url: string }>(this.STRIPE_API_URL, checkoutData)
      .subscribe(async (response) => {
        const stripe = await this.stripePromise;
        if (stripe && response.url) {
          await stripe.redirectToCheckout({sessionId: response.url});
        }
      });
  }
}
