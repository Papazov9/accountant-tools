import {Component, OnInit} from '@angular/core';
import {AuthService} from "../services/auth.service";
import {RouterLink} from "@angular/router";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [
    RouterLink,
    NgIf
  ],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.css'
})
export class FooterComponent implements OnInit {

  isLoggedIn: boolean = false;

  constructor(private authService: AuthService) {
  }

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isAuthenticated();
  }

}