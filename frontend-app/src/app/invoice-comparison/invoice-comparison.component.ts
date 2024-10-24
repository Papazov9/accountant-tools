import {Component} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {LoadingService} from "../services/loading.service";
import {InvoiceComparisonService} from "../services/invoice-comparison.service";
import {HttpClient, HttpEventType, HttpResponse, HttpStatusCode} from "@angular/common/http";
import {CommonModule, NgIf} from "@angular/common";

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
export class InvoiceComparisonComponent {
  csvFiles: File[] = [];
  txtFiles: File[] = [];
  remainingComparisons: number = 5;
  subscriptionPlan: string = "FREE";
  isFreePlan: boolean = true;

  constructor(private comparisonService: InvoiceComparisonService) {
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
    selectedFiles.forEach(file => {
      if (!this.csvFiles.some(existingFile => existingFile.name === file.name)) {
        this.csvFiles.push(file);
      }
    });
  }

  onTxtFileSelect(event: any): void {
    const selectedFiles: File[] = Array.from(event.target.files);  // Convert selected files to an array
    selectedFiles.forEach(file => {
      if (!this.txtFiles.some(existingFile => existingFile.name === file.name)) {
        this.txtFiles.push(file);
      }
    });
  }

  triggerFileInput(inputId: string): void {
    document.getElementById(inputId)?.click();
  }

  uploadFiles(): void {
    if (this.csvFiles.length === 0 || this.txtFiles.length === 0) {
      alert("Please select both CSV and TXT files.");
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
          }

          if (event.status === HttpStatusCode.Ok) {
            this.resetPage();
            console.log(event);
          }
        },
        error: (error) => {
          this.resetPage();
          console.error('Error uploading files', error);
        }
      }
    );
  }

  resetPage() {
    this.csvFiles = [];
    this.txtFiles = [];
  }

}
