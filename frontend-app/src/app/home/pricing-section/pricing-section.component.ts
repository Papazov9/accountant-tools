import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {NgForOf} from "@angular/common";

export interface PricingPlan {
  title: string;
  price: string,
  comparisons: string,
  pros: string[],
  buttonText: string,
}


@Component({
  selector: 'app-pricing-section',
  standalone: true,
  imports: [
    NgForOf
  ],
  templateUrl: './pricing-section.component.html',
  styleUrl: './pricing-section.component.css'
})

export class PricingSectionComponent implements OnInit {
  pricingPlans: PricingPlan[] = [];

  constructor(private http: HttpClient) {
  }

  ngOnInit(): void {
    this.http.get<PricingPlan[]>('/assets/pricing-plans.json').subscribe(data => {
      this.pricingPlans = data;
    });
  }

}
