import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { provideRouter } from '@angular/router';
import { routes } from './app/app-routing.module';
import {provideHttpClient, withInterceptors} from '@angular/common/http'; // Optional if you use HttpClient
import { FormsModule } from '@angular/forms';
import {authTokenInterceptor} from "./app/interceptors/auth-token.interceptor"; // Optional if you use forms

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([authTokenInterceptor])
    ), // Optional if HttpClientModule is required
    FormsModule, // Optional if forms are required
  ]
}).catch(err => console.error());
