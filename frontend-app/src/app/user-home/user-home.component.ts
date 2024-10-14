import {Component, OnInit} from '@angular/core';
import {LoadingService} from "../services/loading.service";
import {UserProfile, UserService} from "../services/user.service";
import {DatePipe} from "@angular/common";
import {AuthService} from "../services/auth.service";

@Component({
  selector: 'app-user-home',
  standalone: true,
  imports: [
    DatePipe
  ],
  templateUrl: './user-home.component.html',
  styleUrl: './user-home.component.css'
})
export class UserHomeComponent implements OnInit {

  profile: UserProfile | null = null;

  constructor(private userService: UserService, private loadingService: LoadingService) {
  }

  ngOnInit(): void {
    this.loadingService.showOverlay();
    this.userService.fetchUserProfile().subscribe({
      next: (userProfile) => {
        this.profile = userProfile;
      },
      error: (error) => {
        this.loadingService.hideOverlay();
      },
      complete: () => {
        this.loadingService.hideOverlay();
      }
    })
  }
}
