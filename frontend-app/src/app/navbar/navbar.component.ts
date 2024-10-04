import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../services/auth.service";
import {CommonModule} from "@angular/common";

@Component({
  standalone: true,
  imports: [CommonModule],
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  constructor(private authService: AuthService, private router: Router, private route: ActivatedRoute) { }
  isMenuOpen = false;  // State for mobile menu

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }

  isLoggedIn(): boolean {
    return false;
  }

  isLoginOrRegisterPage() {
    return this.router.url === '/login' || this.router.url === '/register';
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/home']);
  }
}
