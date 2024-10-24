import { Injectable } from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class ToggleService {

  private _subVisible = new BehaviorSubject<boolean>(false);
  readonly loadingOverlay$ = this._subVisible.asObservable();

  constructor() { }

  showSubscriptions() {
    this._subVisible.next(true);
  }
  hideSubscriptions() {
    this._subVisible.next(false);
  }
}
