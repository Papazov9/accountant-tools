import {Component} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {AuthService} from "../services/auth.service";
import {Router} from "@angular/router";
import {DatePipe, NgClass, NgIf} from "@angular/common";
import {LoadingService} from "../services/loading.service";

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIf,
    NgClass
  ],
  providers: [DatePipe],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  registerForm: FormGroup;
  isLoading: boolean = false;
  passwordErrors: any = {};
  showPasswordRequirements: boolean = false;

  constructor(private fb: FormBuilder,
              private authService: AuthService,
              private loadingService: LoadingService) {
    this.registerForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      username: ['', [Validators.required, Validators.minLength(4)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(6)]],
      gender: ['', [Validators.required]]
    }, {validator: this.matchPasswords('password', 'confirmPassword')});

    this.onPasswordChanges();
    this.loadingService.loadingForm$.subscribe((loading) => {
      this.isLoading = loading;
    })
  }

  onPasswordChanges() {
    this.registerForm.get('password')?.valueChanges.subscribe(password => {
      this.passwordErrors = {
        minLength: password.length >= 8,
        hasUppercase: /[A-Z]/.test(password),
        hasLowercase: /[a-z]/.test(password),
        hasNumber: /\d/.test(password),
        hasSpecialChar: /[!@#$%^&*(),.?":{}|<>]/.test(password),
      };
    });
  }

  isPasswordValid(): boolean {
    const password = this.registerForm.get('password')?.value;
    if (!password) return false; // Return false if password is empty

    const hasMinLength = password.length >= 8;
    const hasUppercase = /[A-Z]/.test(password);
    const hasLowercase = /[a-z]/.test(password);
    const hasNumber = /[0-9]/.test(password);
    const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);

    return hasMinLength && hasUppercase && hasLowercase && hasNumber && hasSpecialChar;
  }

  isFieldInvalid(fieldName: string) {
    const control = this.registerForm.get(fieldName);
    return control?.invalid && (control.dirty || control.touched);
  }

  matchPasswords(password: string, confirmPassword: string) {
    return (formGroup: FormGroup) => {
      const pass = formGroup.controls[password];
      const confirmPass = formGroup.controls[confirmPassword];

      if (confirmPass.errors && !confirmPass.errors['passwordMismatch']) {
        return;
      }

      if (pass.value !== confirmPass.value) {
        confirmPass.setErrors({passwordMismatch: true});
      } else {
        confirmPass.setErrors(null);
      }
    }
  }

  onSubmit() {
    console.log('Submit triggered');
    this.loadingService.showLoadingForm();
    if (this.registerForm.invalid) {
      this.loadingService.hideLoadingForm();
      return;
    }

    this.authService.register(this.registerForm.value).subscribe({
      complete: () => this.loadingService.hideLoadingForm()
    });
  }

  onPasswordInput() {
    this.showPasswordRequirements = true;
  }

  onGenderClick() {
    console.log({
      formValid: this.registerForm.valid,
      passwordValid: this.isPasswordValid(),
      isLoading: this.isLoading
    });
  }

  onSubmitButtonClick() {
    console.log('Submit button clicked');
  }

}
