import { ErrorHandler, Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import {LoadingService} from "./loading.service";

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {

  constructor(private router: Router, private loaderService: LoadingService) {}

  handleError(error: any): void {
    if (error instanceof HttpErrorResponse) {
      this.handleHttpError(error);
    } else {
      console.error(error);
      this.router.navigate(['/error']);
    }
    this.loaderService.hideOverlay();
  }

  private handleHttpError(error: HttpErrorResponse): void {
    switch (error.status) {
      case 400:
        this.router.navigate(['/error', 400]);
        break;
      case 401:
        this.router.navigate(['/error', 401]);
        break;
      case 404:
        this.router.navigate(['/error', 404]);
        break;
      case 500:
        this.router.navigate(['/error', 500]);
        break;
      default:
        this.router.navigate(['/error']);
        break;
    }
  }
}
