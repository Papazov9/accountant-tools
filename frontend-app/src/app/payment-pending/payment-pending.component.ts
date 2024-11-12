import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-payment-pending',
  standalone: true,
  imports: [],
  templateUrl: './payment-pending.component.html',
  styleUrl: './payment-pending.component.css'
})
export class PaymentPendingComponent implements OnInit{
  email: string | null = null;

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.email = params['email'] || 'your email';
    });
  }
}
