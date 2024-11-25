import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {CommonModule} from "@angular/common";
import {AdminService} from "../services/admin.service";
import {Router} from "@angular/router";
import {LoadingService} from "../services/loading.service";

@Component({
  selector: 'app-admin-access',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-access.component.html',
  styleUrl: './admin-access.component.css'
})
export class AdminAccessComponent implements OnInit {

  accessForm!: FormGroup;
  errorMessage: string | null = null;

  constructor(private fb: FormBuilder, private adminService: AdminService, private router: Router, private loadingService: LoadingService) {
  }

  ngOnInit() {

    if (sessionStorage.getItem("admin-access") == "true") {
      this.router.navigate(['/admin-panel']);
      return;
    }

    this.accessForm = this.fb.group({
      activationCode: ['', [Validators.required, Validators.minLength(12), Validators.maxLength(12)]],
    });

    this.loadingService.showOverlay();
    this.adminService.generateActivationCode().subscribe({
      next: (response) => {

        if (!response.isSuccess) {
          console.error("ERROR!");
        }
      },
      error: err => {
        this.loadingService.hideOverlay();
      },
      complete: () => {
        this.loadingService.hideOverlay();
      }
    });
  }

  get activationCode() {
    return this.accessForm.get('activationCode');
  }

  isFieldInvalid(fieldName: string) : boolean {
    const field = this.accessForm.get(fieldName);
    return !!field?.errors && (field.dirty || field.touched);
  }

  verifyCode(): void {
    if (this.accessForm.valid) {
      const {activationCode} = this.accessForm.value;

      this.adminService.verifyActivationCode(activationCode).subscribe({
        next: (response) => {
          if (response.success) {
            sessionStorage.setItem("admin-access", 'true');
            this.router.navigate(['/admin-panel']);
          } else {
            sessionStorage.removeItem("admin-access");
            this.errorMessage = 'Failed to verify activation code!';
          }
        },
        error: (err) => {
          sessionStorage.removeItem("admin-access");
          this.errorMessage = 'Failed to verify activation code!';
          throw err.error;
        }
      })
    }
  }
}
