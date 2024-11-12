import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from "./login/login.component";
import {HomeComponent} from "./home/home.component";
import {RegisterComponent} from "./register/register.component";
import {UserHomeComponent} from "./user-home/user-home.component";
import {AuthGuard} from "./guards/AuthGuard";
import {InvoiceComparisonComponent} from "./invoice-comparison/invoice-comparison.component";
import {AuthGuardForHome} from "./guards/AuthGuardForHome";
import {NotAuthGuard} from "./guards/NotAuthGuard";
import {ErrorComponent} from "./error/error.component";
import {PaymentSuccessComponent} from "./payment-success/payment-success.component";
import {PaymentCancelComponent} from "./payment-cancel/payment-cancel.component";
import {HistoryComponent} from "./history/history.component";
import {ConfirmCodeComponent} from "./confirm-code/confirm-code.component";
import {CheckoutComponent} from "./checkout/checkout.component";

export const routes: Routes = [
  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: 'home', component: HomeComponent, canActivate: [AuthGuardForHome]},
  {path: 'success', component: PaymentSuccessComponent, canActivate: [AuthGuard]},
  {path: 'cancel', component: PaymentCancelComponent, canActivate: [AuthGuard]},
  {path: 'payment-pending', component: PaymentCancelComponent, canActivate: [AuthGuard]},
  {path: 'dashboard', component: UserHomeComponent, canActivate: [AuthGuard]},
  {path: 'invoice-comparison', component: InvoiceComparisonComponent, canActivate: [AuthGuard]},
  {path: 'checkout', component: CheckoutComponent, canActivate: [AuthGuard]},
  {path: 'history', component: HistoryComponent, canActivate: [AuthGuard]},
  {path: 'login', component: LoginComponent, canActivate: [NotAuthGuard]},
  {path: 'register', component: RegisterComponent, canActivate: [NotAuthGuard]},
  {path: 'confirm-code', component: ConfirmCodeComponent, canActivate: [NotAuthGuard]},
  {path: '**', redirectTo: 'home'},
  {path: 'error/:code', component: ErrorComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
