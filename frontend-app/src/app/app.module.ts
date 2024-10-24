import {ErrorHandler, NgModule} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { NavbarComponent } from './navbar/navbar.component';
import {NgOptimizedImage} from "@angular/common";
import {WelcomeSectionComponent} from "./home/welcome-section/welcome-section.component";
import {OverviewSectionComponent} from "./home/overview-section/overview-section.component";
import {StepsSectionComponent} from "./home/steps-section/steps-section.component";
import {SweetAlert2Module} from "@sweetalert2/ngx-sweetalert2";
import {GlobalErrorHandler} from "./services/GlobalErrorHandler";

@NgModule({
  declarations: [

  ],
  imports: [
    AppComponent,
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    NgOptimizedImage,
    HomeComponent,
    LoginComponent,
    NavbarComponent,
    WelcomeSectionComponent,
    OverviewSectionComponent,
    StepsSectionComponent,
    SweetAlert2Module.forRoot()
  ],
  providers: [{provide: ErrorHandler, useClass: GlobalErrorHandler}],
  bootstrap: []
})
export class AppModule { }
