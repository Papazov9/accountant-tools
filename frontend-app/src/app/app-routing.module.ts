import {NgModule} from '@angular/core';
import {PreloadAllModules, RouterModule, Routes} from '@angular/router';
import {AuthGuard} from "./guards/AuthGuard";
import {AuthGuardForHome} from "./guards/AuthGuardForHome";
import {NotAuthGuard} from "./guards/NotAuthGuard";
import {ErrorComponent} from "./error/error.component";
import {AppSelectionOverlayComponent} from "./app-selection-overlay/app-selection-overlay.component";
import {adminGuard} from "./guards/admin.guard";
import {AdminAccessComponent} from "./admin-access/admin-access.component";
import {AdminPanelComponent} from "./admin-panel/admin-panel.component";
import {PendingPaymentsComponent} from "./pending-payments/pending-payments.component";

export const routes: Routes = [
  // Redirect root to /home within the invoice-comparison namespace
  { path: '', redirectTo: 'home', pathMatch: 'full'},
  { path: 'app-selection', component: AppSelectionOverlayComponent, canActivate: [AuthGuard] },

  // Unauthenticated routes
  { path: 'home', loadComponent: () => import('./home/home.component').then(m => m.HomeComponent), canActivate: [AuthGuardForHome]},
  { path: 'login', loadComponent: () => import('./login/login.component').then(m => m.LoginComponent), canActivate: [NotAuthGuard] },
  { path: 'register', loadComponent: () => import('./register/register.component').then(m => m.RegisterComponent), canActivate: [NotAuthGuard] },
  { path: 'confirm-code', loadComponent: () => import('./confirm-code/confirm-code.component').then(m => m.ConfirmCodeComponent), canActivate: [NotAuthGuard] },
  { path: 'contact', loadComponent: () => import('./contacts/contacts.component').then(m => m.ContactsComponent), canActivate: [NotAuthGuard] },

  // Authenticated routes under the /invoice-comparison namespace
  {
    path: 'invoice-comparison',
    canActivate: [AuthGuard], // Apply AuthGuard to the parent path
    children: [
      { path: 'how-it-works', loadComponent: () => import('./how-works/how-works.component').then(m => m.HowWorksComponent) },
      { path: 'home', loadComponent: () => import('./user-home/user-home.component').then(m => m.UserHomeComponent)},
      { path: 'main', loadComponent: () => import('./invoice-comparison/invoice-comparison.component').then(m => m.InvoiceComparisonComponent) },
      { path: 'checkout', loadComponent: () => import('./checkout/checkout.component').then(m => m.CheckoutComponent) },
      { path: 'history', loadComponent: () => import('./history/history.component').then(m => m.HistoryComponent) },
      { path: 'payment-pending', loadComponent: () => import('./payment-pending/payment-pending.component').then(m => m.PaymentPendingComponent) },
      { path: '**', redirectTo: 'home', pathMatch: 'full' }
    ]
  },
  {
    path: 'inventory',
    canActivate: [AuthGuard],
    children: [
      { path: 'home', loadComponent: () => import('./user-home/user-home.component').then(m => m.UserHomeComponent) },
      // { path: 'how-it-works', component: HowWorksComponent },
      // { path: 'success', component: PaymentSuccessComponent },
      // { path: 'cancel', component: PaymentCancelComponent },
      // { path: 'payment-pending', component: PaymentCancelComponent },
      // { path: 'dashboard', component: UserHomeComponent },
      // { path: '', component: InvoiceComparisonComponent }, // default component for invoice-comparison path
      // { path: 'checkout', component: CheckoutComponent },
      // { path: 'history', component: HistoryComponent }
      { path: '**', redirectTo: 'home', pathMatch: 'full' }
    ]
  },

  { path: 'error/:code', component: ErrorComponent },
  { path: 'admin-entry', component: AdminAccessComponent,  canActivate: [adminGuard] },
  { path: 'pending-payments', component: PendingPaymentsComponent,  canActivate: [adminGuard] },
  { path: 'admin-panel', component: AdminPanelComponent,  canActivate: [adminGuard] },
  { path: '**', redirectTo: 'home',  pathMatch: 'full'},
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {preloadingStrategy: PreloadAllModules})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
