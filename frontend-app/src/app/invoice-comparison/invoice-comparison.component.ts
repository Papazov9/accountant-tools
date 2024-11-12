import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {LoadingService} from "../services/loading.service";
import {InvoiceComparisonService} from "../services/invoice-comparison.service";
import {HttpClient, HttpEventType, HttpResponse, HttpStatusCode} from "@angular/common/http";
import {CommonModule, NgIf} from "@angular/common";
import {UserProfile, UserService} from "../services/user.service";
import Swal from "sweetalert2";
import {ToggleService} from "../services/toggle.service";
import {delay, retryWhen, take} from "rxjs";

@Component({
  selector: 'app-invoice-comparison',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIf,
    CommonModule
  ],
  templateUrl: './invoice-comparison.component.html',
  styleUrl: './invoice-comparison.component.css'
})
export class InvoiceComparisonComponent implements OnInit {
  csvFiles: File[] = [];
  txtFiles: File[] = [];
  isFreePlan: boolean = true;
  userProfile?: UserProfile;

  constructor(private comparisonService: InvoiceComparisonService, private userService: UserService, private loadingService: LoadingService, private cdr: ChangeDetectorRef, private toggleService: ToggleService) {
  }

  ngOnInit(): void {
    this.loadingService.showOverlay();
    this.userService.userProfile$.subscribe({
      next: userProfile => {
        if (userProfile) {
          this.userProfile = userProfile;
          this.isFreePlan = this.userProfile?.subscription?.title === 'FREE';
          this.cdr.detectChanges();
        }
        this.loadingService.hideOverlay();
      },
      error: () => {
        this.loadingService.hideOverlay();
      },
      complete: () => {
        this.loadingService.hideOverlay();
      }
    });
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  onCsvDrop(event: DragEvent): void {
    event.preventDefault();
    this.checkPlan();
    if (event.dataTransfer?.files) {
      if (this.isFreePlan) {
        this.csvFiles.push(event.dataTransfer.files[0]);
      } else {
        const filesToUpload: File[] = Array.from(event.dataTransfer.files);
        filesToUpload.forEach(file => {
          if (!this.csvFiles.some(existingFile => existingFile.name === file.name)) {
            this.csvFiles.push(file);
          }
        });
      }
    }
  }

  private checkPlan() {
    if (this.isFreePlan && this.csvFiles.length >= 1) {
      alert("Free plan allows only one CSV file. Please upgrade to upload more.");
      return;
    }
  }

  removeCsvFile(file: File): void {
    this.csvFiles = this.csvFiles.filter(f => f !== file);
  }

  removeTxtFile(file: File): void {
    this.txtFiles = this.txtFiles.filter(f => f !== file);
  }

  onTxtDrop(event: DragEvent): void {
    event.preventDefault();
    this.checkPlan();
    if (event.dataTransfer?.files) {
      if (this.isFreePlan) {
        this.txtFiles.push(event.dataTransfer.files[0]);
      } else {
        const filesToUpload: File[] = Array.from(event.dataTransfer.files);
        filesToUpload.forEach(file => {
          if (!this.txtFiles.some(existingFile => existingFile.name === file.name)) {
            this.txtFiles.push(file);
          }
        });
      }
    }
  }

  onCsvFileSelect(event: any): void {
    this.checkPlan();
    const selectedFiles: File[] = Array.from(event.target.files);
    if (this.isFreePlan && selectedFiles.length >= 1) {
      this.csvFiles.push(selectedFiles[0]);
    } else {
      selectedFiles.forEach(file => {
        if (!this.csvFiles.some(existingFile => existingFile.name === file.name)) {
          this.csvFiles.push(file);
        }
      });
    }
  }

  onTxtFileSelect(event: any): void {
    const selectedFiles: File[] = Array.from(event.target.files);

    if (this.isFreePlan && selectedFiles.length >= 1) {
      this.txtFiles.push(selectedFiles[0]);
    } else {
      selectedFiles.forEach(file => {
        if (!this.txtFiles.some(existingFile => existingFile.name === file.name)) {
          this.txtFiles.push(file);
        }
      });
    }
  }

  triggerFileInput(inputId: string): void {
    this.checkComparisonsCount();
    document.getElementById(inputId)?.click();
  }

  uploadFiles(): void {
    this.checkComparisonsCount();
    if (this.csvFiles.length === 0 || this.txtFiles.length === 0) {
      Swal.fire({
        title: 'Attention',
        text: `Please select files from both types, otherwise the comparison cannot be possible!`,
        icon: 'warning',
        confirmButtonColor: '#584ca6',
        confirmButtonText: 'Okay',
        background: '#ffffff'
      });
      return;
    }

    this.comparisonService.uploadFiles(this.csvFiles, this.txtFiles).subscribe({
        next: (event) => {

          if (event.type === HttpEventType.Response) {
            const blob: Blob = new Blob([event.body], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'});
            const url = window.URL.createObjectURL(blob);

            const a = document.createElement('a');
            a.href = url;
            const contentDisposition = event.headers.get('content-disposition');
            let fileName: string = "comparison_report.xlsx";
            const index = contentDisposition.indexOf("=");

            if (index > -1) {
              fileName = contentDisposition.substring(index + 1);
            }

            a.download = fileName;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
            this.userService.fetchUserProfile();
          }

          if (event.status === HttpStatusCode.Ok) {
            this.resetPage();
            console.log(event);
          }
        },
        error: (error) => {
          this.resetPage();
          console.error(error);
        }
      }
    );
  }

  private checkComparisonsCount() {
    if (this.userProfile?.subscription?.comparisons === 0) {
      Swal.fire({
        title: 'Attention',
        text: `It seems you are out of comparisons! Would you want to upgrade your plan to add some?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#584ca6',
        confirmButtonText: 'Upgrade Plan!',
        cancelButtonColor: '#d33',
        background: '#ffffff'
      }).then(result => {
        if (result.isConfirmed) {
          this.toggleService.showSubscriptions();
        }
      });

      return;
    }
  }

  resetPage() {
    this.csvFiles = [];
    this.txtFiles = [];
  }

}
