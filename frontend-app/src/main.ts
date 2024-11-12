/// <reference types="@angular/localize" />

import { bootstrapApplication } from '@angular/platform-browser';
import {AppComponent} from './app/app.component';
import { provideRouter } from '@angular/router';
import { routes } from './app/app-routing.module';
import {HttpClient, provideHttpClient, withInterceptors} from '@angular/common/http'; // Optional if you use HttpClient
import { FormsModule } from '@angular/forms';
import {authTokenInterceptor} from "./app/interceptors/auth-token.interceptor";
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([authTokenInterceptor])
    ), // Optional if HttpClientModule is required
    FormsModule,
    provideAnimationsAsync()
  ]
}).catch(err => console.error());
