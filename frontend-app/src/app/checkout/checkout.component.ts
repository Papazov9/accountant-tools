import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {AsyncPipe, CurrencyPipe, NgForOf, NgIf} from "@angular/common";
import {PricingPlan} from "../home/pricing-section/pricing-section.component";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatInputModule} from "@angular/material/input";
import {map, Observable, startWith, switchMap} from "rxjs";
import {LocationService} from "../services/location.service";

export interface PaymentRequest {
  contactEmail: string
  paymentMethod: string
  companyName: string
  billingAddress: string
  price: string
  subscriptionType: string
}

interface PaymentResponse {
  url?: string;
  status: string;
  customer_id: string;
  email: string;
}

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [
    FormsModule,
    CurrencyPipe,
    NgIf,
    NgForOf,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatInputModule,
    AsyncPipe,
  ],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css'
})
export class CheckoutComponent implements OnInit {
  checkoutForm!: FormGroup;
  selectedPlan: PricingPlan;
  filteredCountries$!: Observable<string[]>;
  filteredCities$!: Observable<string[]>;
  filteredPostalCodes$!: Observable<string[]>;
  private STRIPE_API_URL = 'http://localhost:8080/api/stripe/create-checkout-session';

  constructor(
    private http: HttpClient,
    private router: Router,
    private fb: FormBuilder,
    private locationService: LocationService
  ) {
    const navigation = this.router.getCurrentNavigation();
    const state = navigation?.extras.state as { plan: any };
    this.selectedPlan = state?.plan;
  }

  ngOnInit(): void {
    this.checkoutForm = this.fb.group({
      contactEmail: ['', [Validators.required, Validators.email]],
      companyName: ['', Validators.required],
      addressLine1: ['', Validators.required],
      addressLine2: [''],
      city: ['', Validators.required],
      postalCode: ['', Validators.required],
      country: ['', Validators.required]
    });

    this.filteredCountries$ = this.checkoutForm.get('country')!.valueChanges.pipe(
      startWith(''),
      switchMap(value => {
        return this.locationService.getCountries().pipe(
          map(countries => {
            return countries.filter(country =>
              country.toLowerCase().includes(value?.toLowerCase() || '')
            );
          })
        );
      }
    )
    );
  }

  submitCheckoutForm(): void {
    if (this.checkoutForm.invalid) {
      return;
    }

    const checkoutData: PaymentRequest = {
      ...this.checkoutForm.value,
      price: this.selectedPlan?.price * 100,
      subscriptionType: this.selectedPlan?.title,
    };

    this.http.post<PaymentResponse>(this.STRIPE_API_URL, checkoutData).subscribe({
      next: response => {
        if (response.status === 'success') {
          this.router.navigate(['/payment-pending'], {queryParams: {email: response.email}});
        }
      }
    })
  }
}
