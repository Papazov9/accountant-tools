import {Component, HostListener, OnInit} from '@angular/core';
import {AuthService} from "../services/auth.service";
import {Router, RouterLink} from "@angular/router";
import {AsyncPipe, NgClass, NgIf} from "@angular/common";
import {UserService} from "../services/user.service";
import {ToggleService} from "../services/toggle.service";
import {AppSelectionService} from "../services/app-selection.service";
import {map, Observable} from "rxjs";

@Component({
  selector: 'app-side-navbar',
  standalone: true,
  imports: [
    RouterLink,
    NgClass,
    NgIf,
    AsyncPipe
  ],
  templateUrl: './side-navbar.component.html',
  styleUrls: ['./side-navbar.component.css']
})
export class SideNavbarComponent implements OnInit {
  profile: any = null;
  userPlan?: string;
  basePath: "invoice-comparison" | "inventory" | null = null;
  isAdmin$: Observable<boolean>;

  constructor(private authService: AuthService, private userService: UserService, private router: Router, private toggleService: ToggleService, private appSelectionService: AppSelectionService ) {
    this.isAdmin$ = this.authService.currentUser$.pipe(
      map(currentUser => !!currentUser && currentUser.roles.includes('ADMIN'))
    );
  }

  ngOnInit(): void {
    this.appSelectionService.selectedApp$.subscribe( app => {
      this.basePath = app;
    })
    this.userService.fetchUserProfile();
    this.userService.userProfile$.subscribe(profile => {
      if (profile) {
        this.profile = profile;
        this.userPlan = profile.subscription?.title;
      }
    });
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  clickSub() {
    this.toggleService.showSubscriptions();
  }
}
