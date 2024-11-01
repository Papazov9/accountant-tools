import {Component, HostListener, OnInit} from '@angular/core';
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
  constructor(private router: Router, private route: ActivatedRoute) {
  }
  isMenuOpen = false;
  isSticky: boolean = false;

  @HostListener('window:scroll', [])
  onWindowScroll() {
    const offset = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0;
    this.isSticky = offset > 50; // Change this value depending on when you want the sticky effect to kick in
  }

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }

  isLoginOrRegisterPage() {
    return this.router.url === '/login' || this.router.url === '/register' || this.router.url === '/confirm-code';
  }

  scrollTo(event: MouseEvent) {
    event.preventDefault();

    const targetedSection = (event.target as HTMLElement).getAttribute('data-section');
    console.log(targetedSection);

    const selectionElement = document.getElementById(`${targetedSection}`);

    if (selectionElement) {
      selectionElement.scrollIntoView({behavior: 'smooth'});
    }

  }
}
