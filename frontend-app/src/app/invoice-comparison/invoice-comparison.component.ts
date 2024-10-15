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
  remainingComparisons: number = 0;
  subscriptionPlan: string = "";

  constructor(private comparisonService: InvoiceComparisonService) {
  }

  onCsvFileSelect(event: any): void {
    const selectedFiles: File[] = Array.from(event.target.files);  // Convert selected files to an array
    selectedFiles.forEach(file => {
      if (!this.csvFiles.some(existingFile => existingFile.name === file.name)) {
        this.csvFiles.push(file);  // Only add the file if it's not already present
      }
    });
  }

  onTxtFileSelect(event: any): void {
    const selectedFiles: File[] = Array.from(event.target.files);  // Convert selected files to an array
    selectedFiles.forEach(file => {
      if (!this.txtFiles.some(existingFile => existingFile.name === file.name)) {
        this.txtFiles.push(file);  // Only add the file if it's not already present
      }
    });
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
    // const csvInput = <HTMLInputElement>document.getElementById('csv-upload');
    // const txtInput = <HTMLInputElement>document.getElementById('txt-upload');
    //
    // if (csvInput) {
    //   csvInput.value = '';
    // }
    //
    // if (txtInput) {
    //   txtInput.value = '';
    // }
  }

}
