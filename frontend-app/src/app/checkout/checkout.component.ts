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
import {UserService} from "../services/user.service";
import {AppSelectionService} from "../services/app-selection.service";

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
  currentEmail: string = '';
  filteredCountries$!: Observable<string[]>;
  filteredCities$!: Observable<string[]>;
  filteredPostalCodes$!: Observable<string[]>;
  private STRIPE_API_URL = 'http://localhost:8080/api/stripe/create-checkout-session';
  private basePath!: 'invoice-comparison' | 'inventory';

  constructor(
    private http: HttpClient,
    private router: Router,
    private fb: FormBuilder,
    private locationService: LocationService,
    private userService: UserService,
    private appSelectionService: AppSelectionService,
  ) {
    const navigation = this.router.getCurrentNavigation();
    const state = navigation?.extras.state as { plan: any };
    this.selectedPlan = state?.plan;
    console.log(this.selectedPlan);
  }

  ngOnInit(): void {
    this.userService.userProfile$.subscribe({
      next: userProfile => {
        if (userProfile) {
          this.currentEmail = userProfile.email;
        }
      }
    });
    this.checkoutForm = this.fb.group({
      contactEmail: [`${this.currentEmail}`, [Validators.required, Validators.email]],
      companyName: ['', Validators.required],
      bulstat: ['', Validators.required],
      vatRegistration: ['', Validators.required],
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

    this.appSelectionService.selectedApp$.subscribe(app => this.basePath = app);
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
          this.router.navigate([`/${this.basePath}/payment-pending`], {queryParams: {email: response.email}});
        }
      }
    })
  }
}
