import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {NavbarComponent} from "./navbar/navbar.component";
import {NavigationCancel, NavigationEnd, NavigationError, NavigationStart, Router, RouterOutlet} from "@angular/router";
import {LoadingService} from "./services/loading.service";
import {LoadingSpinnerComponent} from "./loading-spinner/loading-spinner.component";
import {AuthService} from "./services/auth.service";
import {NgClass, NgIf} from "@angular/common";
import {SideNavbarComponent} from "./side-navbar/side-navbar.component";
import {SidebarService} from "./services/sidebar.service";
import {FooterComponent} from "./footer/footer.component";
import {SubscriptionsComponent} from "./subscriptions/subscriptions.component";
import {ToggleService} from "./services/toggle.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  standalone: true,
  imports: [
    NavbarComponent,
    RouterOutlet,
    LoadingSpinnerComponent,
    NgIf,
    SideNavbarComponent,
    NgClass,
    FooterComponent,
    SubscriptionsComponent
  ]
})
export class AppComponent implements OnInit {
  isNavOpen: boolean = true;
  isLoading: boolean = false;
  isSubVisible: boolean = false;

  constructor(
    private authService: AuthService,
    private sideBarService: SidebarService,
    private loadingService: LoadingService,
    private cdr: ChangeDetectorRef,
    private toggleService: ToggleService,
  ) {
  }

  ngOnInit(): void {
    // Subscribe to sidebar service to keep the sidebar state updated
    this.toggleService.loadingOverlay$.subscribe((subVisible) => this.isSubVisible = subVisible);
    this.loadingService.loadingOverlay$.subscribe((isLoading) => {
      this.isLoading = isLoading;
      this.cdr.detectChanges();
    });
    this.sideBarService.sidebarState$.subscribe(isOpen => {
      this.isNavOpen = isOpen;
      this.cdr.detectChanges();
    });

    // Set sidebar open or closed depending on screen size
    this.checkScreenSize();
    window.addEventListener('resize', this.checkScreenSize.bind(this));
  }

  checkScreenSize(): void {
    if (window.innerWidth <= 768) {
      this.sideBarService.closeSidebar(); // Close sidebar on mobile screens
    } else {
      this.sideBarService.openSidebar(); // Open sidebar on desktop screens
    }
    this.cdr.detectChanges();
  }

  isLoggedIn(): boolean {
    return this.authService.isAuthenticated();
  }

  toggleSideNav(): void {
    this.sideBarService.toggleSidebar();
    this.cdr.detectChanges();
  }
}
