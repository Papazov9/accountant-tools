import { Component } from '@angular/core';
import {AuthService} from "../services/auth.service";
import {Router} from "@angular/router";
import { FormsModule } from '@angular/forms';
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: true,
  imports: [CommonModule],
})
export class LoginComponent {
  username: string = '';
  password: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    if (this.username === 'admin' && this.password === 'password') {
      this.authService.login('fake-jwt-token');
      this.router.navigate(['/dashboard']);
    } else {
      alert('Invalid login');
    }
  }
}
