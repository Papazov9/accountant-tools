import { Component } from '@angular/core';
import {AuthService} from "../services/auth.service";
import {Router} from "@angular/router";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {CommonModule} from "@angular/common";
import {LoadingService} from "../services/loading.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading: boolean = false;
  errorMessage: string | null = null;

  constructor(private authService: AuthService,
              private fb: FormBuilder,
              private loadingService: LoadingService,
              private router: Router) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(4)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false]
    });

    this.loadingService.loadingForm$.subscribe((loading) => {
      this.isLoading = loading;
    })
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      return;
    }

    this.loadingService.showLoadingForm();

    this.authService.login(this.loginForm.value).subscribe({
      complete: () => {
        this.loadingService.hideLoadingForm()
        this.router.navigate(['/app-selection']);
      }
    });
  }
}
