import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class AppSelectionService {

  private selectedApp = new BehaviorSubject<'invoice-comparison' | 'inventory'>('invoice-comparison');
  selectedApp$ = this.selectedApp.asObservable();

  setSelectedApp(app: 'invoice-comparison' | 'inventory') {
    this.selectedApp.next(app);
    if (app) {
      localStorage.setItem("selectedApp", app);
    }
  }

  getSelectedApp(): string | null {
    return localStorage.getItem('selectedApp');
  }
}
