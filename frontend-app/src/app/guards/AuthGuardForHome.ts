import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import {AuthService} from "../services/auth.service";

@Injectable({
  providedIn: 'root'
})
export class AuthGuardForHome implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isAuthenticated()) { // Assume this method checks the login status
      this.router.navigate(['/dashboard']); // Redirect to dashboard if logged in
      return false;
    } else {
      return true; // Continue to home component if not logged in
    }
  }
}
