import {Component} from '@angular/core';
import {CommonModule} from "@angular/common";
import {Router, RouterLink} from "@angular/router";

@Component({
  selector: 'app-welcome-section',
  standalone: true,
  imports: [CommonModule, CommonModule, RouterLink],
  templateUrl: './welcome-section.component.html',
  styleUrl: './welcome-section.component.css'
})
export class WelcomeSectionComponent {

  constructor(private router: Router) {
  }

  goToRegister(): void {
    this.router.navigate(['/register']);
  }
}
