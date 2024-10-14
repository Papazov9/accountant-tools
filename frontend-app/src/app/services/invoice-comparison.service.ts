import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpEvent, HttpEventType} from "@angular/common/http";
import {catchError, Observable, tap, throwError} from "rxjs";
import {AuthService, CurrentUser} from "./auth.service";
import {LoadingService} from "./loading.service";

@Injectable({
  providedIn: 'root'
})
export class InvoiceComparisonService {
  currentUsername: string = "";
  private readonly API_URL = 'http://localhost:8080/api/files/upload'; // Adjust to your backend API endpoint

  constructor(private http: HttpClient, private authService: AuthService, private loadingService: LoadingService) { }

  /**
   * Uploads the selected CSV and TXT files to the backend for comparison.
   * @param csvFiles List of CSV files selected by the user.
   * @param txtFiles List of TXT files selected by the user.
   * @returns Observable with comparison result or error if the upload fails.
   */
  uploadFiles(csvFiles: File[], txtFiles: File[]): Observable<any> {
    const formData: FormData = new FormData();

    csvFiles.forEach((csv: File) => {
      formData.append("csvFiles", csv, csv.name);
    });

    txtFiles.forEach((txt: File) => {
      formData.append("txtFiles", txt, txt.name);
    });
    this.authService.currentUser$.subscribe((user: CurrentUser | null) => {
      if (user) {
        this.currentUsername = user.username;
      }
    })
    formData.append('username', this.currentUsername);
    this.loadingService.showOverlay();
    return this.http.post(this.API_URL, formData, {
      reportProgress: true,
      observe: 'events',
      responseType: 'arraybuffer'
    }).pipe(
      tap((event: HttpEvent<any>) => {
        if (event.type === HttpEventType.Response) {
          this.loadingService.hideOverlay(); // Hide overlay on response
        }
      }),
      catchError(err => {
        this.loadingService.hideOverlay(); // Hide overlay if there's an error
        throw err;
      })
    );
  }
}
