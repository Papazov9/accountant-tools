import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {NavbarComponent} from "./navbar/navbar.component";
import {Router, RouterOutlet} from "@angular/router";
import {LoadingService} from "./services/loading.service";
import {LoadingSpinnerComponent} from "./loading-spinner/loading-spinner.component";
import {AuthService} from "./services/auth.service";
import {NgClass, NgIf} from "@angular/common";
import {SideNavbarComponent} from "./side-navbar/side-navbar.component";
import {SidebarService} from "./services/sidebar.service";
import {FooterComponent} from "./footer/footer.component";
import {SubscriptionsComponent} from "./subscriptions/subscriptions.component";
import {ToggleService} from "./services/toggle.service";
import {LanguageSelectorComponent} from "./language-selector/language-selector.component";

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
    SubscriptionsComponent,
    LanguageSelectorComponent
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
    private router: Router
  ) {
  }

  ngOnInit(): void {
    this.toggleService.loadingOverlay$.subscribe((subVisible) => this.isSubVisible = subVisible);
    this.loadingService.loadingOverlay$.subscribe((isLoading) => {
      this.isLoading = isLoading;
      this.cdr.detectChanges();
    });
    this.sideBarService.sidebarState$.subscribe(isOpen => {
      this.isNavOpen = isOpen;
      this.cdr.detectChanges();
    });

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

  isTutorial() {
    return this.router.url === '/how-it-works';
  }

  toggleSideNav(): void {
    this.sideBarService.toggleSidebar();
    this.cdr.detectChanges();
  }
}
