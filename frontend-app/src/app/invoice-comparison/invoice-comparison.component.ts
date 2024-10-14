import { Component } from '@angular/core';
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
  processingProgress: number = 0;
  uploadProgress: number = 0;
  isProcessing: boolean = false;
  remainingComparisons: number = 0;
  subscriptionPlan: string = "";

  constructor(private http: HttpClient, private comparisonService: InvoiceComparisonService) {}

  onCsvFileSelect(event: any): void {
    const selectedFiles: File[] = Array.from(event.target.files);  // Convert selected files to an array
    selectedFiles.forEach(file => {
      if (!this.csvFiles.some(existingFile => existingFile.name === file.name)) {
        this.csvFiles.push(file);  // Only add the file if it's not already present
      }
    });  }

  onTxtFileSelect(event: any): void {
    const selectedFiles: File[] = Array.from(event.target.files);  // Convert selected files to an array
    selectedFiles.forEach(file => {
      if (!this.txtFiles.some(existingFile => existingFile.name === file.name)) {
        this.txtFiles.push(file);  // Only add the file if it's not already present
      }
    });  }
  uploadFiles(): void {
    if (this.csvFiles.length === 0 || this.txtFiles.length === 0) {
      alert("Please select both CSV and TXT files.");
      return;
    }

    this.comparisonService.uploadFiles(this.csvFiles, this.txtFiles).subscribe({
      next: (event) => {
        if (event.status === HttpStatusCode.Ok) {
          console.log(event);
        }
      },
      error: (error) => {
        console.error('Error uploading files', error);
      }}
    );
  }

}
