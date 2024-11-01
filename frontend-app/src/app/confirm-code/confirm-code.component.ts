import { Component } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {AuthService} from "../services/auth.service";
import {ActivatedRoute, Router} from "@angular/router";
import Swal from "sweetalert2";

@Component({
  selector: 'app-confirm-code',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './confirm-code.component.html',
  styleUrl: './confirm-code.component.css'
})
export class ConfirmCodeComponent {
  confirmCodeForm: FormGroup;
  email: string;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.confirmCodeForm = this.fb.group({
      code: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]]
    });

    this.email = this.route.snapshot.queryParamMap.get('email') || '';
  }

  onSubmit() {
    if (this.confirmCodeForm.invalid) return;

    const code = this.confirmCodeForm.get('code')?.value;

    this.authService.confirmEmail(this.email, code).subscribe({
      next: () => {
        alert('Email successfully confirmed!');
        this.router.navigate(['/login']);
      },
      error: () => {
        Swal.fire({
          title: 'Wrong confirmation code',
          text: 'Something went wrong with code confirmation. Please try again or contactus for more information. Keep in mind that the verification code is valid for 1 hour, if you exceed this time you have to request it again.',
          icon: 'error',
          showConfirmButton: false,
          showCloseButton: true,
        });
      }
    });
  }
}
