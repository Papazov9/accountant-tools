import { Injectable } from '@angular/core';
import {HttpClient, HttpEvent, HttpEventType} from "@angular/common/http";
import {AuthService} from "./auth.service";
import {LoadingService} from "./loading.service";
import {catchError, Observable, tap} from "rxjs";


export interface HistoryRecord {
  batchId: string;
  status: string;
  date: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class HistoryService {
  private readonly API_URL = 'http://localhost:8080/api/files'; // Adjust to your backend API endpoint
  private username?: string;

  constructor(private http: HttpClient, private authService: AuthService, private loadingService: LoadingService) { }


  loadHistory():Observable<HistoryRecord[]> {
    this.authService.currentUser$.subscribe((currentUser) => {
      this.username = currentUser?.username;
    });

    return this.http.get<HistoryRecord[]>(`${this.API_URL}/history/${this.username}`);
  }

  downloadComparison(batchId: string) {
    return this.http.get(`${this.API_URL}/download/${batchId}`, {
      reportProgress: true,
      observe: 'response',
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
