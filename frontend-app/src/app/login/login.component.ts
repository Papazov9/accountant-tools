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
              private router: Router,
              private fb: FormBuilder,
              private loadingService: LoadingService) {
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
      next: (response) => {
        this.authService.setToken(response.token, this.loginForm.value.rememberMe);
        this.router.navigate(['dashboard']);
      },
      error: (error) => {
        console.log(error.error.message);
        this.errorMessage = error.error.message;
        console.log(error.error.message);
        this.loadingService.hideLoadingForm();
      },
      complete: () => {
        console.log("Login completed!");
        this.loadingService.hideLoadingForm();
      }
    })
  }
}
