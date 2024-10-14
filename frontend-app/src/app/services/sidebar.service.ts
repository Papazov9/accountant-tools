import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SidebarService {
  private sidebarState = new BehaviorSubject<boolean>(false); // false means closed by default
  sidebarState$ = this.sidebarState.asObservable();

  toggleSidebar(): void {
    this.sidebarState.next(!this.sidebarState.value);
  }

  openSidebar(): void {
    this.sidebarState.next(true);
  }

  closeSidebar(): void {
    this.sidebarState.next(false);
  }

  isSidebarOpen(): boolean {
    return this.sidebarState.value;
  }
}
