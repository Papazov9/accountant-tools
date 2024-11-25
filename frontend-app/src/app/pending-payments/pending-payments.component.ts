import {Component, OnInit} from '@angular/core';
import {PaymentService} from "../services/payment.service";
import {Router} from "@angular/router";
import {AuthService, CurrentUser} from "../services/auth.service";
import {NgForOf} from "@angular/common";

export interface PaymentModel {
  invoiceNumber: string,
  status: string,
  googleDriveLink: string
  customer: string
}

@Component({
  selector: 'app-pending-payments',
  standalone: true,
  imports: [
    NgForOf
  ],
  templateUrl: './pending-payments.component.html',
  styleUrl: './pending-payments.component.css'
})
export class PendingPaymentsComponent implements OnInit {

  pendingPayments: PaymentModel[] = [];
  paginatedPendingPayments: PaymentModel[] = [];
  itemsPerPage: number = 10;
  totalPages: number = 0;
  pageText: string = '';
  currentPage: number = 1;

  constructor(private authService: AuthService, private paymentService: PaymentService, private router: Router) {
  }

  ngOnInit() {
    this.authService.currentUser$.subscribe(currentUser => {
      if (currentUser && currentUser.roles.includes("ADMIN")) {
        this.paymentService.loadPendingPayments().subscribe((payments: PaymentModel[]) => {
          this.pendingPayments = payments;
          this.totalPages = Math.ceil(this.pendingPayments.length / this.itemsPerPage);
          this.pageText = `${this.currentPage} of ${this.totalPages}`;
          if (this.totalPages === 0) {
            this.currentPage = 0;
          }

          console.log(this.currentPage, this.totalPages);
          this.paginatePendingPayments();
        });
      }
    });

  }

  private paginatePendingPayments() {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    this.paginatedPendingPayments = this.pendingPayments.slice(start, start + this.itemsPerPage);
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.paginatePendingPayments();
    }
  }

  prevPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.paginatePendingPayments();
    }
  }

  markAsCompleted(invoiceNumber: string) {

  }

  declinePayment(invoiceNumber: string) {

  }
}
