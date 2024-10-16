import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from "./login/login.component";
import {HomeComponent} from "./home/home.component";
import {RegisterComponent} from "./register/register.component";
import {UserHomeComponent} from "./user-home/user-home.component";
import {AuthGuard} from "./guards/AuthGuard";
import {InvoiceComparisonComponent} from "./invoice-comparison/invoice-comparison.component";
import {AuthGuardForHome} from "./guards/AuthGuardForHome";

export const routes: Routes = [
  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: 'home', component: HomeComponent, canActivate: [AuthGuardForHome]},
  {path: 'dashboard', component: UserHomeComponent, canActivate: [AuthGuard]},
  {path: 'invoice-comparison', component: InvoiceComparisonComponent, canActivate: [AuthGuard]},
  {path: 'login', component: LoginComponent},
  {path: 'register', component: RegisterComponent},
  {path: '**', redirectTo: 'home'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
