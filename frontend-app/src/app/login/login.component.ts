import { Component } from '@angular/core';
import {AuthService} from "../services/auth.service";
import {Router} from "@angular/router";
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
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
