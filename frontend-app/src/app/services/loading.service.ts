import { Injectable } from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LoadingService {

  private _loadingOverlay = new BehaviorSubject<boolean>(false);
  readonly loadingOverlay$ = this._loadingOverlay.asObservable();

  private _loadingForm = new BehaviorSubject<boolean>(false);
  readonly loadingForm$ = this._loadingForm.asObservable();

  showOverlay() {
    this._loadingOverlay.next(true);
  }

  hideOverlay() {
    this._loadingOverlay.next(false);
  }

  showLoadingForm(){
    this._loadingForm.next(true);
  }

  hideLoadingForm(){
    this._loadingForm.next(false);
  }
}
