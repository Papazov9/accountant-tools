import {Component} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {AuthService} from "../services/auth.service";
import {Router} from "@angular/router";
import {DatePipe, NgIf} from "@angular/common";
import {LoadingService} from "../services/loading.service";

@Component({
  selector: 'app-register',
  standalone: true,
    imports: [
        ReactiveFormsModule,
        NgIf
    ],
  providers: [DatePipe],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  registerForm: FormGroup;
  isLoading: boolean = false;

  constructor(private fb: FormBuilder,
              private authService: AuthService,
              private router: Router,
              private datePipe: DatePipe,
              private loadingService: LoadingService) {
    this.registerForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      username: ['', [Validators.required, Validators.minLength(4)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(6)]],
      birthDate: ['', [Validators.required]]
    }, {validator: this.matchPasswords('password', 'confirmPassword')});

    this.loadingService.loadingForm$.subscribe((loading) => {
      this.isLoading = loading;
    })
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
    this.loadingService.showLoadingForm();
    if (this.registerForm.invalid) {
      this.loadingService.hideLoadingForm();
      return;
    }

    let birthDate = this.registerForm.value.birthDate;
    if (birthDate) {
      birthDate = this.datePipe.transform(birthDate, 'yyyy-MM-dd');
      this.registerForm.patchValue({birthDate});
    }

    this.authService.register(this.registerForm.value).subscribe({
        next: (response) => {
          console.log(response.message);
          this.router.navigate(['login']);
        },
        error: (error) => {
          console.log(error.message)
          this.loadingService.hideLoadingForm();
        },
        complete: () => {
          console.log("Registration completed.")
          this.loadingService.hideLoadingForm();
        }
      }
    )
  }
}
