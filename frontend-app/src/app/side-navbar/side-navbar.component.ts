import {Component, HostListener, OnInit} from '@angular/core';
import {AuthService} from "../services/auth.service";
import {Router, RouterLink} from "@angular/router";
import {NgClass, NgIf} from "@angular/common";
import {UserService} from "../services/user.service";
import {NavbarComponent} from "../navbar/navbar.component";
import {ToggleService} from "../services/toggle.service";

@Component({
  selector: 'app-side-navbar',
  standalone: true,
  imports: [
    RouterLink,
    NgClass,
    NgIf
  ],
  templateUrl: './side-navbar.component.html',
  styleUrls: ['./side-navbar.component.css']
})
export class SideNavbarComponent implements OnInit {
  profile: any = null;
  userPlan?: string;

  constructor(private authService: AuthService, private userService: UserService, private router: Router, private toggleService: ToggleService) { }

  ngOnInit(): void {
    this.userService.fetchUserProfile().subscribe(profile => {
      if (profile) {
        this.profile = profile;
        this.userPlan = profile.subscription?.title;
      } else {
        console.log("User not logged in!")
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
