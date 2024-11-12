import {Component, OnInit} from '@angular/core';
import {NgForOf} from "@angular/common";
import {plans} from "../../models";


export interface PricingPlan {
  title: string;
  oldPrice: number | null;
  price: number,
  comparisons: number,
  imgUrl: string,
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

  constructor() {
  }

  ngOnInit(): void {
    this.pricingPlans = plans;
  }
}
